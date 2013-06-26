/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.launcher.gui.LauncherFrame;
import org.terasology.launcher.gui.SplashScreenWindow;
import org.terasology.launcher.updater.LauncherUpdater;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.version.TerasologyGameVersions;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

public final class TerasologyLauncher {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyLauncher.class);

    private TerasologyLauncher() {
    }

    public static void main(final String[] args) {
        try {
            logger.info("TerasologyLauncher is starting");

            // SplashScreen
            logger.trace("Create SplashScreenWindow...");
            final SplashScreenWindow splash = new SplashScreenWindow(BundleUtils.getBufferedImage("splash"));
            splash.setVisible(true);

            // Java
            logger.debug("Java version and vendor: {} {}", System.getProperty("java.version"),
                System.getProperty("java.vendor"));

            // TerasologyLauncherVersionInfo
            final TerasologyLauncherVersionInfo launcherVersionInfo = TerasologyLauncherVersionInfo.getInstance();
            logger.debug("TerasologyLauncherVersionInfo: {}", launcherVersionInfo.toString());

            // Language
            logger.trace("Init Languages...");
            Languages.init();
            logger.debug("Language: {}", Languages.getCurrentLocale());

            // OS
            logger.trace("Init OperatingSystem...");
            final OperatingSystem os = OperatingSystem.getOS();
            if (os == OperatingSystem.UNKNOWN) {
                logger.error("The operating system is not supported! '{}' '{}'", System.getProperty("os.name"),
                    System.getProperty("os.arch"));
                JOptionPane.showMessageDialog(splash,
                    BundleUtils.getLabel("message_error_operatingSystem"),
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Operating system: {} {} ({})", System.getProperty("os.name"), System.getProperty("os.arch"),
                os);

            // Launcher directory
            logger.trace("Init launcherDirectory...");
            final File launcherDirectory = DirectoryUtils.getApplicationDirectory(os,
                DirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
            try {
                DirectoryUtils.checkDirectory(launcherDirectory);
            } catch (IOException e) {
                logger.error("The launcher directory can not be created or used! '{}'", launcherDirectory, e);
                JOptionPane.showMessageDialog(splash,
                    BundleUtils.getLabel("message_error_launcherDirectory") + "\n" + launcherDirectory,
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Launcher directory: {}", launcherDirectory);

            // Download directory
            logger.trace("Init downloadDirectory...");
            final File downloadDirectory = new File(launcherDirectory, DirectoryUtils.DOWNLOAD_DIR_NAME);
            try {
                DirectoryUtils.checkDirectory(downloadDirectory);
                FileUtils.deleteDirectoryContent(downloadDirectory);
            } catch (IOException e) {
                logger.error("The download directory can not be created or used! '{}'", downloadDirectory, e);
                JOptionPane.showMessageDialog(splash,
                    BundleUtils.getLabel("message_error_downloadDirectory") + "\n" + downloadDirectory,
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Download directory: {}", downloadDirectory);

            // LauncherSettings
            logger.trace("Init LauncherSettings...");
            final LauncherSettings launcherSettings = new LauncherSettings(launcherDirectory);
            try {
                launcherSettings.load();
                launcherSettings.init();
            } catch (IOException e) {
                logger.error("The launcher settings can not be loaded or initialized! '{}'",
                    launcherSettings.getLauncherSettingsFilePath(), e);
                JOptionPane.showMessageDialog(splash,
                    BundleUtils.getLabel("message_error_loadSettings"),
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("LauncherSettings: {}", launcherSettings);

            // Launcher Update
            if (launcherSettings.isSearchForLauncherUpdates()) {
                logger.trace("Search for launcher updates...");
                splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateCheck"));
                final LauncherUpdater updater = new LauncherUpdater(os, downloadDirectory,
                    launcherVersionInfo.getBuildNumber(), launcherVersionInfo.getJobName());
                if (updater.updateAvailable()) {
                    logger.info("An update is available to the TerasologyLauncher. '{}' '{}'",
                        updater.getUpstreamVersion(), updater.getVersionInfo());
                    splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateAvailable"));

                    showUpdateDialog(splash, updater, launcherVersionInfo);
                }
            }

            // Games directory
            logger.trace("Init gamesDirectory...");
            File gamesDirectory = launcherSettings.getGamesDirectory();
            if (gamesDirectory != null) {
                try {
                    DirectoryUtils.checkDirectory(gamesDirectory);
                } catch (IOException e) {
                    logger.warn("The game installation directory can not be created or used! '{}'", gamesDirectory, e);
                    JOptionPane.showMessageDialog(splash,
                        BundleUtils.getLabel("message_error_gamesDirectory") + "\n" + gamesDirectory,
                        BundleUtils.getLabel("message_error_title"),
                        JOptionPane.WARNING_MESSAGE);

                    // Set gamesDirectory to 'null' -> user has to choose new games directory
                    gamesDirectory = null;

                    splash.setVisible(true);
                }
            }
            if (gamesDirectory == null) {
                logger.trace("Choose gamesDirectory...");
                splash.getInfoLabel().setText(BundleUtils.getLabel("splash_chooseGamesDirectory"));
                gamesDirectory = DirectoryUtils.getApplicationDirectory(os, DirectoryUtils.GAMES_APPLICATION_DIR_NAME);
                final JFileChooser fileChooser = new JFileChooser(gamesDirectory.getParentFile());
                // Cannot use mode DIRECTORIES_ONLY, because the preselected name doesn't work.
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooser.setSelectedFile(gamesDirectory);
                fileChooser.setDialogTitle(BundleUtils.getLabel("message_dialog_title_chooseGamesDirectory"));
                if (fileChooser.showSaveDialog(splash) != JFileChooser.APPROVE_OPTION) {
                    logger.info("The new game installation directory is not approved. " +
                        "The TerasologyLauncher is terminated.");
                    System.exit(0);
                }

                gamesDirectory = fileChooser.getSelectedFile();

                splash.setVisible(true);
            }
            try {
                DirectoryUtils.checkDirectory(gamesDirectory);
                launcherSettings.setGamesDirectory(gamesDirectory);
            } catch (IOException e) {
                logger.error("The game installation directory can not be created or used! '{}'", gamesDirectory, e);
                JOptionPane.showMessageDialog(splash,
                    BundleUtils.getLabel("message_error_gamesDirectory") + "\n" + gamesDirectory,
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Games directory: {}", gamesDirectory);

            // Game versions
            logger.trace("Load game versions...");
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_loadGameVersions"));
            final TerasologyGameVersions gameVersions = new TerasologyGameVersions();
            gameVersions.loadGameVersions(launcherSettings, launcherDirectory, gamesDirectory);
            gameVersions.fixSettingsBuildVersion(launcherSettings);
            logger.debug("Game versions: {}", gameVersions);

            // Store LauncherSettings (after 'Games directory' and after 'Game versions')
            logger.trace("Store LauncherSettings...");
            try {
                launcherSettings.store();
            } catch (IOException e) {
                logger.error("The launcher settings can not be stored! '{}'",
                    launcherSettings.getLauncherSettingsFilePath(), e);
                JOptionPane.showMessageDialog(splash,
                    BundleUtils.getLabel("message_error_storeSettings"),
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("LauncherSettings saved successfully: {}", launcherSettings);

            // LauncherFrame
            logger.trace("Create LauncherFrame...");
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_createFrame"));
            final Frame frame = new LauncherFrame(launcherDirectory, downloadDirectory, launcherSettings, gameVersions);
            frame.setVisible(true);

            // Dispose splash screen
            logger.trace("Dispose SplashScreen...");
            splash.setVisible(false);
            splash.dispose();

            logger.info("The TerasologyLauncher was successfully started.");
        } catch (Exception e) {
            logger.error("The TerasologyLauncher could not be started.!", e);
            JOptionPane.showMessageDialog(null,
                BundleUtils.getLabel("message_error_launcherStart"),
                BundleUtils.getLabel("message_error_title"),
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static void showUpdateDialog(final SplashScreenWindow splash, final LauncherUpdater updater,
                                         final TerasologyLauncherVersionInfo launcherVersionInfo) {
        final Object[] options = {BundleUtils.getLabel("main_yes"), BundleUtils.getLabel("main_no")};

        final JPanel msgPanel = new JPanel(new BorderLayout());
        final JTextArea msgLabel = new JTextArea(BundleUtils.getLabel("message_update_launcher"));
        msgLabel.setBackground(msgPanel.getBackground());
        msgLabel.setEditable(false);

        final StringBuilder builder = new StringBuilder();
        builder.append("  ");
        builder.append(BundleUtils.getLabel("message_update_current"));
        builder.append(launcherVersionInfo.getDisplayVersion());
        builder.append("\n");
        builder.append("  ");
        builder.append(BundleUtils.getLabel("message_update_latest"));
        if (updater.getVersionInfo() != null) {
            builder.append(updater.getVersionInfo().getDisplayVersion());
        } else if (updater.getUpstreamVersion() != null) {
            builder.append(updater.getUpstreamVersion());
        }

        final JTextArea msgArea = new JTextArea();
        msgArea.setText(builder.toString());
        msgArea.setOpaque(false);
        msgArea.setEditable(false);
        msgArea.setBackground(msgPanel.getBackground());
        msgArea.setCaretPosition(msgArea.getText().length());
        msgArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        msgPanel.add(msgLabel, BorderLayout.PAGE_START);
        msgPanel.add(msgArea, BorderLayout.CENTER);

        final int option = JOptionPane.showOptionDialog(splash,
            msgPanel,
            BundleUtils.getLabel("message_update_launcher_title"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        splash.setVisible(true);

        if (option == 0) {
            logger.trace("Updating TerasologyLauncher...");
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_updatingLauncher"));
            updater.update(splash);
        }
    }
}
