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

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.updater.LauncherUpdater;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class LauncherInitTask extends Task<LauncherConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(LauncherInitTask.class);

    private final Stage owner;

    public LauncherInitTask(final Stage newOwner) {
        this.owner = newOwner;
    }

    /**
     * Assembles a {@link LauncherConfiguration}.
     *
     * @return a complete launcher configuration or {@code null} if the initialization failed
     */
    @Override
    protected LauncherConfiguration call() {
        try {
            // get OS info
            final OperatingSystem os = getOperatingSystem();

            // init directories
            updateMessage(BundleUtils.getLabel("splash_initLauncherDirs"));
            final File launcherDirectory = getLauncherDirectory(os);
            final File downloadDirectory = getDownloadDirectory(launcherDirectory);
            final File tempDirectory = getTempDirectory(launcherDirectory);

            // launcher settings
            final BaseLauncherSettings launcherSettings = getLauncherSettings(launcherDirectory);

            if (launcherSettings.isSearchForLauncherUpdates()) {
                final boolean selfUpdaterStarted = checkForLauncherUpdates(downloadDirectory, tempDirectory, launcherSettings.isKeepDownloadedFiles());
                if (selfUpdaterStarted) {
                    logger.info("Exit old TerasologyLauncher: {}", TerasologyLauncherVersionInfo.getInstance());
                    return NullLauncherConfiguration.getInstance();
                }
            }

            // game directories
            updateMessage(BundleUtils.getLabel("splash_initGameDirs"));
            final File gameDirectory = getGameDirectory(os, launcherSettings.getGameDirectory());
            final File gameDataDirectory = getGameDataDirectory(os, launcherSettings.getGameDataDirectory());

            final TerasologyGameVersions gameVersions = getTerasologyGameVersions(launcherDirectory, gameDirectory, launcherSettings);

            logger.trace("Change LauncherSettings...");
            launcherSettings.setGameDirectory(gameDirectory);
            launcherSettings.setGameDataDirectory(gameDataDirectory);
            gameVersions.fixSettingsBuildVersion(launcherSettings);

            storeLauncherSettingsAfterInit(launcherSettings);

            logger.trace("Creating launcher frame...");

            return new LauncherConfiguration(launcherDirectory, downloadDirectory, tempDirectory, launcherSettings, gameVersions);
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

    private File getLauncherDirectory(OperatingSystem os) throws LauncherStartFailedException {
        logger.trace("Init LauncherDirectory...");
        final File launcherDirectory = DirectoryUtils.getApplicationDirectory(os, DirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
        try {
            DirectoryUtils.checkDirectory(launcherDirectory);
        } catch (IOException e) {
            logger.error("The launcher directory can not be created or used! '{}'", launcherDirectory, e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_launcherDirectory") + "\n" + launcherDirectory);
            throw new LauncherStartFailedException();
        }
        logger.debug("Launcher directory: {}", launcherDirectory);
        return launcherDirectory;
    }

    private File getDownloadDirectory(File launcherDirectory) throws LauncherStartFailedException {
        logger.trace("Init DownloadDirectory...");
        final File downloadDirectory = new File(launcherDirectory, DirectoryUtils.DOWNLOAD_DIR_NAME);
        try {
            DirectoryUtils.checkDirectory(downloadDirectory);
        } catch (IOException e) {
            logger.error("The download directory can not be created or used! '{}'", downloadDirectory, e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_downloadDirectory") + "\n" + downloadDirectory);
            throw new LauncherStartFailedException();
        }
        logger.debug("Download directory: {}", downloadDirectory);
        return downloadDirectory;
    }

    private File getTempDirectory(File launcherDirectory) throws LauncherStartFailedException {
        logger.trace("Init TempDirectory...");
        final File tempDirectory = new File(launcherDirectory, DirectoryUtils.TEMP_DIR_NAME);
        try {
            DirectoryUtils.checkDirectory(tempDirectory);
        } catch (IOException e) {
            logger.error("The temp directory can not be created or used! '{}'", tempDirectory, e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_tempDirectory") + "\n" + tempDirectory);
            throw new LauncherStartFailedException();
        }
        try {
            FileUtils.deleteDirectoryContent(tempDirectory);
        } catch (IOException e) {
            logger.warn("The content of the temp directory can not be deleted! '{}'", tempDirectory, e);
        }
        logger.debug("Temp directory: {}", tempDirectory);
        return tempDirectory;
    }

    private BaseLauncherSettings getLauncherSettings(File launcherDirectory) throws LauncherStartFailedException {
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

    private boolean checkForLauncherUpdates(File downloadDirectory, File tempDirectory, boolean saveDownloadedFiles) {
        logger.trace("Check for launcher updates...");
        boolean selfUpdaterStarted = false;
        updateMessage(BundleUtils.getLabel("splash_launcherUpdateCheck"));
        final LauncherUpdater updater = new LauncherUpdater(TerasologyLauncherVersionInfo.getInstance());
        if (updater.updateAvailable()) {
            logger.trace("Launcher update available!");
            updateMessage(BundleUtils.getLabel("splash_launcherUpdateAvailable"));
            boolean foundLauncherInstallationDirectory = false;
            try {
                updater.detectAndCheckLauncherInstallationDirectory();
                foundLauncherInstallationDirectory = true;
            } catch (URISyntaxException | IOException e) {
                logger.error("The launcher installation directory can not be detected or used!", e);
                GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_launcherInstallationDirectory"));
                // Run launcher without an update. Don't throw a LauncherStartFailedException.
            }
            if (foundLauncherInstallationDirectory) {
                final boolean update = updater.showUpdateDialog(owner);
                if (update) {
                    if (saveDownloadedFiles) {
                        selfUpdaterStarted = updater.update(downloadDirectory, tempDirectory);
                    } else {
                        selfUpdaterStarted = updater.update(tempDirectory, tempDirectory);
                    }
                }
            }
        }
        return selfUpdaterStarted;
    }

    private File getGameDirectory(OperatingSystem os, File settingsGameDirectory) throws LauncherStartFailedException {
        logger.trace("Init GameDirectory...");
        File gameDirectory = settingsGameDirectory;
        if (gameDirectory != null) {
            try {
                DirectoryUtils.checkDirectory(gameDirectory);
            } catch (IOException e) {
                logger.warn("The game directory can not be created or used! '{}'", gameDirectory, e);
                GuiUtils.showWarningMessageDialog(owner, BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory);

                // Set gameDirectory to 'null' -> user has to choose new game directory
                gameDirectory = null;
            }
        }
        if (gameDirectory == null) {
            logger.trace("Choose installation directory for the game...");
            updateMessage(BundleUtils.getLabel("splash_chooseGameDirectory"));
            gameDirectory = GuiUtils.chooseDirectoryDialog(owner, DirectoryUtils.getApplicationDirectory(os, DirectoryUtils.GAME_APPLICATION_DIR_NAME),
                BundleUtils.getLabel("message_dialog_title_chooseGameDirectory"));
            if (gameDirectory == null) {
                logger.info("The new game directory is not approved. The TerasologyLauncher is terminated.");
                Platform.exit();
            }
        }
        try {
            DirectoryUtils.checkDirectory(gameDirectory);
        } catch (IOException e) {
            logger.error("The game directory can not be created or used! '{}'", gameDirectory, e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory);
            throw new LauncherStartFailedException();
        }
        logger.debug("Game directory: {}", gameDirectory);
        return gameDirectory;
    }

    private File getGameDataDirectory(OperatingSystem os, File settingsGameDataDirectory) throws LauncherStartFailedException {
        logger.trace("Init GameDataDirectory...");
        File gameDataDirectory = settingsGameDataDirectory;
        if (gameDataDirectory != null) {
            try {
                DirectoryUtils.checkDirectory(gameDataDirectory);
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
            gameDataDirectory = GuiUtils.chooseDirectoryDialog(owner, DirectoryUtils.getGameDataDirectory(os),
                BundleUtils.getLabel("message_dialog_title_chooseGameDataDirectory"));
            if (gameDataDirectory == null) {
                logger.info("The new game data directory is not approved. The TerasologyLauncher is terminated.");
                Platform.exit();
            }
        }
        try {
            DirectoryUtils.checkDirectory(gameDataDirectory);
        } catch (IOException e) {
            logger.error("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
            GuiUtils.showErrorMessageDialog(owner, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);
            throw new LauncherStartFailedException();
        }
        logger.debug("Game data directory: {}", gameDataDirectory);
        return gameDataDirectory;
    }

    private TerasologyGameVersions getTerasologyGameVersions(File launcherDirectory, File gameDirectory, BaseLauncherSettings launcherSettings) {
        logger.trace("Init TerasologyGameVersions...");
        updateMessage(BundleUtils.getLabel("splash_loadGameVersions"));
        final TerasologyGameVersions gameVersions = new TerasologyGameVersions();
        gameVersions.loadGameVersions(launcherSettings, launcherDirectory, gameDirectory);
        if (logger.isInfoEnabled()) {
            for (GameJob gameJob : GameJob.values()) {
                logger.info("Game versions: {} {}", gameJob, gameVersions.getGameVersionList(gameJob).size() - 1);
            }
        }
        logger.debug("Game versions: {}", gameVersions);
        return gameVersions;
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
