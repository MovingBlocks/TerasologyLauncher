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

import org.terasologylauncher.Settings;
import org.terasologylauncher.gui.LauncherFrame;
import org.terasologylauncher.updater.GameData;
import org.terasologylauncher.util.TerasologyDirectories;
import org.terasologylauncher.version.TerasologyLauncherVersion;

import javax.swing.JOptionPane;
import java.awt.Frame;
import java.util.Properties;

/** @author Skaldarnar */
public class TerasologyLauncher {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        final long startUpTime = start;

        //TODO: Add splash screen

        //TODO: Init logger

        //TODO: check for launcher update
        System.out.println("Launcher build number: " + TerasologyLauncherVersion.getInstance().getBuildNumber());

        System.out.println("Checking for launcher update took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        TerasologyDirectories dirs = new TerasologyDirectories();
        dirs.getLauncherDir().mkdirs();

        System.out.println("Setting up directories took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        if (Settings.getProperties() == null) {
            Properties properties = Settings.setUpSettings();
            if (properties == null) {
                throw new NullPointerException("Could not initialize the settings file!");
            }
            Settings.setProperties(properties);
        }

        System.out.println("Reading settings/properties took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        if (!GameData.checkInternetConnection()) {
            JOptionPane.showMessageDialog(null, "Cannot establish internet connection. You can only play offline.",
                "No internet connection!", JOptionPane.WARNING_MESSAGE);
        }

        System.out.println("Checking internet connection took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        //TODO: Add Debug console

        // Setup launcher frame and display
        Frame frame = new LauncherFrame();

        System.out.println("Creating window took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        //TODO: Check for game update

        //TODO: dispose splash screen
        frame.setVisible(true);

        System.out.println("Setting visible took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        System.out.println("Startup took " + (System.currentTimeMillis() - startUpTime) + " ms");
    }

}
