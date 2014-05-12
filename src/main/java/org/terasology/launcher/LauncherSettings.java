/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.GameSettings;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Properties;

/**
 * Provides access to launcher settings.
 */
public final class LauncherSettings implements GameSettings {

    private static final Logger logger = LoggerFactory.getLogger(LauncherSettings.class);

    private static final String LAUNCHER_SETTINGS_FILE_NAME = "TerasologyLauncherSettings.properties";
    private static final String COMMENT_SETTINGS = "Terasology Launcher - Settings";

    private static final GameJob JOB_DEFAULT = GameJob.TerasologyStable;
    private static final JavaHeapSize MAX_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    private static final JavaHeapSize INITIAL_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    private static final String LAST_BUILD_NUMBER_DEFAULT = "";
    private static final boolean SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT = true;
    private static final boolean CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT = true;
    private static final boolean SAVE_DOWNLOADED_FILES_DEFAULT = false;

    private static final String PROPERTY_LOCALE = "locale";
    private static final String PROPERTY_JOB = "job";
    private static final String PROPERTY_MAX_HEAP_SIZE = "maxHeapSize";
    private static final String PROPERTY_INITIAL_HEAP_SIZE = "initialHeapSize";
    private static final String PROPERTY_PREFIX_BUILD_VERSION = "buildVersion_";
    private static final String PROPERTY_PREFIX_LAST_BUILD_NUMBER = "lastBuildNumber_";
    private static final String PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES = "searchForLauncherUpdates";
    private static final String PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START = "closeLauncherAfterGameStart";
    private static final String PROPERTY_GAME_DIRECTORY = "gameDirectory";
    private static final String PROPERTY_GAME_DATA_DIRECTORY = "gameDataDirectory";
    private static final String PROPERTY_SAVE_DOWNLOADED_FILES = "saveDownloadedFiles";

    private final File launcherSettingsFile;
    private final Properties properties;

    public LauncherSettings(File launcherDirectory) {
        launcherSettingsFile = new File(launcherDirectory, LAUNCHER_SETTINGS_FILE_NAME);
        properties = new Properties();
    }

    public String getLauncherSettingsFilePath() {
        return launcherSettingsFile.getPath();
    }

    public synchronized void load() throws IOException {
        if (launcherSettingsFile.exists()) {
            logger.debug("Load the launcher settings from the file '{}'.", launcherSettingsFile);

            // load settings
            try (InputStream inputStream = new FileInputStream(launcherSettingsFile)) {
                properties.load(inputStream);
            }
        }
    }

    public synchronized void store() throws IOException {
        logger.trace("Store the launcher settings into the file '{}'.", launcherSettingsFile);

        // create directory
        if (!launcherSettingsFile.getParentFile().exists() && !launcherSettingsFile.getParentFile().mkdirs()) {
            throw new IOException("Could not create directory! " + launcherSettingsFile.getParentFile());
        }

        // store settings
        try (OutputStream outputStream = new FileOutputStream(launcherSettingsFile)) {
            properties.store(outputStream, COMMENT_SETTINGS);
        }
    }

    public synchronized void init() {
        logger.trace("Init launcher settings. {}", properties);

        initLocale();
        initJob();
        initBuildVersion();
        initLastBuildNumber();
        initMaxHeapSize();
        initInitialHeapSize();
        initSearchForLauncherUpdates();
        initCloseLauncherAfterGameStart();
        initSaveDownloadedFiles();
        initGameDirectory();
        initGameDataDirectory();
    }

    private void initLocale() {
        final String localeStr = properties.getProperty(PROPERTY_LOCALE);
        if (localeStr != null) {
            Languages.init(localeStr);

            if (!Languages.getCurrentLocale().toString().equals(localeStr)) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", localeStr, PROPERTY_LOCALE);
            }
        }
        properties.setProperty(PROPERTY_LOCALE, Languages.getCurrentLocale().toString());
    }

    private void initJob() {
        final String jobStr = properties.getProperty(PROPERTY_JOB);
        GameJob job = JOB_DEFAULT;
        if (jobStr != null) {
            try {
                job = GameJob.valueOf(jobStr);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", jobStr, PROPERTY_JOB);
            }
        }
        properties.setProperty(PROPERTY_JOB, job.name());
    }

    private void initBuildVersion() {
        for (GameJob j : GameJob.values()) {
            final String key = PROPERTY_PREFIX_BUILD_VERSION + j.name();
            final String buildVersionStr = properties.getProperty(key);
            int buildVersion = TerasologyGameVersion.BUILD_VERSION_LATEST;
            if (buildVersionStr != null) {
                try {
                    buildVersion = Integer.parseInt(buildVersionStr);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid value '{}' for the parameter '{}'!", buildVersionStr, key);
                }
            }
            properties.setProperty(key, String.valueOf(buildVersion));
        }
    }

    private void initLastBuildNumber() {
        for (GameJob j : GameJob.values()) {
            final String key = PROPERTY_PREFIX_LAST_BUILD_NUMBER + j.name();
            final String lastBuildNumberStr = properties.getProperty(key);
            Integer lastBuildNumber = null;
            if (lastBuildNumberStr != null) {
                try {
                    lastBuildNumber = Integer.parseInt(lastBuildNumberStr);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid value '{}' for the parameter '{}'!", lastBuildNumberStr, key);
                }
            }
            if ((lastBuildNumber != null) && (lastBuildNumber >= j.getMinBuildNumber())) {
                properties.setProperty(key, lastBuildNumber.toString());
            } else {
                properties.setProperty(key, LAST_BUILD_NUMBER_DEFAULT);
            }
        }
    }

    private void initMaxHeapSize() {
        final String maxHeapSizeStr = properties.getProperty(PROPERTY_MAX_HEAP_SIZE);
        JavaHeapSize maxJavaHeapSize = MAX_HEAP_SIZE_DEFAULT;
        if (maxHeapSizeStr != null) {
            try {
                maxJavaHeapSize = JavaHeapSize.valueOf(maxHeapSizeStr);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", maxHeapSizeStr, PROPERTY_MAX_HEAP_SIZE);
            }
        }
        properties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxJavaHeapSize.name());
    }

    private void initInitialHeapSize() {
        final String initialHeapSizeStr = properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE);
        JavaHeapSize initialJavaHeapSize = INITIAL_HEAP_SIZE_DEFAULT;
        if (initialHeapSizeStr != null) {
            try {
                initialJavaHeapSize = JavaHeapSize.valueOf(initialHeapSizeStr);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", initialHeapSizeStr, PROPERTY_INITIAL_HEAP_SIZE);
            }
        }
        properties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialJavaHeapSize.name());
    }

    private void initSearchForLauncherUpdates() {
        final String searchForLauncherUpdatesStr = properties.getProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES);
        boolean searchForLauncherUpdates = SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT;
        if (searchForLauncherUpdatesStr != null) {
            searchForLauncherUpdates = Boolean.valueOf(searchForLauncherUpdatesStr);
        }
        properties.setProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES, Boolean.toString(searchForLauncherUpdates));
    }

    private void initCloseLauncherAfterGameStart() {
        final String closeLauncherAfterGameStartStr = properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START);
        boolean closeLauncherAfterGameStart = CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT;
        if (closeLauncherAfterGameStartStr != null) {
            closeLauncherAfterGameStart = Boolean.valueOf(closeLauncherAfterGameStartStr);
        }
        properties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, Boolean.toString(closeLauncherAfterGameStart));
    }

    private void initSaveDownloadedFiles() {
        final String saveDownloadedFilesStr = properties.getProperty(PROPERTY_SAVE_DOWNLOADED_FILES);
        boolean saveDownloadedFiles = SAVE_DOWNLOADED_FILES_DEFAULT;
        if (saveDownloadedFilesStr != null) {
            saveDownloadedFiles = Boolean.valueOf(saveDownloadedFilesStr);
        }
        properties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, Boolean.toString(saveDownloadedFiles));
    }

    private void initGameDirectory() {
        final String gameDirectoryStr = properties.getProperty(PROPERTY_GAME_DIRECTORY);
        File gameDirectory = null;
        if ((gameDirectoryStr != null) && (gameDirectoryStr.trim().length() > 0)) {
            try {
                gameDirectory = new File(new URI(gameDirectoryStr));
            } catch (URISyntaxException | RuntimeException e) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", gameDirectoryStr, PROPERTY_GAME_DIRECTORY);
            }
        }
        if (gameDirectory != null) {
            properties.setProperty(PROPERTY_GAME_DIRECTORY, gameDirectory.toURI().toString());
        } else {
            properties.setProperty(PROPERTY_GAME_DIRECTORY, "");
        }
    }

    private void initGameDataDirectory() {
        final String gameDataDirectoryStr = properties.getProperty(PROPERTY_GAME_DATA_DIRECTORY);
        File gameDataDirectory = null;
        if ((gameDataDirectoryStr != null) && (gameDataDirectoryStr.trim().length() > 0)) {
            try {
                gameDataDirectory = new File(new URI(gameDataDirectoryStr));
            } catch (URISyntaxException | RuntimeException e) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", gameDataDirectoryStr, PROPERTY_GAME_DATA_DIRECTORY);
            }
        }
        if (gameDataDirectory != null) {
            properties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, gameDataDirectory.toURI().toString());
        } else {
            properties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, "");
        }
    }

    public synchronized void setLocale(Locale locale) {
        properties.setProperty(PROPERTY_LOCALE, locale.toString());
    }

    public synchronized void setJob(GameJob job) {
        properties.setProperty(PROPERTY_JOB, job.name());
    }

    public synchronized GameJob getJob() {
        return GameJob.valueOf(properties.getProperty(PROPERTY_JOB));
    }

    public synchronized void setBuildVersion(int version, GameJob job) {
        properties.setProperty(PROPERTY_PREFIX_BUILD_VERSION + job.name(), String.valueOf(version));
    }

    public synchronized int getBuildVersion(GameJob job) {
        return Integer.parseInt(properties.getProperty(PROPERTY_PREFIX_BUILD_VERSION + job.name()));
    }

    public synchronized void setLastBuildNumber(Integer lastBuildNumber, GameJob job) {
        if ((lastBuildNumber != null) && (lastBuildNumber >= job.getMinBuildNumber())) {
            properties.setProperty(PROPERTY_PREFIX_LAST_BUILD_NUMBER + job.name(), lastBuildNumber.toString());
        } else {
            properties.setProperty(PROPERTY_PREFIX_LAST_BUILD_NUMBER + job.name(), LAST_BUILD_NUMBER_DEFAULT);
        }
    }

    public synchronized Integer getLastBuildNumber(GameJob job) {
        final String lastBuildNumberStr = properties.getProperty(PROPERTY_PREFIX_LAST_BUILD_NUMBER + job.name());
        if (LAST_BUILD_NUMBER_DEFAULT.equals(lastBuildNumberStr)) {
            return null;
        }
        return Integer.parseInt(lastBuildNumberStr);
    }

    public synchronized void setMaxHeapSize(JavaHeapSize maxHeapSize) {
        properties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxHeapSize.name());
    }

    public synchronized JavaHeapSize getMaxHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_MAX_HEAP_SIZE));
    }

    public synchronized void setInitialHeapSize(JavaHeapSize initialHeapSize) {
        properties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialHeapSize.name());
    }

    public synchronized JavaHeapSize getInitialHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE));
    }

    public synchronized void setSearchForLauncherUpdates(boolean searchForLauncherUpdates) {
        properties.setProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES, Boolean.toString(searchForLauncherUpdates));
    }

    public synchronized boolean isSearchForLauncherUpdates() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES));
    }

    public synchronized void setCloseLauncherAfterGameStart(boolean closeLauncherAfterGameStart) {
        properties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, Boolean.toString(closeLauncherAfterGameStart));
    }

    public synchronized boolean isCloseLauncherAfterGameStart() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START));
    }

    public synchronized void setSaveDownloadedFiles(boolean saveDownloadedFiles) {
        properties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, Boolean.toString(saveDownloadedFiles));
    }

    public synchronized boolean isSaveDownloadedFiles() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_SAVE_DOWNLOADED_FILES));
    }

    public synchronized void setGameDirectory(File gameDirectory) {
        properties.setProperty(PROPERTY_GAME_DIRECTORY, gameDirectory.toURI().toString());
    }

    public synchronized File getGameDirectory() {
        final String gameDirectoryStr = properties.getProperty(PROPERTY_GAME_DIRECTORY);
        if ((gameDirectoryStr != null) && (gameDirectoryStr.trim().length() > 0)) {
            try {
                return new File(new URI(gameDirectoryStr));
            } catch (URISyntaxException | RuntimeException e) {
                logger.error("Invalid value '{}' for the parameter '{}'!", gameDirectoryStr, PROPERTY_GAME_DIRECTORY);
            }
        }
        return null;
    }

    public synchronized void setGameDataDirectory(File gameDataDirectory) {
        properties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, gameDataDirectory.toURI().toString());
    }

    public synchronized File getGameDataDirectory() {
        final String gameDataDirectoryStr = properties.getProperty(PROPERTY_GAME_DATA_DIRECTORY);
        if ((gameDataDirectoryStr != null) && (gameDataDirectoryStr.trim().length() > 0)) {
            try {
                return new File(new URI(gameDataDirectoryStr));
            } catch (URISyntaxException | RuntimeException e) {
                logger.error("Invalid value '{}' for the parameter '{}'!", gameDataDirectoryStr, PROPERTY_GAME_DATA_DIRECTORY);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + properties.toString() + "]";
    }
}
