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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Provides access to launcher settings.
 *
 * @author Skaldarnar
 */
public final class Settings {

    public static final BuildType BUILD_TYPE_DEFAULT = BuildType.STABLE;
    public static final int BUILD_VERSION_LATEST = -1;
    public static final int MAX_MEMORY_DEFAULT = 0;
    public static final int INITIAL_MEMORY_NONE = -1;

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static final String SETTINGS_FILE_NAME = "settings.properties";

    private final File settingsFile;
    private final Properties properties;

    public Settings(final File directory) {
        properties = new Properties();
        settingsFile = new File(directory, SETTINGS_FILE_NAME);
    }

    public synchronized void load() throws IOException {
        if (settingsFile.exists()) {
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
    }

    public synchronized void store() throws IOException {
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

    public synchronized void init() {
        // locale
        final String localeStr = properties.getProperty("locale");
        if (localeStr != null) {
            Languages.init(localeStr);
        }
        properties.setProperty("locale", Languages.getCurrentLocale().toString());

        // buildType
        final String buildTypeStr = properties.getProperty("buildType");
        BuildType buildType = BUILD_TYPE_DEFAULT;
        if (buildTypeStr != null) {
            try {
                buildType = BuildType.valueOf(buildTypeStr);
            } catch (IllegalArgumentException e) {
                logger.debug("Illegal BuildType! " + buildTypeStr, e);
            }
        }
        properties.setProperty("buildType", buildType.name());

        // buildVersion
        final BuildType[] buildTypes = BuildType.values();
        for (BuildType b : buildTypes) {
            final String key = "buildVersion_" + b.name();
            final String buildVersionStr = properties.getProperty(key);
            int buildVersion = BUILD_VERSION_LATEST;
            if (buildVersionStr != null) {
                try {
                    buildVersion = Integer.parseInt(buildVersionStr);
                } catch (NumberFormatException e) {
                    logger.debug("Illegal BuildVersion! " + buildVersionStr, e);
                }
            }
            properties.setProperty(key, String.valueOf(buildVersion));
        }

        // maxMemory
        final String maxMemoryStr = properties.getProperty("maxMemory");
        int maxMemory = MAX_MEMORY_DEFAULT;
        try {
            maxMemory = Integer.parseInt(maxMemoryStr);
        } catch (NumberFormatException e) {
            logger.debug("Illegal MaxMemory! " + maxMemoryStr, e);
        }
        properties.setProperty("maxMemory", String.valueOf(maxMemory));

        // initialMemory
        final String initialMemoryStr = properties.getProperty("initialMemory");
        int initialMemory = INITIAL_MEMORY_NONE;
        try {
            initialMemory = Integer.parseInt(initialMemoryStr);
        } catch (NumberFormatException e) {
            logger.debug("Illegal InitialMemory! " + initialMemoryStr, e);
        }
        properties.setProperty("initialMemory", String.valueOf(initialMemory));
    }

    /*============================== Settings access ================================*/

    public synchronized void setLocale(Locale locale) {
        properties.setProperty("locale", locale.toString());
    }

    public synchronized void setBuildType(final BuildType buildType) {
        properties.setProperty("buildType", buildType.name());
    }

    public synchronized BuildType getBuildType() {
        return BuildType.valueOf(properties.getProperty("buildType"));
    }

    /**
     * Sets the build version property, depending on the build version.
     *
     * @param version   the version number; -1 for the "Latest"
     * @param buildType the build type of the game
     */
    public synchronized void setBuildVersion(final int version, final BuildType buildType) {
        properties.setProperty("buildVersion_" + buildType.name(), String.valueOf(version));
    }

    public synchronized int getBuildVersion(final BuildType buildType) {
        return Integer.parseInt(properties.getProperty("buildVersion_" + buildType.name()));
    }

    public synchronized boolean isBuildVersionLatest(final BuildType buildType) {
        return BUILD_VERSION_LATEST == getBuildVersion(buildType);
    }

    public synchronized void setMaximalMemory(final int maxMemory) {
        properties.setProperty("maxMemory", String.valueOf(maxMemory));
    }

    /** @return the option id of the memory object. */
    public synchronized int getMaximalMemory() {
        return Integer.parseInt(properties.getProperty("maxMemory"));
    }

    public synchronized void setInitialMemory(final int initialMemory) {
        properties.setProperty("initialMemory", String.valueOf(initialMemory));
    }

    /** @return the option id of the memory object or -1 for "None". */
    public synchronized int getInitialMemory() {
        return Integer.parseInt(properties.getProperty("initialMemory"));
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + properties.toString() + "]";
    }
}
