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
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.version.TerasologyGameVersions;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

/**
 * @author Skaldarnar
 */
public final class TerasologyLauncher {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyLauncher.class);

    private TerasologyLauncher() {
    }

    public static void main(final String[] args) {
        try {
            logger.debug("Starting TerasologyLauncher");

            // SplashScreen
            final SplashScreenWindow splash = new SplashScreenWindow(BundleUtils.getBufferedImage("splash"));
            splash.setVisible(true);
            logger.debug("Show SplashScreen");

            // TerasologyLauncherVersionInfo
            final TerasologyLauncherVersionInfo launcherVersionInfo = TerasologyLauncherVersionInfo.getInstance();
            logger.debug("TerasologyLauncherVersionInfo: {}", launcherVersionInfo.toString());

            // Language
            Languages.init();
            logger.debug("Language: {}", Languages.getCurrentLocale());

            // OS
            final OperatingSystem os = OperatingSystem.getOS();
            if (os == OperatingSystem.UNKNOWN) {
                logger.error("Unknown/Unsupported operating system!");
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_operatingSystem"),
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Operating system: {}", os);

            // Launcher directory
            final File launcherDirectory = DirectoryUtils.getApplicationDirectory(os,
                DirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
            try {
                DirectoryUtils.checkDirectory(launcherDirectory);
            } catch (IOException e) {
                logger.error("Cannot create or use launcher directory '{}'!", launcherDirectory, e);
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_launcherDirectory") + "\n" + launcherDirectory,
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Launcher directory: {}", launcherDirectory);

            // Download directory
            final File downloadDirectory = new File(launcherDirectory, DirectoryUtils.DOWNLOAD_DIR_NAME);
            try {
                DirectoryUtils.checkDirectory(downloadDirectory);
            } catch (IOException e) {
                logger.error("Cannot create or use download directory '{}'!", downloadDirectory, e);
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_downloadDirectory") + "\n" + downloadDirectory,
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Download directory: {}", downloadDirectory);

            // LauncherSettings
            final LauncherSettings launcherSettings = new LauncherSettings(launcherDirectory);
            try {
                launcherSettings.load();
                launcherSettings.init();
            } catch (IOException e) {
                logger.error("Cannot load and init launcher settings!", e);
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_loadSettings"),
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("LauncherSettings: {}", launcherSettings);

            // Launcher Update
            if (launcherSettings.isSearchForLauncherUpdates()) {
                splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateCheck"));
                final LauncherUpdater updater = new LauncherUpdater(os, downloadDirectory,
                    launcherVersionInfo.getBuildNumber(), launcherVersionInfo.getJobName());
                if (updater.updateAvailable()) {
                    logger.info("Launcher update available! {} {}", updater.getUpstreamVersion(),
                        updater.getVersionInfo());
                    splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateAvailable"));

                    showUpdateDialog(splash, updater, launcherVersionInfo);
                }
            }

            // Games directory
            File gamesDirectory = launcherSettings.getGamesDirectory();
            if (gamesDirectory == null) {
                gamesDirectory = DirectoryUtils.getApplicationDirectory(os, DirectoryUtils.GAMES_APPLICATION_DIR_NAME);
            }
            try {
                DirectoryUtils.checkDirectory(gamesDirectory);
                launcherSettings.setGamesDirectory(gamesDirectory);
            } catch (IOException e) {
                logger.error("Cannot create or use games directory '{}'!", gamesDirectory, e);
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_gamesDirectory") + "\n" + gamesDirectory,
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Games directory: {}", gamesDirectory);

            // Game versions
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_loadGameVersions"));
            final TerasologyGameVersions gameVersions = new TerasologyGameVersions();
            gameVersions.loadGameVersions(launcherSettings, gamesDirectory);
            gameVersions.fixSettingsBuildVersion(launcherSettings);
            logger.debug("Game versions: {}", gameVersions);

            // Store LauncherSettings (after 'Games directory' and after 'Game versions')
            try {
                launcherSettings.store();
            } catch (IOException e) {
                logger.error("Cannot store launcher settings!", e);
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_storeSettings"),
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("LauncherSettings stored: {}", launcherSettings);

            // LauncherFrame
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_createFrame"));
            final Frame frame = new LauncherFrame(downloadDirectory, gamesDirectory, os, launcherSettings,
                gameVersions);
            frame.setVisible(true);

            // Dispose splash screen
            splash.setVisible(false);
            splash.dispose();

            logger.debug("TerasologyLauncher started");
        } catch (Exception e) {
            logger.error("Starting TerasologyLauncher failed!", e);
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

        final int option = JOptionPane.showOptionDialog(null,
            msgPanel,
            BundleUtils.getLabel("message_update_launcher_title"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        splash.setVisible(true);

        if (option == 0) {
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_updatingLauncher"));
            updater.update();
        }
    }
}
