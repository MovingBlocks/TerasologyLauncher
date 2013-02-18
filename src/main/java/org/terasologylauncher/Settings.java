package org.terasologylauncher;

import org.terasologylauncher.launcher.TerasologyLauncher;
import org.terasologylauncher.util.Memory;
import org.terasologylauncher.util.TerasologyDirectories;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 12.02.13
 * Time: 21:08
 * To change this template use File | Settings | File Templates.
 */
public class Settings {
    public static final String SETTINGS_FILE_NAME = "launcher.settings";

    private static Properties properties;

    public static void setProperties(Properties properties) {
        if (Settings.properties != null) {
            throw new IllegalArgumentException("Settings already set!");
        }
        Settings.properties = properties;
    }

    public static synchronized Properties getProperties() {
        return properties;
    }

    public static Properties setUpSettings() {
        File settingsFile = new File(TerasologyDirectories.getLauncherDir(), SETTINGS_FILE_NAME);
        Properties defaultProperties = new Properties();
        // if the file does not exist, copy default file from launcher
        if (!settingsFile.exists()){
            try {
                InputStream input = TerasologyLauncher.class.getResourceAsStream("/launcher.settings");
                if (input != null) {
                    defaultProperties.load(input);

                    FileOutputStream out = null;
                    try {
                        settingsFile.getParentFile().mkdirs();
                        out = new FileOutputStream(settingsFile);
                        defaultProperties.store(out, "Default settings!");
                    } catch (Exception e) {

                    } finally {
                        try {
                            input.close();
                        } catch (Exception ignored) { }
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (Exception ignored) { }
                    }

                }
            } catch (Exception e) { }
        } else {
            try {
                InputStream inputStream = new FileInputStream(settingsFile);
                defaultProperties.load(inputStream);
                try {
                    inputStream.close();
                } catch (Exception ignored) { }
            } catch (Exception e) { }
        }
        return defaultProperties;
    }

    public static void storeSettings() {
        File settingsFile = new File(TerasologyDirectories.getLauncherDir(), SETTINGS_FILE_NAME);
        try {
            OutputStream output = new FileOutputStream(settingsFile);
            properties.store(output, "Terasology Launcher settings");
            try {
                output.close();
            } catch (Exception ignored) { }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /*============================== Settings access ================================*/

    public static synchronized void setBuildType(BuildType type){
        properties.setProperty("buildType", String.valueOf(type.type()));
    }

    public static synchronized BuildType getBuildType() {
        int buildType = Integer.parseInt(properties.getProperty("buildType"));
        return BuildType.getType(buildType);
    }

    /**
     * Sets the build version property, depending on the build version. The key for stable build is
     * <code>stableBuildVersion</code>, the key for nightly build is <code>nightlyBuildVersion</code>
     *
     * @param version the version number
     * @param type the build type of the game
     */
    public static synchronized void setBuildVersion(String version, BuildType type){
        properties.setProperty(type.toString()+"BuildVersion", version);
    }

    public static synchronized String getBuildVersion(BuildType type) {
        return properties.getProperty(type.toString() + "BuildVersion");
    }

    public static synchronized void setMaximalMemory(int memoryID) {
        properties.setProperty("maxMemory", String.valueOf(memoryID));
    }

    /**
     * @return the option id of the memory object.
     */
    public static synchronized int getMaximalMemory() {
        return Integer.parseInt(properties.getProperty("maxMemory"));
    }

    public static synchronized void setInitialMemory(int memoryID) {
        properties.setProperty("initialMemory", String.valueOf(memoryID));
    }

    /**
     * @return the option id of the memory object or -1 for "None".
     */
    public static synchronized int getInitialMemory() {
        return Integer.parseInt(properties.getProperty("initialMemory"));
    }

    public static List<String> createParameters() {
        List<String> parameters = new ArrayList<String>();
        // add maximal RAM parameter
        parameters.add("-Xmx");
        parameters.add(Memory.getMemoryFromId(getMaximalMemory()).getMemoryMB()+"m");
        // add initial RAM parameter
        if (getInitialMemory() >= 0){
            parameters.add("-Xms");
            parameters.add(Memory.getMemoryFromId(getInitialMemory()).getMemoryMB()+"m");
        }
        return parameters;
    }
}
