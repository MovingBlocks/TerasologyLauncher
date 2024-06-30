// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher;

import javafx.concurrent.Task;
import javafx.stage.Stage;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GHRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameManager;
import org.terasology.launcher.model.LauncherVersion;
import org.terasology.launcher.platform.UnsupportedPlatformException;
import org.terasology.launcher.repositories.CombinedRepository;
import org.terasology.launcher.settings.LauncherSettingsValidator;
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.ui.Dialogs;
import org.terasology.launcher.updater.LauncherUpdater;
import org.terasology.launcher.util.I18N;
import org.terasology.launcher.util.DirectoryCreator;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.HostServices;
import org.terasology.launcher.util.LauncherDirectoryUtils;
import org.terasology.launcher.util.LauncherManagedDirectory;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.platform.Platform;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
            updateMessage(I18N.getLabel("splash_initLauncherDirs"));
            final Path installationDirectory = LauncherDirectoryUtils.getInstallationDirectory();
            final Path userDataDirectory = getLauncherDirectory(platform);

            final Path downloadDirectory = getDirectoryFor(LauncherManagedDirectory.DOWNLOAD, userDataDirectory);
            final Path tempDirectory = getDirectoryFor(LauncherManagedDirectory.TEMP, userDataDirectory);
            final Path cacheDirectory = getDirectoryFor(LauncherManagedDirectory.CACHE, userDataDirectory);

            // launcher settings
            final Settings launcherSettings = getLauncherSettings(userDataDirectory);
            // By default, we initialize the launcher with the host system's default locale (if supported).
            // The user may have chosen a different locale in the launcher settings, so apply that as soon as possible.
            I18N.setLocale(launcherSettings.locale.get());

            // validate the settings
            LauncherSettingsValidator.validate(launcherSettings);

            // looking for launcher updates (first network communication)
            final var client = new OkHttpClient.Builder()
                    .cache(new Cache(cacheDirectory.toFile(), 10L * 1024L * 1024L /*10 MiB*/))
                    .callTimeout(10, TimeUnit.SECONDS)
                    .build();
            checkForLauncherUpdates(downloadDirectory, tempDirectory, launcherSettings.keepDownloadedFiles.get());

            // game directories
            updateMessage(I18N.getLabel("splash_initGameDirs"));
            final Path gameDirectory = getDirectoryFor(LauncherManagedDirectory.GAMES, installationDirectory);
            final Path gameDataDirectory = getGameDataDirectory(platform, launcherSettings.gameDataDirectory.get());

            updateMessage(I18N.getLabel("splash_fetchReleases"));
            logger.info("Fetching game releases ...");
            // implicitly fetches game releases and cache them
            final CombinedRepository releaseRepository = new CombinedRepository(client);

            // implicitly scans the game directory for installed games and cache them
            final GameManager gameManager = new GameManager(cacheDirectory, gameDirectory);

            logger.trace("Change LauncherSettings...");
            launcherSettings.gameDirectory.set(gameDirectory);
            launcherSettings.gameDataDirectory.set(gameDataDirectory);
            // TODO: Rewrite gameVersions.fixSettingsBuildVersion(launcherSettings);

            storeLauncherSettingsAfterInit(launcherSettings, userDataDirectory);

            logger.trace("Creating launcher frame...");

            return new LauncherConfiguration(
                    userDataDirectory,
                    downloadDirectory,
                    launcherSettings,
                    gameManager,
                    releaseRepository);
        } catch (LauncherStartFailedException e) {
            logger.warn("Could not configure launcher.");
        } catch (UnsupportedPlatformException e) {
            logger.error("Unsupported OS or architecture: {}", e.getMessage());
        }

        return null;
    }

    private Platform getPlatform() throws UnsupportedPlatformException {
        logger.trace("Init Platform...");
        updateMessage(I18N.getLabel("splash_checkOS"));
        final Platform platform = Platform.getPlatform();
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
            Dialogs.showError(owner, I18N.getLabel(errorLabel) + "\n" + dir);
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

    private Settings getLauncherSettings(Path settingsPath) throws LauncherStartFailedException {
        logger.trace("Init LauncherSettings...");
        updateMessage(I18N.getLabel("splash_retrieveLauncherSettings"));

        final Settings settings = Optional.ofNullable(Settings.load(settingsPath)).orElse(Settings.getDefault());

        logger.debug("Launcher Settings: {}", settings);

        return settings;
    }

    private void checkForLauncherUpdates(Path downloadDirectory, Path tempDirectory, boolean saveDownloadedFiles) {
        logger.trace("Check for launcher updates...");
        updateMessage(I18N.getLabel("splash_launcherUpdateCheck"));
        final LauncherUpdater updater = new LauncherUpdater(LauncherVersion.getInstance());
        final GHRelease release = updater.updateAvailable();
        if (release != null) {
            logger.info("Launcher update available: {}", release.getTagName());
            updateMessage(I18N.getLabel("splash_launcherUpdateAvailable"));
            boolean foundLauncherInstallationDirectory = false;
            try {
                final Path installationDir = LauncherDirectoryUtils.getInstallationDirectory();
                FileUtils.ensureWritableDir(installationDir);
                logger.trace("Launcher installation directory: {}", installationDir);
                foundLauncherInstallationDirectory = true;
            } catch (IOException e) {
                logger.error("The launcher installation directory can not be detected or used!", e);
                Dialogs.showError(owner, I18N.getLabel("message_error_launcherInstallationDirectory"));
                // Run launcher without an update. Don't throw a LauncherStartFailedException.
            }
            if (foundLauncherInstallationDirectory) {
                final boolean update = updater.showUpdateDialog(owner, release);
                if (update) {
                    showDownloadPage();
                }
            }
        }
    }

    private void showDownloadPage() {
        final String downloadPage = "https://terasology.org/downloads/";
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
                Dialogs.showWarning(owner, I18N.getLabel("message_error_gameDataDirectory") + "\n"
                        + gameDataDirectory);

                // Set gameDataDirectory to 'null' -> user has to choose new game data directory
                gameDataDirectory = null;
            }
        }
        if (gameDataDirectory == null) {
            logger.trace("Choose data directory for the game...");
            updateMessage(I18N.getLabel("splash_chooseGameDataDirectory"));
            gameDataDirectory = Dialogs.chooseDirectory(owner, LauncherDirectoryUtils.getGameDataDirectory(os),
                    I18N.getLabel("message_dialog_title_chooseGameDataDirectory"));
            if (Files.notExists(gameDataDirectory)) {
                logger.info("The new game data directory is not approved. The TerasologyLauncher is terminated.");
                javafx.application.Platform.exit();
            }
        }
        try {
            FileUtils.ensureWritableDir(gameDataDirectory);
        } catch (IOException e) {
            logger.error("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
            Dialogs.showError(owner, I18N.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);
            throw new LauncherStartFailedException();
        }
        logger.debug("Game data directory: {}", gameDataDirectory);
        return gameDataDirectory;
    }

    private void storeLauncherSettingsAfterInit(Settings launcherSettings, final Path settingsPath) throws LauncherStartFailedException {
        logger.trace("Store LauncherSettings...");
        updateMessage(I18N.getLabel("splash_storeLauncherSettings"));
        try {
            Settings.store(launcherSettings, settingsPath);
        } catch (IOException e) {
            logger.error("The launcher settings cannot be stored to '{}'.", settingsPath, e);
            Dialogs.showError(owner, I18N.getLabel("message_error_storeSettings"));
            //TODO: should we fail here, or is it fine to work with in-memory settings?
            throw new LauncherStartFailedException();
        }
        logger.debug("Launcher Settings stored: {}", launcherSettings);
    }
}
