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

package org.terasologylauncher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.launcher.TerasologyLauncher;
import org.terasologylauncher.util.Memory;
import org.terasologylauncher.util.TerasologyDirectories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Provides access to launcher settings.
 *
 * @author Skaldarnar
 */
public final class Settings {

    public static final String SETTINGS_FILE_NAME = "launcher.settings";

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static Properties properties;

    private Settings() {
    }

    public static void setProperties(final Properties properties) {
        if (Settings.properties != null) {
            throw new IllegalArgumentException("Settings already set!");
        }
        Settings.properties = properties;
    }

    public static synchronized Properties getProperties() {
        return properties;
    }

    public static Properties setUpSettings() {
        final File settingsFile = new File(TerasologyDirectories.getLauncherDir(), SETTINGS_FILE_NAME);
        final Properties defaultProperties = new Properties();
        // if the file does not exist, copy default file from launcher
        if (!settingsFile.exists()) {
            try {
                final InputStream input = TerasologyLauncher.class.getResourceAsStream("/launcher.settings");
                if (input != null) {
                    defaultProperties.load(input);

                    FileOutputStream out = null;
                    try {
                        // TODO handle "false" result of mkdirs()
                        settingsFile.getParentFile().mkdirs();
                        out = new FileOutputStream(settingsFile);
                        defaultProperties.store(out, "Default settings!");
                    } catch (IOException e) {
                        logger.warn("Setting up settings file failed.", e);
                    } finally {
                        // JAVA 7: Cleanup here
                        try {
                            input.close();
                        } catch (Exception ignored) {
                            logger.info("Could not close settings file.", ignored);
                        }
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (Exception ignored) {
                            logger.info("Could not close settings file.", ignored);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Could not load settings.", e);
            }
        } else {
            try {
                final InputStream inputStream = new FileInputStream(settingsFile);
                defaultProperties.load(inputStream);
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                    logger.info("Could not close settings file.", ignored);
                }
            } catch (Exception e) {
                logger.info("Can't load setting!", e);
            }
        }
        return defaultProperties;
    }

    public static void storeSettings() {
        final File settingsFile = new File(TerasologyDirectories.getLauncherDir(), SETTINGS_FILE_NAME);
        try {
            final OutputStream output = new FileOutputStream(settingsFile);
            properties.store(output, "Terasology Launcher settings");
            try {
                output.close();
            } catch (Exception ignored) {
                logger.info("Could not close settings file.", ignored);
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not store settings.", e);
        } catch (IOException e) {
            logger.error("Could not store settings.", e);
        }
    }

    /*============================== Settings access ================================*/

    public static synchronized void setBuildType(final BuildType type) {
        properties.setProperty("buildType", String.valueOf(type.type()));
    }

    public static synchronized BuildType getBuildType() {
        final int buildType = Integer.parseInt(properties.getProperty("buildType"));
        return BuildType.getType(buildType);
    }

    /**
     * Sets the build version property, depending on the build version.
     * The key for stable build is <code>stableBuildVersion</code>,
     * the key for nightly build is <code>nightlyBuildVersion</code>.
     *
     * @param version the version number
     * @param type    the build type of the game
     */
    public static synchronized void setBuildVersion(final String version, final BuildType type) {
        properties.setProperty(type.toString() + "BuildVersion", version);
    }

    public static synchronized String getBuildVersion(final BuildType type) {
        return properties.getProperty(type.toString() + "BuildVersion");
    }

    public static synchronized void setMaximalMemory(final int memoryID) {
        properties.setProperty("maxMemory", String.valueOf(memoryID));
    }

    /**
     * @return the option id of the memory object.
     */
    public static synchronized int getMaximalMemory() {
        return Integer.parseInt(properties.getProperty("maxMemory"));
    }

    public static synchronized void setInitialMemory(final int memoryID) {
        properties.setProperty("initialMemory", String.valueOf(memoryID));
    }

    /**
     * @return the option id of the memory object or -1 for "None".
     */
    public static synchronized int getInitialMemory() {
        return Integer.parseInt(properties.getProperty("initialMemory"));
    }

    public static List<String> createParameters() {
        final List<String> parameters = new ArrayList<String>();
        // add maximal RAM parameter
        parameters.add("-Xmx" + Memory.getMemoryFromId(getMaximalMemory()).getMemoryMB() + "m");
        // add initial RAM parameter
        if (getInitialMemory() >= 0) {
            parameters.add("-Xms" + Memory.getMemoryFromId(getInitialMemory()).getMemoryMB() + "m");
        }
        return parameters;
    }
}
