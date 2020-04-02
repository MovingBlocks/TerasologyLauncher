/*
 * Copyright 2020 MovingBlocks
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

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.github.GitHubRelease;
import org.terasology.launcher.packages.PackageManager;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.settings.LauncherSettingsValidator;
import org.terasology.launcher.updater.LauncherUpdater;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryCreator;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.HostServices;
import org.terasology.launcher.util.LauncherDirectoryUtils;
import org.terasology.launcher.util.LauncherManagedDirectory;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            final OperatingSystem os = getOperatingSystem();

            // init directories
            updateMessage(BundleUtils.getLabel("splash_initLauncherDirs"));
            final Path installationDirectory = LauncherDirectoryUtils.getInstallationDirectory();
            final Path userDataDirectory = getLauncherDirectory(os);

            final Path downloadDirectory = getDirectoryFor(LauncherManagedDirectory.DOWNLOAD, userDataDirectory);
            final Path tempDirectory = getDirectoryFor(LauncherManagedDirectory.TEMP, userDataDirectory);
            final Path cacheDirectory = getDirectoryFor(LauncherManagedDirectory.CACHE, userDataDirectory);

            // launcher settings
            final BaseLauncherSettings launcherSettings = getLauncherSettings(userDataDirectory);

            // validate the settings
            LauncherSettingsValidator.validate(launcherSettings);

            final boolean serverAvailable = DownloadUtils.isJenkinsAvailable();
            if (serverAvailable && launcherSettings.isSearchForLauncherUpdates()) {
                final boolean selfUpdaterStarted = checkForLauncherUpdates(os, downloadDirectory, tempDirectory, launcherSettings.isKeepDownloadedFiles());
                if (selfUpdaterStarted) {
                    logger.info("Exit old TerasologyLauncher: {}", TerasologyLauncherVersionInfo.getInstance());
                    return NullLauncherConfiguration.getInstance();
                }
            }

            // game directories
            updateMessage(BundleUtils.getLabel("splash_initGameDirs"));
            final Path gameDirectory = getDirectoryFor(LauncherManagedDirectory.GAMES, installationDirectory);
            final Path gameDataDirectory = getGameDataDirectory(os, launcherSettings.getGameDataDirectory());

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

            storeLauncherSettingsAfterInit(launcherSettings);

            logger.trace("Creating launcher frame...");

            return new LauncherConfiguration(userDataDirectory, downloadDirectory, tempDirectory, cacheDirectory, launcherSettings, packageManager);
        } catch (LauncherStartFailedException e) {
            logger.warn("Could not configure launcher.");
        }

        return null;
    }

    private OperatingSystem getOperatingSystem() throws LauncherStartFailedException {
        logger.trace("Init OperatingSystem...");
        updateMessage(BundleUtils.getLabel("splash_checkOS"));
        final OperatingSystem os = OperatingSystem.getOS();
        if (os == OperatingSystem.UNKNOWN) {
            logger.error("The operating system is not supported! '{}' '{}' '{}'", System.getProperty("os.name"), System.getProperty("os.arch"),
                    System.getProperty("os.version"));
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_operatingSystem"));
            throw new LauncherStartFailedException();
        }
        logger.debug("Operating system: {}", os);
        return os;
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

    private Path getLauncherDirectory(OperatingSystem os) throws LauncherStartFailedException {
        final Path launcherDirectory =
                LauncherDirectoryUtils.getApplicationDirectory(os, LauncherDirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
        initDirectory(launcherDirectory, "message_error_launcherDirectory", FileUtils::ensureWritableDir);
        return launcherDirectory;
    }

    private BaseLauncherSettings getLauncherSettings(Path launcherDirectory) throws LauncherStartFailedException {
        logger.trace("Init LauncherSettings...");
        updateMessage(BundleUtils.getLabel("splash_retrieveLauncherSettings"));
        final BaseLauncherSettings launcherSettings = new BaseLauncherSettings(launcherDirectory);
        try {
            launcherSettings.load();
            launcherSettings.init();
        } catch (IOException e) {
            logger.error("The launcher settings can not be loaded or initialized! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_loadSettings") + "\n" + launcherSettings.getLauncherSettingsFilePath());
            throw new LauncherStartFailedException();
        }
        logger.debug("Launcher Settings: {}", launcherSettings);
        return launcherSettings;
    }

    private boolean checkForLauncherUpdates(OperatingSystem os, Path downloadDirectory, Path tempDirectory, boolean saveDownloadedFiles) {
        final String platform;
        if (os.isWindows()) {
            platform = "windows";
        } else if (os.isMac()) {
            platform = "mac";
        } else if (os == OperatingSystem.LINUX) {
            platform = "linux";
        } else {
            logger.warn("Current platform is unsupported: {}", System.getProperty("os.name"));
            platform = null;
        }

        logger.trace("Check for launcher updates...");
        boolean selfUpdaterStarted = false;
        updateMessage(BundleUtils.getLabel("splash_launcherUpdateCheck"));
        final LauncherUpdater updater = new LauncherUpdater(TerasologyLauncherVersionInfo.getInstance());
        final GitHubRelease release = updater.updateAvailable();
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
            if (foundLauncherInstallationDirectory && updater.confirmUpdate(owner, release)) {
                if (platform != null) {
                    // TODO: start self-updater
                    final Path targetDirectory = saveDownloadedFiles ? downloadDirectory : tempDirectory;
                    selfUpdaterStarted = updater.update(targetDirectory, tempDirectory);
                } else {
                    showDownloadPage();
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

    private Path getGameDataDirectory(OperatingSystem os, Path settingsGameDataDirectory) throws LauncherStartFailedException {
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
                Platform.exit();
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
        }, Platform::runLater).join();
    }

    private void storeLauncherSettingsAfterInit(BaseLauncherSettings launcherSettings) throws LauncherStartFailedException {
        logger.trace("Store LauncherSettings...");
        updateMessage(BundleUtils.getLabel("splash_storeLauncherSettings"));
        try {
            launcherSettings.store();
        } catch (IOException e) {
            logger.error("The launcher settings can not be stored! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_storeSettings"));
            throw new LauncherStartFailedException();
        }
        logger.debug("Launcher Settings stored: {}", launcherSettings);
    }
}
