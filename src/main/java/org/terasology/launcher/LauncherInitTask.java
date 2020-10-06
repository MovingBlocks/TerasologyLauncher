/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kohsuke.github.GHRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.packages.PackageManager;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.settings.LauncherSettings;
import org.terasology.launcher.settings.LauncherSettingsValidator;
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.updater.LauncherUpdater;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryCreator;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.HostServices;
import org.terasology.launcher.util.LauncherDirectoryUtils;
import org.terasology.launcher.util.LauncherManagedDirectory;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.util.Platform;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LauncherInitTask extends Task<LauncherConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(LauncherInitTask.class);

    private final Stage owner;
    private final HostServices hostServices;

    public LauncherInitTask(final Stage newOwner, HostServices hostServices) {
        this.owner = newOwner;
        this.hostServices = hostServices;
    }

    /**
     * Assembles a {@link LauncherConfiguration}.
     *
     * @return a complete launcher configuration or {@code null} if the initialization failed
     */
    @Override
    protected LauncherConfiguration call() {
        // TODO: Use idiomatic JavaFX error handling.

        try {
            // get OS info
            final Platform platform = getPlatform();

            // init directories
            updateMessage(BundleUtils.getLabel("splash_initLauncherDirs"));
            final Path installationDirectory = LauncherDirectoryUtils.getInstallationDirectory();
            final Path userDataDirectory = getLauncherDirectory(platform);

            final Path downloadDirectory = getDirectoryFor(LauncherManagedDirectory.DOWNLOAD, userDataDirectory);
            final Path tempDirectory = getDirectoryFor(LauncherManagedDirectory.TEMP, userDataDirectory);
            final Path cacheDirectory = getDirectoryFor(LauncherManagedDirectory.CACHE, userDataDirectory);

            // launcher settings
            final Path settingsFile = userDataDirectory.resolve(Settings.DEFAULT_FILE_NAME);
            final LauncherSettings launcherSettings = getLauncherSettings(settingsFile);

            // validate the settings
            LauncherSettingsValidator.validate(launcherSettings);

            if (launcherSettings.isSearchForLauncherUpdates()) {
                final boolean selfUpdaterStarted =
                        checkForLauncherUpdates(downloadDirectory, tempDirectory, launcherSettings.isKeepDownloadedFiles());

                if (selfUpdaterStarted) {
                    logger.info("Exit old TerasologyLauncher: {}", TerasologyLauncherVersionInfo.getInstance());
                    return NullLauncherConfiguration.getInstance();
                }
            }

            // game directories
            updateMessage(BundleUtils.getLabel("splash_initGameDirs"));
            final Path gameDirectory = getDirectoryFor(LauncherManagedDirectory.GAMES, installationDirectory);
            final Path gameDataDirectory = getGameDataDirectory(platform, launcherSettings.getGameDataDirectory());

            // TODO: Does this interact with any remote server for fetching/initializing the database?
            logger.trace("Setting up Package Manager");
            final PackageManager packageManager = new PackageManager(userDataDirectory, gameDirectory);
            if (!packageManager.validateSources()) {
                if (confirmSourcesOverwrite()) {
                    packageManager.copyDefaultSources();
                } else {
                    throw new IllegalStateException("Error reading sources file");
                }
            }
            packageManager.initDatabase();
            packageManager.syncDatabase();

            logger.trace("Change LauncherSettings...");
            launcherSettings.setGameDirectory(gameDirectory);
            launcherSettings.setGameDataDirectory(gameDataDirectory);
            // TODO: Rewrite gameVersions.fixSettingsBuildVersion(launcherSettings);

            storeLauncherSettingsAfterInit(launcherSettings, settingsFile);

            logger.trace("Creating launcher frame...");

            return new LauncherConfiguration(userDataDirectory, downloadDirectory, launcherSettings, packageManager);
        } catch (LauncherStartFailedException e) {
            logger.warn("Could not configure launcher.");
        }

        return null;
    }

    private Platform getPlatform() {
        logger.trace("Init Platform...");
        updateMessage(BundleUtils.getLabel("splash_checkOS"));
        final Platform platform = Platform.getPlatform();
        if (!platform.isLinux() && !platform.isMac() && !platform.isWindows()) {
            logger.warn("Detected unexpected platform: {}", platform);
        }
        logger.debug("Platform: {}", platform);
        return platform;
    }

    private void initDirectory(Path dir, String errorLabel, DirectoryCreator... creators)
            throws LauncherStartFailedException {
        try {
            for (DirectoryCreator creator : creators) {
                creator.apply(dir);
            }
        } catch (IOException e) {
            logger.error("Directory '{}' cannot be created or used! '{}'", dir.getFileName(), dir, e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel(errorLabel) + "\n" + dir);
            throw new LauncherStartFailedException();
        }
        logger.debug("{} directory: {}", dir.getFileName(), dir);
    }

    private Path getDirectoryFor(LauncherManagedDirectory directoryType, Path launcherDirectory)
            throws LauncherStartFailedException {

        Path dir = directoryType.getDirectoryPath(launcherDirectory);

        initDirectory(dir, directoryType.getErrorLabel(), directoryType.getCreators());
        return dir;
    }

    private Path getLauncherDirectory(Platform platform) throws LauncherStartFailedException {
        final Path launcherDirectory =
                LauncherDirectoryUtils.getApplicationDirectory(platform, LauncherDirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
        initDirectory(launcherDirectory, "message_error_launcherDirectory", FileUtils::ensureWritableDir);
        return launcherDirectory;
    }

    private LauncherSettings getLauncherSettings(Path settingsFile) throws LauncherStartFailedException {
        logger.trace("Init LauncherSettings...");
        updateMessage(BundleUtils.getLabel("splash_retrieveLauncherSettings"));

        final LauncherSettings settings = Optional.ofNullable(Settings.load(settingsFile)).orElse(Settings.getDefault());
        settings.init();

        logger.debug("Launcher Settings: {}", settings);

        return settings;
    }

    private boolean checkForLauncherUpdates(Path downloadDirectory, Path tempDirectory, boolean saveDownloadedFiles) {
        logger.trace("Check for launcher updates...");
        boolean selfUpdaterStarted = false;
        updateMessage(BundleUtils.getLabel("splash_launcherUpdateCheck"));
        final LauncherUpdater updater = new LauncherUpdater(TerasologyLauncherVersionInfo.getInstance());
        final GHRelease release = updater.updateAvailable();
        if (release != null) {
            logger.info("Launcher update available: {}", release.getTagName());
            updateMessage(BundleUtils.getLabel("splash_launcherUpdateAvailable"));
            boolean foundLauncherInstallationDirectory = false;
            try {
                final Path installationDir = LauncherDirectoryUtils.getInstallationDirectory();
                FileUtils.ensureWritableDir(installationDir);
                logger.trace("Launcher installation directory: {}", installationDir);
                foundLauncherInstallationDirectory = true;
            } catch (IOException e) {
                logger.error("The launcher installation directory can not be detected or used!", e);
                GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_launcherInstallationDirectory"));
                // Run launcher without an update. Don't throw a LauncherStartFailedException.
            }
            if (foundLauncherInstallationDirectory) {
                final boolean update = updater.showUpdateDialog(owner, release);
                if (update) {
                    showDownloadPage();
                    // TODO: start self-updater instead
                    if (false) {
                        final Path targetDirectory = saveDownloadedFiles ? downloadDirectory : tempDirectory;
                        selfUpdaterStarted = updater.update(targetDirectory, tempDirectory);
                    }
                }
            }
        }
        return selfUpdaterStarted;
    }

    private void showDownloadPage() {
        final String downloadPage = "https://terasology.org/download";
        try {
            hostServices.tryOpenUri(new URI(downloadPage));
        } catch (URISyntaxException e) {
            logger.info("Could not open '{}': {}", downloadPage, e.getMessage());
        }
    }

    private Path getGameDataDirectory(Platform os, Path settingsGameDataDirectory) throws LauncherStartFailedException {
        logger.trace("Init GameDataDirectory...");
        Path gameDataDirectory = settingsGameDataDirectory;
        if (gameDataDirectory != null) {
            try {
                FileUtils.ensureWritableDir(gameDataDirectory);
            } catch (IOException e) {
                logger.warn("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
                GuiUtils.showWarningMessageDialog(owner, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n"
                        + gameDataDirectory);

                // Set gameDataDirectory to 'null' -> user has to choose new game data directory
                gameDataDirectory = null;
            }
        }
        if (gameDataDirectory == null) {
            logger.trace("Choose data directory for the game...");
            updateMessage(BundleUtils.getLabel("splash_chooseGameDataDirectory"));
            gameDataDirectory = GuiUtils.chooseDirectoryDialog(owner, LauncherDirectoryUtils.getGameDataDirectory(os),
                    BundleUtils.getLabel("message_dialog_title_chooseGameDataDirectory"));
            if (Files.notExists(gameDataDirectory)) {
                logger.info("The new game data directory is not approved. The TerasologyLauncher is terminated.");
                javafx.application.Platform.exit();
            }
        }
        try {
            FileUtils.ensureWritableDir(gameDataDirectory);
        } catch (IOException e) {
            logger.error("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);
            throw new LauncherStartFailedException();
        }
        logger.debug("Game data directory: {}", gameDataDirectory);
        return gameDataDirectory;
    }

    /**
     * Shows a confirmation dialog for overwriting current sources file
     * with default values.
     *
     * @return whether the user confirms this overwrite
     */
    private boolean confirmSourcesOverwrite() {
        return CompletableFuture.supplyAsync(() -> {
            final Alert alert = new Alert(
                    Alert.AlertType.WARNING,
                    BundleUtils.getLabel("message_error_sourcesFile_content"),
                    ButtonType.OK,
                    new ButtonType(BundleUtils.getLabel("launcher_exit")));
            alert.setHeaderText(BundleUtils.getLabel("message_error_sourcesFile_header"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            return alert.showAndWait()
                    .map(btn -> btn == ButtonType.OK)
                    .orElse(false);
        }, javafx.application.Platform::runLater).join();
    }

    private void storeLauncherSettingsAfterInit(LauncherSettings launcherSettings, final Path settingsFile) throws LauncherStartFailedException {
        logger.trace("Store LauncherSettings...");
        updateMessage(BundleUtils.getLabel("splash_storeLauncherSettings"));
        try {
            Settings.store(launcherSettings, settingsFile);
        } catch (IOException e) {
            logger.error("The launcher settings cannot be stored! '{}'", settingsFile, e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_storeSettings"));
            //TODO: should we fail here, or is it fine to work with in-memory settings?
            throw new LauncherStartFailedException();
        }
        logger.debug("Launcher Settings stored: {}", launcherSettings);
    }
}
