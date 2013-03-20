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

package org.terasologylauncher.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.Languages;
import org.terasologylauncher.Settings;
import org.terasologylauncher.gui.LauncherFrame;
import org.terasologylauncher.gui.SplashScreen;
import org.terasologylauncher.updater.LauncherUpdater;
import org.terasologylauncher.util.BundleUtils;
import org.terasologylauncher.util.DirectoryUtils;
import org.terasologylauncher.util.OperatingSystem;
import org.terasologylauncher.version.TerasologyGameVersion;
import org.terasologylauncher.version.TerasologyLauncherVersion;

import javax.swing.JOptionPane;
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
            final SplashScreen splash = new SplashScreen(BundleUtils.getBufferedImage("splash"));
            splash.setVisible(true);
            logger.debug("Show SplashScreen");

            // TerasologyLauncherVersion
            logger.debug("TerasologyLauncherVersion: {}", TerasologyLauncherVersion.getInstance().toString());

            // Language
            Languages.init();
            logger.debug("Language: {}", Languages.getCurrentLocale());

            // OS
            final OperatingSystem os = OperatingSystem.getOS();
            logger.debug("OS: {}", os);

            if (os == OperatingSystem.UNKNOWN) {
                logger.error("Unknown/Unsupported operating system!");
                // TODO Message and title
                JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // Application directory
            final File applicationDir = DirectoryUtils.getApplicationDirectory(os);
            try {
                DirectoryUtils.checkDirectory(applicationDir);
            } catch (IOException e) {
                logger.error("Can not create or use application directory! " + applicationDir, e);
                // TODO Message and title
                JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Application directory: {}", applicationDir);

            // Launcher directory
            final File launcherDir = new File(applicationDir, DirectoryUtils.LAUNCHER_DIR_NAME);
            try {
                DirectoryUtils.checkDirectory(launcherDir);
            } catch (IOException e) {
                logger.error("Can not create or use launcher directory! " + launcherDir, e);
                // TODO Message and title
                JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
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
                logger.error("Can not load/init/store settings!", e);
                // TODO Message and title
                JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Settings loaded: {}", settings);

            // Launcher Update
            splash.getInfoLabel().setText("Check launcher update...");  //TODO: i18n
            LauncherUpdater updater = new LauncherUpdater(applicationDir,
                TerasologyLauncherVersion.getInstance().getBuildNumber());
            if (updater.updateAvailable()) {
                logger.info("Launcher update available!");
                splash.getInfoLabel().setText("Launcher update available!"); //TODO: i18n
                // TODO: i18n question dialog
                int option = JOptionPane.showConfirmDialog(null,
                    "A launcher update is available.\nWould you like to update the launcher?",
                    "Update available",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

                // TODO: use custom icon/gui

                splash.setVisible(true);

                if (option == 0) {
                    splash.getInfoLabel().setText("Updating the launcher ... please wait.");
                    updater.update();
                }
            }

            // Game versions
            splash.getInfoLabel().setText("Load game versions..."); //TODO: i18n
            final TerasologyGameVersion gameVersion = new TerasologyGameVersion();
            gameVersion.loadVersions(settings);
            logger.debug("Game versions loaded: {}", gameVersion);

            // LauncherFrame
            splash.getInfoLabel().setText("Creating launcher frame ...");   // TODO: i18n
            final Frame frame = new LauncherFrame(applicationDir, os, settings, gameVersion);
            frame.setVisible(true);

            // Dispose splash screen
            splash.setVisible(false);
            splash.dispose();

            logger.debug("TerasologyLauncher started");
        } catch (Exception e) {
            logger.error("Starting TerasologyLauncher failed!", e);
            // TODO Message and title
            JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

}
