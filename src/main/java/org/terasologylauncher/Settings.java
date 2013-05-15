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
import org.terasologylauncher.util.JavaHeapSize;
import org.terasologylauncher.version.TerasologyGameVersion;

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

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static final String SETTINGS_FILE_NAME = "settings.properties";

    private static final String COMMENT_SETTINGS = "Terasology Launcher - Settings";

    private static final BuildType BUILD_TYPE_DEFAULT = BuildType.STABLE;
    private static final JavaHeapSize MAX_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    private static final JavaHeapSize INITIAL_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    private static final boolean SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT = true;
    private static final boolean CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT = true;

    private static final String PROPERTY_LOCALE = "locale";
    private static final String PROPERTY_BUILD_TYPE = "buildType";
    private static final String PROPERTY_MAX_HEAP_SIZE = "maxHeapSize";
    private static final String PROPERTY_INITIAL_HEAP_SIZE = "initialHeapSize";
    private static final String PROPERTY_PREFIX_BUILD_VERSION = "buildVersion_";
    private static final String PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES = "searchForLauncherUpdates";
    private static final String PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START = "closeLauncherAfterGameStart";

    private final File settingsFile;
    private final Properties properties;

    public Settings(final File directory) {
        settingsFile = new File(directory, SETTINGS_FILE_NAME);
        properties = new Properties();
    }

    public synchronized void load() throws IOException {
        if (settingsFile.exists()) {
            logger.debug("Load the launcher settings from the file '{}'.", settingsFile);

            // load settings
            final InputStream inputStream = new FileInputStream(settingsFile);
            try {
                properties.load(inputStream);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("The file '{}' could not be closed.", settingsFile, e);
                }
            }
        }
    }

    public synchronized void store() throws IOException {
        logger.debug("Store the launcher settings into the file '{}'.", settingsFile);

        // create directory
        if (!settingsFile.getParentFile().exists() && !settingsFile.getParentFile().mkdirs()) {
            throw new IOException("The directory could not be created. " + settingsFile.getParentFile());
        }

        // store settings
        final OutputStream outputStream = new FileOutputStream(settingsFile);
        try {
            properties.store(outputStream, COMMENT_SETTINGS);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.warn("The file '{}' could not be closed.", settingsFile, e);
            }
        }
    }

    public synchronized void init() {
        // locale
        final String localeStr = properties.getProperty(PROPERTY_LOCALE);
        if (localeStr != null) {
            Languages.init(localeStr);

            if (!Languages.getCurrentLocale().toString().equals(localeStr)) {
                logger.info("Invalid value '{}' for the parameter '{}'!", localeStr, PROPERTY_LOCALE);
            }
        }
        properties.setProperty(PROPERTY_LOCALE, Languages.getCurrentLocale().toString());

        // buildType
        final String buildTypeStr = properties.getProperty(PROPERTY_BUILD_TYPE);
        BuildType buildType = BUILD_TYPE_DEFAULT;
        if (buildTypeStr != null) {
            try {
                buildType = BuildType.valueOf(buildTypeStr);
            } catch (IllegalArgumentException e) {
                logger.info("Invalid value '{}' for the parameter '{}'!", buildTypeStr, PROPERTY_BUILD_TYPE);
            }
        }
        properties.setProperty(PROPERTY_BUILD_TYPE, buildType.name());

        // buildVersion
        final BuildType[] buildTypes = BuildType.values();
        for (BuildType b : buildTypes) {
            final String key = PROPERTY_PREFIX_BUILD_VERSION + b.name();
            final String buildVersionStr = properties.getProperty(key);
            int buildVersion = TerasologyGameVersion.BUILD_VERSION_LATEST;
            if (buildVersionStr != null) {
                try {
                    buildVersion = Integer.parseInt(buildVersionStr);
                } catch (NumberFormatException e) {
                    logger.info("Invalid value '{}' for the parameter '{}'!", buildVersionStr, key);
                }
            }
            properties.setProperty(key, String.valueOf(buildVersion));
        }

        // max heap size
        final String maxHeapSizeStr = properties.getProperty(PROPERTY_MAX_HEAP_SIZE);
        JavaHeapSize maxJavaHeapSize = MAX_HEAP_SIZE_DEFAULT;
        if (maxHeapSizeStr != null) {
            try {
                maxJavaHeapSize = JavaHeapSize.valueOf(maxHeapSizeStr);
            } catch (IllegalArgumentException e) {
                logger.info("Invalid value '{}' for the parameter '{}'!", maxHeapSizeStr,
                    PROPERTY_MAX_HEAP_SIZE);
            }
        }
        properties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxJavaHeapSize.name());

        // initial heap size
        final String initialHeapSizeStr = properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE);
        JavaHeapSize initialJavaHeapSize = INITIAL_HEAP_SIZE_DEFAULT;
        if (initialHeapSizeStr != null) {
            try {
                initialJavaHeapSize = JavaHeapSize.valueOf(initialHeapSizeStr);
            } catch (IllegalArgumentException e) {
                logger.info("Invalid value '{}' for the parameter '{}'!", initialHeapSizeStr,
                    PROPERTY_INITIAL_HEAP_SIZE);
            }
        }
        properties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialJavaHeapSize.name());


        // searchForLauncherUpdates
        final String searchForLauncherUpdatesStr = properties.getProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES);
        boolean searchForLauncherUpdates = SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT;
        if (searchForLauncherUpdatesStr != null) {
            searchForLauncherUpdates = Boolean.valueOf(searchForLauncherUpdatesStr);
        }
        properties.setProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES, Boolean.toString(searchForLauncherUpdates));

        // closeLauncherAfterGameStart
        final String closeLauncherAfterGameStartStr = properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START);
        boolean closeLauncherAfterGameStart = CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT;
        if (closeLauncherAfterGameStartStr != null) {
            closeLauncherAfterGameStart = Boolean.valueOf(closeLauncherAfterGameStartStr);
        }
        properties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, Boolean.toString(closeLauncherAfterGameStart));
    }

    public synchronized void setLocale(final Locale locale) {
        properties.setProperty(PROPERTY_LOCALE, locale.toString());
    }

    public synchronized void setBuildType(final BuildType buildType) {
        properties.setProperty(PROPERTY_BUILD_TYPE, buildType.name());
    }

    public synchronized BuildType getBuildType() {
        return BuildType.valueOf(properties.getProperty(PROPERTY_BUILD_TYPE));
    }

    public synchronized void setBuildVersion(final int version, final BuildType buildType) {
        properties.setProperty(PROPERTY_PREFIX_BUILD_VERSION + buildType.name(), String.valueOf(version));
    }

    public synchronized int getBuildVersion(final BuildType buildType) {
        return Integer.parseInt(properties.getProperty(PROPERTY_PREFIX_BUILD_VERSION + buildType.name()));
    }

    public synchronized void setMaxHeapSize(final JavaHeapSize maxHeapSize) {
        properties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxHeapSize.name());
    }

    public synchronized JavaHeapSize getMaxHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_MAX_HEAP_SIZE));
    }

    public synchronized void setInitialHeapSize(final JavaHeapSize initialHeapSize) {
        properties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialHeapSize.name());
    }

    public synchronized JavaHeapSize getInitialHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE));
    }

    public synchronized void setSearchForLauncherUpdates(final boolean searchForLauncherUpdates) {
        properties.setProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES, Boolean.toString(searchForLauncherUpdates));
    }

    public synchronized boolean isSearchForLauncherUpdates() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES));
    }

    public synchronized void setCloseLauncherAfterGameStart(final boolean closeLauncherAfterGameStart) {
        properties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, Boolean.toString(closeLauncherAfterGameStart));
    }

    public synchronized boolean isCloseLauncherAfterGameStart() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START));
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + properties.toString() + "]";
    }
}
