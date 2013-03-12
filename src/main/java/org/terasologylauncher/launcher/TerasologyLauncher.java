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
import org.terasologylauncher.Settings;
import org.terasologylauncher.gui.LauncherFrame;
import org.terasologylauncher.updater.GameData;
import org.terasologylauncher.util.TerasologyDirectories;
import org.terasologylauncher.version.TerasologyLauncherVersion;

import javax.swing.JOptionPane;
import java.awt.Frame;
import java.util.Properties;

/**
 * @author Skaldarnar
 */
public final class TerasologyLauncher {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyLauncher.class);

    private TerasologyLauncher() {
    }

    public static void main(final String[] args) {
        long start = System.currentTimeMillis();
        final long startUpTime = start;

        //TODO: Add splash screen

        //TODO: check for launcher update
        logger.debug("Launcher build number: {}", TerasologyLauncherVersion.getInstance().getBuildNumber());

        logger.debug("Checking for launcher update took {}", (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        TerasologyDirectories.getLauncherDir().mkdirs();

        logger.debug("Setting up directories took {}", (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        if (Settings.getProperties() == null) {
            final Properties properties = Settings.setUpSettings();
            if (properties == null) {
                throw new NullPointerException("Could not initialize the settings file!");
            }
            Settings.setProperties(properties);
        }

        logger.debug("Reading settings/properties took {}", (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        if (!GameData.checkInternetConnection()) {
            JOptionPane.showMessageDialog(null, "Cannot establish internet connection. You can only play offline.",
                "No internet connection!", JOptionPane.WARNING_MESSAGE);
        }

        logger.debug("Checking internet connection took {}", (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        //TODO: Add Debug console

        // Setup launcher frame and display
        final Frame frame = new LauncherFrame();

        logger.debug("Creating window took {}", (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        //TODO: Check for game update

        //TODO: dispose splash screen
        frame.setVisible(true);

        logger.debug("Setting visible took {}", (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        logger.debug("Startup took {}", (System.currentTimeMillis() - startUpTime) + " ms");
    }

}
