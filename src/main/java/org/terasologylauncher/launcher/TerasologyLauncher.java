package org.terasologylauncher.launcher;

import org.terasologylauncher.Settings;
import org.terasologylauncher.gui.LauncherFrame;
import org.terasologylauncher.updater.GameData;
import org.terasologylauncher.util.TerasologyDirectories;

import javax.swing.JOptionPane;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/** @author Skaldarnar */
public class TerasologyLauncher {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        final long startUpTime = start;

        //TODO: Add splash screen

        //TODO: Init logger

        //TODO: check for launcher update
        int launcherBuild = parseInt(getLauncherBuild(), -1);       // local launcher build

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

    private static int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getLauncherBuild() {
        String build = "0";
        try {
            InputStream version = TerasologyLauncher.class.getResourceAsStream("/version");
            BufferedReader br = new BufferedReader(new InputStreamReader(version));
            build = br.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return build;
    }
}
