/*
 * Copyright 2014 MovingBlocks
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

import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.gui.GuiUtils;
import org.terasology.launcher.gui.javafx.ApplicationController;
import org.terasology.launcher.updater.LauncherUpdater;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.util.ProgressListener;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public final class TerasologyLauncher extends Application implements ProgressListener {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyLauncher.class);

    @Override
    public void start(final Stage stage) throws Exception {
        logger.info("TerasologyLauncher is starting");

        logSystemInformation();

        initLanguage();

        try {
            // get OS info
            final OperatingSystem os = getOperatingSystem();

            // init directories
            final File launcherDirectory = getLauncherDirectory(os);
            final File downloadDirectory = getDownloadDirectory(launcherDirectory);
            final File tempDirectory = getTempDirectory(launcherDirectory);

            // launcher settings
            final LauncherSettings launcherSettings = getLauncherSettings(launcherDirectory);

            if (launcherSettings.isSearchForLauncherUpdates()) {
                final boolean selfUpdaterStarted = checkForLauncherUpdates(downloadDirectory, tempDirectory, launcherSettings.isSaveDownloadedFiles());
                if (selfUpdaterStarted) {
                    logger.info("Exit old TerasologyLauncher: {}", TerasologyLauncherVersionInfo.getInstance());
                    System.exit(0);
                }
            }

            // game directories
            final File gameDirectory = getGameDirectory(os, launcherSettings.getGameDirectory());
            final File gameDataDirectory = getGameDataDirectory(os, launcherSettings.getGameDataDirectory());

            final TerasologyGameVersions gameVersions = getTerasologyGameVersions(launcherDirectory, gameDirectory, launcherSettings);

            logger.trace("Change LauncherSettings...");
            launcherSettings.setGameDirectory(gameDirectory);
            launcherSettings.setGameDataDirectory(gameDataDirectory);
            gameVersions.fixSettingsBuildVersion(launcherSettings);

            storeLauncherSettingsAfterInit(launcherSettings);

            logger.trace("Creating launcher frame...");
            // TODO JavaFX Preloader
            // splash.getInfoLabel().setText(BundleUtils.getLabel("splash_createFrame"));

            // launcher frame
            final FXMLLoader fxmlLoader = new FXMLLoader(BundleUtils.getFXMLUrl("application"), ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle"));
            final Parent root = (Parent) fxmlLoader.load();
            final ApplicationController controller = fxmlLoader.<ApplicationController>getController();
            controller.initialize(launcherDirectory, downloadDirectory, tempDirectory, launcherSettings, gameVersions);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(BundleUtils.getStylesheet("css_terasology"));

            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            logger.info("The TerasologyLauncher was successfully started.");
        } catch (LauncherStartFailedException e) {
            logger.error("The TerasologyLauncher could not be started!");
            System.exit(1);
        } catch (RuntimeException | Error e) {
            logger.error("The TerasologyLauncher could not be started!", e);

            Path logFile = Paths.get("TerasologyLauncher.log");
            CrashReporter.report(e, logFile);
            System.exit(1);
        }
    }

    private void logSystemInformation() {
        if (logger.isDebugEnabled()) {
            // Java
            logger.debug("Java: {} {} {}", System.getProperty("java.version"), System.getProperty("java.vendor"), System.getProperty("java.home"));
            logger.debug("Java VM: {} {} {}", System.getProperty("java.vm.name"), System.getProperty("java.vm.vendor"), System.getProperty("java.vm.version"));
            logger.debug("Java classpath: {}", System.getProperty("java.class.path"));

            // OS
            logger.debug("OS: {} {} {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));

            //Memory
            logger.debug("Max. Memory: {} bytes", Runtime.getRuntime().maxMemory());

            // TerasologyLauncherVersionInfo
            logger.debug("Launcher version: {}", TerasologyLauncherVersionInfo.getInstance());
        }
    }

    private void initLanguage() {
        logger.trace("Init Languages...");
        Languages.init();
        logger.debug("Language: {}", Languages.getCurrentLocale());
    }

    private OperatingSystem getOperatingSystem() throws LauncherStartFailedException {
        logger.trace("Init OperatingSystem...");
        final OperatingSystem os = OperatingSystem.getOS();
        if (os == OperatingSystem.UNKNOWN) {
            logger.error("The operating system is not supported! '{}' '{}' '{}'", System.getProperty("os.name"), System.getProperty("os.arch"),
                System.getProperty("os.version"));
            // TODO: JavaFX Dialog
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_operatingSystem"));
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
            // TODO: JavaFX Dialog
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_launcherDirectory") + "\n" + launcherDirectory);
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
            // TODO: JavaFX Dialog
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_downloadDirectory") + "\n" + downloadDirectory);
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
            // TODO: JavaFX Dialog
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_tempDirectory") + "\n" + tempDirectory);
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

    private LauncherSettings getLauncherSettings(File launcherDirectory) throws LauncherStartFailedException {
        logger.trace("Init LauncherSettings...");
        final LauncherSettings launcherSettings = new LauncherSettings(launcherDirectory);
        try {
            launcherSettings.load();
            launcherSettings.init();
        } catch (IOException e) {
            logger.error("The launcher settings can not be loaded or initialized! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
            // TODO: JavaFX Dialog
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_loadSettings") + "\n" + launcherSettings.getLauncherSettingsFilePath());
            throw new LauncherStartFailedException();
        }
        logger.debug("Launcher Settings: {}", launcherSettings);
        return launcherSettings;
    }

    private boolean checkForLauncherUpdates(File downloadDirectory, File tempDirectory, boolean saveDownloadedFiles) {
        logger.trace("Check for launcher updates...");
        boolean selfUpdaterStarted = false;
        // TODO splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateCheck"));
        final LauncherUpdater updater = new LauncherUpdater(TerasologyLauncherVersionInfo.getInstance());
        if (updater.updateAvailable()) {
            logger.trace("Launcher update available!");
            // TODO splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateAvailable"));
            boolean foundLauncherInstallationDirectory = false;
            try {
                updater.detectAndCheckLauncherInstallationDirectory();
                foundLauncherInstallationDirectory = true;
            } catch (URISyntaxException | IOException e) {
                logger.error("The launcher installation directory can not be detected or used!", e);
                GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_launcherInstallationDirectory"));
                // Run launcher without an update. Don't throw a LauncherStartFailedException.
            }
            if (foundLauncherInstallationDirectory) {
                final boolean update = updater.showUpdateDialog(null);
                if (update) {
                    // TODO splash.setVisible(true);
                    if (saveDownloadedFiles) {
                        selfUpdaterStarted = updater.update(downloadDirectory, tempDirectory, null);
                    } else {
                        selfUpdaterStarted = updater.update(tempDirectory, tempDirectory, null);
                    }
                }
            }
            // TODO splash screen
            // splash.setVisible(true);
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
                // TODO JavaFX Dialog
                GuiUtils.showWarningMessageDialog(null, BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory);

                // Set gameDirectory to 'null' -> user has to choose new game directory
                gameDirectory = null;
                // TODO JavaFX Preloader
                // splash.setVisible(true);
            }
        }
        if (gameDirectory == null) {
            logger.trace("Choose installation directory for the game...");
            // TODO JavaFX Preloader
            // splash.getInfoLabel().setText(BundleUtils.getLabel("splash_chooseGameDirectory"));
            // TODO remove Swing Component
            gameDirectory = GuiUtils.chooseDirectory(null, DirectoryUtils.getApplicationDirectory(os, DirectoryUtils.GAME_APPLICATION_DIR_NAME),
                BundleUtils.getLabel("message_dialog_title_chooseGameDirectory"));
            if (gameDirectory == null) {
                logger.info("The new game directory is not approved. The TerasologyLauncher is terminated.");
                throw new LauncherStartFailedException();
            }
            // splash.setVisible(true);
        }
        try {
            DirectoryUtils.checkDirectory(gameDirectory);
        } catch (IOException e) {
            logger.error("The game directory can not be created or used! '{}'", gameDirectory, e);
            // TODO JavaFX Dialog
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory);
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
                // TODO JavaFX Dialog
                GuiUtils.showWarningMessageDialog(null, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);

                // Set gameDataDirectory to 'null' -> user has to choose new game data directory
                gameDataDirectory = null;

                // splash.setVisible(true);
            }
        }
        if (gameDataDirectory == null) {
            logger.trace("Choose data directory for the game...");
            // TODO JavaFX Preloader
            // splash.getInfoLabel().setText(BundleUtils.getLabel("splash_chooseGameDataDirectory"));
            gameDataDirectory = GuiUtils.chooseDirectory(null, DirectoryUtils.getGameDataDirectory(os),
                BundleUtils.getLabel("message_dialog_title_chooseGameDataDirectory"));
            if (gameDataDirectory == null) {
                logger.info("The new game data directory is not approved. The TerasologyLauncher is terminated.");
                throw new LauncherStartFailedException();
            }

            // splash.setVisible(true);
        }
        try {
            DirectoryUtils.checkDirectory(gameDataDirectory);
        } catch (IOException e) {
            logger.error("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
            // TODO JavaFX Dialog
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);
            throw new LauncherStartFailedException();
        }
        logger.debug("Game data directory: {}", gameDataDirectory);
        return gameDataDirectory;
    }

    private TerasologyGameVersions getTerasologyGameVersions(File launcherDirectory, File gameDirectory, LauncherSettings launcherSettings) {
        logger.trace("Init TerasologyGameVersions...");
        // TODO JavaFX Preloader
        // splash.getInfoLabel().setText(BundleUtils.getLabel("splash_loadGameVersions"));
        final TerasologyGameVersions gameVersions = new TerasologyGameVersions();
        gameVersions.loadGameVersions(launcherSettings, launcherDirectory, gameDirectory, this);
        if (logger.isInfoEnabled()) {
            for (GameJob gameJob : GameJob.values()) {
                logger.info("Game versions: {} {}", gameJob, gameVersions.getGameVersionList(gameJob).size() - 1);
            }
        }
        logger.debug("Game versions: {}", gameVersions);
        return gameVersions;
    }

    /**
     * Store LauncherSettings ('Game directory', 'Game data directory', 'Game versions').
     */
    private static void storeLauncherSettingsAfterInit(LauncherSettings launcherSettings) throws LauncherStartFailedException {
        logger.trace("Store LauncherSettings...");
        try {
            launcherSettings.store();
        } catch (IOException e) {
            logger.error("The launcher settings can not be stored! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
            // TODO JavaFX Dialog
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_storeSettings"));
            throw new LauncherStartFailedException();
        }
        logger.debug("Launcher Settings stored: {}", launcherSettings);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void update() {
        // TODO notify preloader?
    }

    @Override
    public void update(final int progress) {
        // TODO update splash screen when no preloader
        notifyPreloader(new Preloader.ProgressNotification(progress / 100));
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
