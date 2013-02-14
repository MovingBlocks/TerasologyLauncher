package org.terasologyLauncher.launcher;

import org.terasologyLauncher.Settings;
import org.terasologyLauncher.gui.LauncherFrame;
import org.terasologyLauncher.updater.GameData;
import org.terasologyLauncher.util.TerasologyDirectories;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Skaldarnar
 */
public class TerasologyLauncher {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        final long startUpTime = start;

        //TODO: Add splash screen

        //TODO: Init logger

        //TODO: check for launcher update
        int launcherBuild = parseInt(getLauncherBuild(), -1);       // local launcher build

        System.out.println("Checking for launcher update took " + (System.currentTimeMillis()-start) + "ms");
        start = System.currentTimeMillis();

        TerasologyDirectories dirs = new TerasologyDirectories();
        dirs.getLauncherDir().mkdirs();

        System.out.println("Setting up directories took " + (System.currentTimeMillis()-start) + "ms");
        start = System.currentTimeMillis();

        if (Settings.getProperties() == null) {
            Properties properties = Settings.setUpSettings();
            if (properties == null) {
                throw new NullPointerException("Could not initialize the settings file!");
            }
            Settings.setProperties(properties);
        }

        System.out.println("Reading settings/properties took " + (System.currentTimeMillis()-start) + "ms");
        start = System.currentTimeMillis();

        if (!GameData.checkInternetConnection()) {
            JOptionPane.showMessageDialog(null, "Cannot establish internet connection. You can only play offline.",
                    "No internet connection!", JOptionPane.WARNING_MESSAGE);
        }

        System.out.println("Checking internet connection took " + (System.currentTimeMillis()-start) + "ms");
        start = System.currentTimeMillis();

        //TODO: Add Debug console

        // Setup launcher frame and display
        Frame frame = new LauncherFrame();

        System.out.println("Creating window took " + (System.currentTimeMillis()-start) + "ms");
        start = System.currentTimeMillis();

        //TODO: Check for game update

        //TODO: dispose splash screen
        frame.setVisible(true);

        System.out.println("Setting visible took " + (System.currentTimeMillis()-start) + "ms");
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
            URL version = TerasologyLauncher.class.getResource("/version");
            FileReader reader = new FileReader(new File(version.toURI()));
            BufferedReader br = new BufferedReader(reader);
            build = br.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return build;
    }
}
