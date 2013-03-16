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
import org.terasologylauncher.BuildType;
import org.terasologylauncher.Languages;
import org.terasologylauncher.Settings;
import org.terasologylauncher.Versions;
import org.terasologylauncher.gui.LauncherFrame;
import org.terasologylauncher.gui.SplashScreen;
import org.terasologylauncher.util.OperatingSystem;
import org.terasologylauncher.util.TerasologyDirectories;
import org.terasologylauncher.version.TerasologyLauncherVersion;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

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

            // Show splash screen
            InputStream stream = null;
            BufferedImage bg;
            try {
                stream = TerasologyLauncher.class.getResourceAsStream("/org/terasologylauncher/images/splash.png");
                bg = ImageIO.read(stream);
            } catch (IOException e) {
                logger.error("Could not read splash image.", e);
                bg = new BufferedImage(600, 200, BufferedImage.TYPE_INT_ARGB);
            }
            final SplashScreen splash = new SplashScreen(bg);
            splash.setVisible(true);
            
            // TerasologyLauncherVersion
            logger.debug("TerasologyLauncherVersion {}", TerasologyLauncherVersion.getInstance().toString());

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
            final File applicationDir = TerasologyDirectories.getApplicationDirectory(os);
            try {
                TerasologyDirectories.checkDirectory(applicationDir);
            } catch (IOException e) {
                logger.error("Can not create or use application directory! " + applicationDir, e);
                // TODO Message and title
                JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            logger.debug("Application directory: {}", applicationDir);

            // Launcher directory
            final File launcherDir = new File(applicationDir, TerasologyDirectories.LAUNCHER_DIR_NAME);
            try {
                TerasologyDirectories.checkDirectory(launcherDir);
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
            logger.debug("Settings loaded " + settings);

            logger.debug("Checking internet connection took {}", (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();

            //TODO: Add Debug console

            // load game versions
            Versions.getVersions(settings, BuildType.STABLE);
            Versions.getVersions(settings, BuildType.NIGHTLY);

            // LauncherFrame
            final Frame frame = new LauncherFrame(applicationDir, os, settings);

            //TODO: Check for game update

            frame.setVisible(true);

            // dispose splash screen
            splash.setVisible(false);
            splash.dispose();

            logger.debug("Setting visible took {}", (System.currentTimeMillis() - start) + "ms");

            logger.debug("Startup took {}", (System.currentTimeMillis() - startUpTime) + " ms");
            logger.debug("TerasologyLauncher started");
        } catch (Exception e) {
            logger.error("Starting Terasology Launcher failed!", e);
            // TODO Message and title
            JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

}
