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

package org.terasology.launcher.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.Languages;
import org.terasology.launcher.Settings;
import org.terasology.launcher.gui.LauncherFrame;
import org.terasology.launcher.gui.SplashScreenWindow;
import org.terasology.launcher.updater.LauncherUpdater;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
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
            logger.debug("TerasologyLauncherVersionInfo: {}", TerasologyLauncherVersionInfo.getInstance().toString());

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

            // Application directory
            final File applicationDir = DirectoryUtils.getApplicationDirectory(os);
            try {
                DirectoryUtils.checkDirectory(applicationDir);
            } catch (IOException e) {
                logger.error("Cannot create or use application directory '{}'!", applicationDir, e);
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_applicationDirectory") + "\n" + applicationDir,
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Application directory: {}", applicationDir);

            // Launcher directory
            final File launcherDir = new File(applicationDir, DirectoryUtils.LAUNCHER_DIR_NAME);
            try {
                DirectoryUtils.checkDirectory(launcherDir);
            } catch (IOException e) {
                logger.error("Cannot create or use launcher directory '{}'!", launcherDir, e);
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_launcherDirectory") + "\n" + launcherDir,
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Launcher directory: {}", launcherDir);

            // Settings
            final Settings settings = new Settings(launcherDir);
            try {
                settings.load();
                settings.init();
                settings.store();
            } catch (IOException e) {
                logger.error("Cannot load/init/store settings!", e);
                JOptionPane.showMessageDialog(null,
                    BundleUtils.getLabel("message_error_loadSettings"),
                    BundleUtils.getLabel("message_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Settings: {}", settings);

            // Launcher Update
            if (settings.isSearchForLauncherUpdates()) {
                splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateCheck"));
                final LauncherUpdater updater = new LauncherUpdater(os, applicationDir,
                    TerasologyLauncherVersionInfo.getInstance().getBuildNumber(),
                    TerasologyLauncherVersionInfo.getInstance().getJobName());
                if (updater.updateAvailable()) {
                    logger.info("Launcher update available! {} {}", updater.getUpstreamVersion(),
                        updater.getVersionInfo());
                    splash.getInfoLabel().setText(BundleUtils.getLabel("splash_launcherUpdateAvailable"));

                    showUpdateDialog(splash, updater);
                }
            }

            // Game versions
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_loadGameVersions"));
            final TerasologyGameVersions gameVersions = new TerasologyGameVersions(settings);
            gameVersions.loadGameVersions(applicationDir);
            logger.debug("Game versions: {}", gameVersions);

            // LauncherFrame
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_createFrame"));
            final Frame frame = new LauncherFrame(applicationDir, os, settings, gameVersions);
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

    private static void showUpdateDialog(final SplashScreenWindow splash, final LauncherUpdater updater) {
        final Object[] options = {BundleUtils.getLabel("main_yes"), BundleUtils.getLabel("main_no")};

        final JPanel msgPanel = new JPanel(new BorderLayout());
        final JTextArea msgLabel = new JTextArea(BundleUtils.getLabel("message_update_launcher"));
        msgLabel.setBackground(msgPanel.getBackground());
        msgLabel.setEditable(false);

        final StringBuilder builder = new StringBuilder();
        builder.append("  ");
        builder.append(BundleUtils.getLabel("message_update_current"));
        builder.append(TerasologyLauncherVersionInfo.getInstance().getDisplayVersion());
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
            splash.getInfoLabel().setText("Updating the launcher ... please wait.");
            updater.update();
        }
    }

}
