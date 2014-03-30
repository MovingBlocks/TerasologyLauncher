/*
 * Copyright 2013 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.gui.GuiUtils;
import org.terasology.launcher.gui.LauncherFrame;
import org.terasology.launcher.gui.SplashProgressIndicator;
import org.terasology.launcher.gui.SplashScreenWindow;
import org.terasology.launcher.updater.LauncherUpdater;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public final class TerasologyLauncher {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyLauncher.class);

    private TerasologyLauncher() {
    }

    public static void main(String[] args) {
        try {
            logger.info("TerasologyLauncher is starting");

            // Java
            logger.debug("Java: {} {} {}", System.getProperty("java.version"), System.getProperty("java.vendor"), System.getProperty("java.home"));
            logger.debug("Java VM: {} {} {}", System.getProperty("java.vm.name"), System.getProperty("java.vm.vendor"), System.getProperty("java.vm.version"));
            logger.debug("Java classpath: {}", System.getProperty("java.class.path"));

            // OS
            logger.debug("OS: {} {} {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));

            //Memory
            logger.debug("Max. Memory: {} bytes", Runtime.getRuntime().maxMemory());

            // TerasologyLauncherVersionInfo
            final TerasologyLauncherVersionInfo launcherVersionInfo = TerasologyLauncherVersionInfo.getInstance();
            logger.debug("TerasologyLauncherVersionInfo: {}", launcherVersionInfo.toString());

            // Language
            logger.trace("Init Languages...");
            Languages.init();
            logger.debug("Language: {}", Languages.getCurrentLocale());

            // SplashScreen
            logger.trace("Create SplashScreenWindow...");
            final SplashScreenWindow splash = new SplashScreenWindow(BundleUtils.getBufferedImage("splash"));
            splash.setVisible(true);

            // OS
            logger.trace("Init OperatingSystem...");
            final OperatingSystem os = OperatingSystem.getOS();
            if (os == OperatingSystem.UNKNOWN) {
                logger.error("The operating system is not supported! '{}' '{}'", System.getProperty("os.name"), System.getProperty("os.arch"));
                GuiUtils.showErrorMessageDialog(true, splash, BundleUtils.getLabel("message_error_operatingSystem"));
            }
            logger.debug("Operating system: {}", os);

            // Launcher directory
            logger.trace("Init launcherDirectory...");
            final File launcherDirectory = DirectoryUtils.getApplicationDirectory(os, DirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
            try {
                DirectoryUtils.checkDirectory(launcherDirectory);
            } catch (IOException e) {
                logger.error("The launcher directory can not be created or used! '{}'", launcherDirectory, e);
                GuiUtils.showErrorMessageDialog(true, splash, BundleUtils.getLabel("message_error_launcherDirectory") + "\n" + launcherDirectory);
            }
            logger.debug("Launcher directory: {}", launcherDirectory);

            // Temp directory
            logger.trace("Init tempDirectory...");
            final File tempDirectory = new File(launcherDirectory, DirectoryUtils.TEMP_DIR_NAME);
            try {
                DirectoryUtils.checkDirectory(tempDirectory);
            } catch (IOException e) {
                logger.error("The temp directory can not be created or used! '{}'", tempDirectory, e);
                GuiUtils.showErrorMessageDialog(true, splash, BundleUtils.getLabel("message_error_tempDirectory") + "\n" + tempDirectory);
            }
            try {
                FileUtils.deleteDirectoryContent(tempDirectory);
            } catch (IOException e) {
                logger.warn("The content of the temp directory can not be deleted! '{}'", tempDirectory, e);
            }
            logger.debug("Temp directory: {}", tempDirectory);

            // LauncherSettings
            logger.trace("Init LauncherSettings...");
            final LauncherSettings launcherSettings = new LauncherSettings(launcherDirectory);
            try {
                launcherSettings.load();
                launcherSettings.init();
            } catch (IOException e) {
                logger.error("The launcher settings can not be loaded or initialized! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
                GuiUtils.showErrorMessageDialog(true, splash, BundleUtils.getLabel("message_error_loadSettings"));
            }
            logger.debug("LauncherSettings: {}", launcherSettings);

            // Launcher Update
            if (launcherSettings.isSearchForLauncherUpdates()) {
                logger.trace("Check for launcher updates...");
                splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateCheck"));
                final LauncherUpdater updater = new LauncherUpdater(launcherVersionInfo);
                if (updater.updateAvailable()) {
                    logger.trace("Launcher update available!");
                    splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateAvailable"));
                    try {
                        updater.detectAndCheckLauncherInstallationDirectory();
                        final boolean update = updater.showUpdateDialog(splash);
                        if (update) {
                            splash.setVisible(true);
                            final boolean selfUpdaterStarted = updater.update(tempDirectory, splash);
                            if (selfUpdaterStarted) {
                                logger.info("Exit old launcher: {}", TerasologyLauncherVersionInfo.getInstance());
                                System.exit(0);
                            }
                        }
                    } catch (URISyntaxException | IOException e) {
                        logger.error("The launcher installation directory can not be detected or used!", e);
                        GuiUtils.showErrorMessageDialog(false, splash, BundleUtils.getLabel("message_error_launcherInstallationDirectory"));
                    }
                    splash.setVisible(true);
                }
            }

            // Game directory
            logger.trace("Init gameDirectory...");
            File gameDirectory = launcherSettings.getGameDirectory();
            if (gameDirectory != null) {
                try {
                    DirectoryUtils.checkDirectory(gameDirectory);
                } catch (IOException e) {
                    logger.warn("The game directory can not be created or used! '{}'", gameDirectory, e);
                    GuiUtils.showWarningMessageDialog(splash, BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory);

                    // Set gameDirectory to 'null' -> user has to choose new game directory
                    gameDirectory = null;

                    splash.setVisible(true);
                }
            }
            if (gameDirectory == null) {
                logger.trace("Choose installation directory for the game...");
                splash.getInfoLabel().setText(BundleUtils.getLabel("splash_chooseGameDirectory"));
                gameDirectory = GuiUtils.chooseDirectory(splash, DirectoryUtils.getApplicationDirectory(os, DirectoryUtils.GAME_APPLICATION_DIR_NAME),
                    BundleUtils.getLabel("message_dialog_title_chooseGameDirectory"));
                if (gameDirectory == null) {
                    logger.info("The new game directory is not approved. The TerasologyLauncher is terminated.");
                    System.exit(0);
                }

                splash.setVisible(true);
            }
            try {
                DirectoryUtils.checkDirectory(gameDirectory);
                launcherSettings.setGameDirectory(gameDirectory);
            } catch (IOException e) {
                logger.error("The game directory can not be created or used! '{}'", gameDirectory, e);
                GuiUtils.showErrorMessageDialog(true, splash, BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory);
            }
            logger.debug("Game directory: {}", gameDirectory);

            // Game data directory
            logger.trace("Init gameDataDirectory...");
            File gameDataDirectory = launcherSettings.getGameDataDirectory();
            if (gameDataDirectory != null) {
                try {
                    DirectoryUtils.checkDirectory(gameDataDirectory);
                } catch (IOException e) {
                    logger.warn("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
                    GuiUtils.showWarningMessageDialog(splash, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);

                    // Set gameDataDirectory to 'null' -> user has to choose new game data directory
                    gameDataDirectory = null;

                    splash.setVisible(true);
                }
            }
            if (gameDataDirectory == null) {
                logger.trace("Choose data directory for the game...");
                splash.getInfoLabel().setText(BundleUtils.getLabel("splash_chooseGameDataDirectory"));
                gameDataDirectory = GuiUtils.chooseDirectory(splash, DirectoryUtils.getGameDataDirectory(os),
                    BundleUtils.getLabel("message_dialog_title_chooseGameDataDirectory"));
                if (gameDataDirectory == null) {
                    logger.info("The new game data directory is not approved. The TerasologyLauncher is terminated.");
                    System.exit(0);
                }

                splash.setVisible(true);
            }
            try {
                DirectoryUtils.checkDirectory(gameDataDirectory);
                launcherSettings.setGameDataDirectory(gameDataDirectory);
            } catch (IOException e) {
                logger.error("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
                GuiUtils.showErrorMessageDialog(true, splash, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);
            }
            logger.debug("Game data directory: {}", gameDataDirectory);

            // Game versions
            logger.trace("Loading game versions...");
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_loadGameVersions"));
            final TerasologyGameVersions gameVersions = new TerasologyGameVersions();
            gameVersions.loadGameVersions(launcherSettings, launcherDirectory, gameDirectory, new SplashProgressIndicator(splash, "splash_loadGameVersions"));
            gameVersions.fixSettingsBuildVersion(launcherSettings);
            logger.debug("Game versions: {}", gameVersions);
            if (logger.isInfoEnabled()) {
                for (GameJob gameJob : GameJob.values()) {
                    logger.info("Game versions: {} {}", gameJob, gameVersions.getGameVersionList(gameJob).size() - 1);
                }
            }

            // Store LauncherSettings ('Game directory', 'Game data directory', 'Game versions')
            logger.trace("Store LauncherSettings...");
            try {
                launcherSettings.store();
            } catch (IOException e) {
                logger.error("The launcher settings can not be stored! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
                GuiUtils.showErrorMessageDialog(true, splash, BundleUtils.getLabel("message_error_storeSettings"));
            }
            logger.debug("LauncherSettings saved successfully: {}", launcherSettings);

            // LauncherFrame
            logger.trace("Creating launcher frame...");
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_createFrame"));
            final Frame frame = new LauncherFrame(launcherDirectory, tempDirectory, launcherSettings, gameVersions);
            frame.setVisible(true);

            // Dispose splash screen
            logger.trace("Dispose SplashScreen...");
            splash.setVisible(false);
            splash.dispose();

            logger.info("The TerasologyLauncher was successfully started.");
        } catch (IOException | RuntimeException e) {
            logger.error("The TerasologyLauncher could not be started!", e);
            GuiUtils.showErrorMessageDialog(true, null, BundleUtils.getLabel("message_error_launcherStart"));
        }
    }
}
