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
import org.terasologylauncher.util.TerasologyDirectories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Provides access to launcher settings.
 *
 * @author Skaldarnar
 */
public final class Settings {

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static final String SETTINGS_FILE_NAME = "settings.properties";
    private static final String DEFAULT_SETTINGS_FILE_NAME = "/settings.properties";

    private static File settingsFile;
    private static Properties properties;

    private Settings() {
    }

    public static void init() throws IOException {
        properties = new Properties();
        settingsFile = new File(TerasologyDirectories.getLauncherDir(), SETTINGS_FILE_NAME);
        if (settingsFile.exists()) {
            loadSettings();
        } else {
            loadDefaultSettings();
        }
    }

    private static void loadDefaultSettings() throws IOException {
        logger.debug("Load default settings");

        final InputStream inputStream = TerasologyLauncher.class.getResourceAsStream(DEFAULT_SETTINGS_FILE_NAME);
        try {
            properties.load(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.info("The InputStream could not be closed.", e);
            }
        }
    }

    private static void loadSettings() throws IOException {
        logger.debug("Load settings from {}", settingsFile);

        final InputStream inputStream = new FileInputStream(settingsFile);
        try {
            properties.load(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.info("The InputStream could not be closed. " + settingsFile, e);
            }
        }
    }

    public static void storeSettings() throws IOException {
        logger.debug("Store settings into {}", settingsFile);

        if (!settingsFile.getParentFile().exists() && !settingsFile.getParentFile().mkdirs()) {
            throw new IOException("The directory could not be created. " + settingsFile);
        }
        final OutputStream outputStream = new FileOutputStream(settingsFile);
        try {
            properties.store(outputStream, "Terasology Launcher - Settings");
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.info("The OutputStream could not be closed. " + settingsFile, e);
            }
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

    public static synchronized void setLocaleString(String localeString) {
        properties.setProperty("locale", localeString);
    }

    public static synchronized String getLocaleString() {
        return properties.getProperty("locale");
    }
}
