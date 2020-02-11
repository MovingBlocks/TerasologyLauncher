/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.launcher.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

/**
 * Provides access to launcher settings.
 */
@Deprecated
public final class BaseLauncherSettings extends AbstractLauncherSettings {

    public static final String USER_JAVA_PARAMETERS_DEFAULT = "-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:MaxGCPauseMillis=20 -XX:ParallelGCThreads=10";
    public static final String USER_GAME_PARAMETERS_DEFAULT = "";

    public static final String PROPERTY_LOCALE = "locale";
    public static final String PROPERTY_JOB = "job";
    public static final String PROPERTY_MAX_HEAP_SIZE = "maxHeapSize";
    public static final String PROPERTY_INITIAL_HEAP_SIZE = "initialHeapSize";
    public static final String PROPERTY_PREFIX_BUILD_VERSION = "buildVersion_";
    public static final String PROPERTY_PREFIX_LAST_BUILD_NUMBER = "lastBuildNumber_";
    public static final String PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES = "searchForLauncherUpdates";
    public static final String PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START = "closeLauncherAfterGameStart";
    public static final String PROPERTY_GAME_DIRECTORY = "gameDirectory";
    public static final String PROPERTY_GAME_DATA_DIRECTORY = "gameDataDirectory";
    public static final String PROPERTY_SAVE_DOWNLOADED_FILES = "saveDownloadedFiles";
    public static final String PROPERTY_USER_JAVA_PARAMETERS = "userJavaParameters";
    public static final String PROPERTY_USER_GAME_PARAMETERS = "userGameParameters";
    public static final String PROPERTY_LOG_LEVEL = "logLevel";

    public static final GameJob JOB_DEFAULT = GameJob.TerasologyStable;
    public static final JavaHeapSize MAX_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    public static final JavaHeapSize INITIAL_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    public static final String LAST_BUILD_NUMBER_DEFAULT = "";
    public static final boolean SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT = true;
    public static final boolean CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT = true;
    public static final boolean SAVE_DOWNLOADED_FILES_DEFAULT = false;

    public static final String LAUNCHER_SETTINGS_FILE_NAME = "TerasologyLauncherSettings.properties";

    private static final Logger logger = LoggerFactory.getLogger(BaseLauncherSettings.class);

    private static final String COMMENT_SETTINGS = "Terasology Launcher - Settings";

    private static final String WARN_MSG_INVALID_VALUE = "Invalid value '{}' for the parameter '{}'!";
    private static final LogLevel LOG_LEVEL_DEFAULT = LogLevel.DEFAULT;

    private final Path launcherSettingsFile;
    private final Properties properties;

    public BaseLauncherSettings(Path launcherDirectory) {
        launcherSettingsFile = launcherDirectory.resolve(LAUNCHER_SETTINGS_FILE_NAME);
        properties = new Properties();
    }

    public String getLauncherSettingsFilePath() {
        return launcherSettingsFile.toString();
    }

    @Override
    public synchronized void load() throws IOException {
        if (Files.exists(launcherSettingsFile)) {
            logger.debug("Load the launcher settings from the file '{}'.", launcherSettingsFile);

            // load settings
            try (InputStream inputStream = Files.newInputStream(launcherSettingsFile)) {
                properties.load(inputStream);
            }
        }
    }

    @Override
    public synchronized void store() throws IOException {
        logger.trace("Store the launcher settings into the file '{}'.", launcherSettingsFile);

        // create directory
        if (Files.notExists(launcherSettingsFile.getParent())) {
            Files.createDirectories(launcherSettingsFile.getParent());
        }

        // store settings
        try (OutputStream outputStream = Files.newOutputStream(launcherSettingsFile)) {
            properties.store(outputStream, COMMENT_SETTINGS);
            logger.trace("Stored settings: {}", this);
        }
    }

    // --------------------------------------------------------------------- //
    // INIT
    // --------------------------------------------------------------------- //

    protected void initLocale() {
        final String localeStr = properties.getProperty(PROPERTY_LOCALE);
        if (localeStr != null) {
            Languages.init(localeStr);

            if (!Languages.getCurrentLocale().toString().equals(localeStr)) {
                logger.warn(WARN_MSG_INVALID_VALUE, localeStr, PROPERTY_LOCALE);
            }
        }
        properties.setProperty(PROPERTY_LOCALE, Languages.getCurrentLocale().toString());
    }

    protected void initJob() {
        final String jobStr = properties.getProperty(PROPERTY_JOB);
        GameJob job = JOB_DEFAULT;
        if (jobStr != null) {
            try {
                job = GameJob.valueOf(jobStr);
            } catch (IllegalArgumentException e) {
                logger.warn(WARN_MSG_INVALID_VALUE, jobStr, PROPERTY_JOB);
            }
        }
        properties.setProperty(PROPERTY_JOB, job.name());
    }

    protected void initBuildVersion() {
        for (GameJob j : GameJob.values()) {
            final String key = PROPERTY_PREFIX_BUILD_VERSION + j.name();
            final String buildVersionStr = properties.getProperty(key);
            int buildVersion = TerasologyGameVersion.BUILD_VERSION_LATEST;
            if (buildVersionStr != null) {
                try {
                    buildVersion = Integer.parseInt(buildVersionStr);
                } catch (NumberFormatException e) {
                    logger.warn(WARN_MSG_INVALID_VALUE, buildVersionStr, key);
                }
            }
            properties.setProperty(key, String.valueOf(buildVersion));
        }
    }

    protected void initLastBuildNumber() {
        for (GameJob j : GameJob.values()) {
            final String key = PROPERTY_PREFIX_LAST_BUILD_NUMBER + j.name();
            final String lastBuildNumberStr = properties.getProperty(key);
            Integer lastBuildNumber = null;
            if (lastBuildNumberStr != null) {
                try {
                    lastBuildNumber = Integer.parseInt(lastBuildNumberStr);
                } catch (NumberFormatException e) {
                    logger.warn(WARN_MSG_INVALID_VALUE, lastBuildNumberStr, key);
                }
            }
            if (lastBuildNumber != null && lastBuildNumber >= j.getMinBuildNumber()) {
                properties.setProperty(key, lastBuildNumber.toString());
            } else {
                properties.setProperty(key, LAST_BUILD_NUMBER_DEFAULT);
            }
        }
    }

    protected void initMaxHeapSize() {
        final String maxHeapSizeStr = properties.getProperty(PROPERTY_MAX_HEAP_SIZE);
        JavaHeapSize maxJavaHeapSize = MAX_HEAP_SIZE_DEFAULT;
        if (maxHeapSizeStr != null) {
            try {
                maxJavaHeapSize = JavaHeapSize.valueOf(maxHeapSizeStr);
            } catch (IllegalArgumentException e) {
                logger.warn(WARN_MSG_INVALID_VALUE, maxHeapSizeStr, PROPERTY_MAX_HEAP_SIZE);
            }
        }
        properties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxJavaHeapSize.name());
    }

    protected void initInitialHeapSize() {
        final String initialHeapSizeStr = properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE);
        JavaHeapSize initialJavaHeapSize = INITIAL_HEAP_SIZE_DEFAULT;
        if (initialHeapSizeStr != null) {
            try {
                initialJavaHeapSize = JavaHeapSize.valueOf(initialHeapSizeStr);
            } catch (IllegalArgumentException e) {
                logger.warn(WARN_MSG_INVALID_VALUE, initialHeapSizeStr, PROPERTY_INITIAL_HEAP_SIZE);
            }
        }
        properties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialJavaHeapSize.name());
    }

    protected void initUserJavaParameters() {
        final String userJavaParsStr = properties.getProperty(PROPERTY_USER_JAVA_PARAMETERS);
        if (userJavaParsStr == null || userJavaParsStr.isEmpty()) {
            properties.setProperty(PROPERTY_USER_JAVA_PARAMETERS, USER_JAVA_PARAMETERS_DEFAULT);
        }
    }

    protected void initUserGameParameters() {
        final String userJavaParsStr = properties.getProperty(PROPERTY_USER_GAME_PARAMETERS);
        if (userJavaParsStr == null || userJavaParsStr.isEmpty()) {
            properties.setProperty(PROPERTY_USER_GAME_PARAMETERS, USER_GAME_PARAMETERS_DEFAULT);
        }
    }

    protected void initLogLevel() {
        final String logLevelStr = properties.getProperty(PROPERTY_LOG_LEVEL);
        LogLevel logLevel = LOG_LEVEL_DEFAULT;
        if (logLevelStr != null) {
            try {
                logLevel = LogLevel.valueOf(logLevelStr);
            } catch (IllegalArgumentException e) {
                logger.warn(WARN_MSG_INVALID_VALUE, logLevelStr, PROPERTY_LOG_LEVEL);
            }
        }
        properties.setProperty(PROPERTY_LOG_LEVEL, logLevel.name());
    }

    protected void initSearchForLauncherUpdates() {
        final String searchForLauncherUpdatesStr = properties.getProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES);
        boolean searchForLauncherUpdates = SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT;
        if (searchForLauncherUpdatesStr != null) {
            searchForLauncherUpdates = Boolean.valueOf(searchForLauncherUpdatesStr);
        }
        properties.setProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES, Boolean.toString(searchForLauncherUpdates));
    }

    protected void initCloseLauncherAfterGameStart() {
        final String closeLauncherAfterGameStartStr = properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START);
        boolean closeLauncherAfterGameStart = CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT;
        if (closeLauncherAfterGameStartStr != null) {
            closeLauncherAfterGameStart = Boolean.valueOf(closeLauncherAfterGameStartStr);
        }
        properties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, Boolean.toString(closeLauncherAfterGameStart));
    }

    protected void initSaveDownloadedFiles() {
        final String saveDownloadedFilesStr = properties.getProperty(PROPERTY_SAVE_DOWNLOADED_FILES);
        boolean saveDownloadedFiles = SAVE_DOWNLOADED_FILES_DEFAULT;
        if (saveDownloadedFilesStr != null) {
            saveDownloadedFiles = Boolean.valueOf(saveDownloadedFilesStr);
        }
        properties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, Boolean.toString(saveDownloadedFiles));
    }

    protected void initGameDirectory() {
        final String gameDirectoryStr = properties.getProperty(PROPERTY_GAME_DIRECTORY);
        Path gameDirectory = null;
        if (gameDirectoryStr != null && gameDirectoryStr.trim().length() > 0) {
            try {
                gameDirectory = Paths.get(new URI(gameDirectoryStr));
            } catch (URISyntaxException | RuntimeException e) {
                logger.warn(WARN_MSG_INVALID_VALUE, gameDirectoryStr, PROPERTY_GAME_DIRECTORY);
            }
        }
        if (gameDirectory != null) {
            properties.setProperty(PROPERTY_GAME_DIRECTORY, gameDirectory.toUri().toString());
        } else {
            properties.setProperty(PROPERTY_GAME_DIRECTORY, "");
        }
    }

    protected void initGameDataDirectory() {
        final String gameDataDirectoryStr = properties.getProperty(PROPERTY_GAME_DATA_DIRECTORY);
        Path gameDataDirectory = null;
        if (gameDataDirectoryStr != null && gameDataDirectoryStr.trim().length() > 0) {
            try {
                gameDataDirectory = Paths.get(new URI(gameDataDirectoryStr));
            } catch (URISyntaxException | RuntimeException e) {
                logger.warn(WARN_MSG_INVALID_VALUE, gameDataDirectoryStr, PROPERTY_GAME_DATA_DIRECTORY);
            }
        }
        if (gameDataDirectory != null) {
            properties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, gameDataDirectory.toUri().toString());
        } else {
            properties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, "");
        }
    }

    // --------------------------------------------------------------------- //
    // GETTERS
    // --------------------------------------------------------------------- //

    @Override
    public synchronized Locale getLocale() {
        return Locale.forLanguageTag(properties.getProperty(PROPERTY_LOCALE));
    }

    @Override
    public synchronized GameJob getJob() {
        return GameJob.valueOf(properties.getProperty(PROPERTY_JOB));
    }

    @Override
    public synchronized Integer getBuildVersion(GameJob job) {
        return Integer.parseInt(properties.getProperty(PROPERTY_PREFIX_BUILD_VERSION + job.name()));
    }

    @Override
    public synchronized Integer getLastBuildNumber(GameJob job) {
        final String lastBuildNumberStr = properties.getProperty(PROPERTY_PREFIX_LAST_BUILD_NUMBER + job.name());
        if (LAST_BUILD_NUMBER_DEFAULT.equals(lastBuildNumberStr)) {
            return null;
        }
        return Integer.parseInt(lastBuildNumberStr);
    }

    @Override
    public synchronized JavaHeapSize getMaxHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_MAX_HEAP_SIZE));
    }

    @Override
    public synchronized JavaHeapSize getInitialHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE));
    }

    @Override
    public synchronized String getUserJavaParameters() {
        return properties.getProperty(PROPERTY_USER_JAVA_PARAMETERS);
    }

    @Override
    public synchronized String getUserGameParameters() {
        return properties.getProperty(PROPERTY_USER_GAME_PARAMETERS);
    }

    @Override
    public synchronized LogLevel getLogLevel() {
        return LogLevel.valueOf(properties.getProperty(PROPERTY_LOG_LEVEL));
    }

    @Override
    public synchronized Path getGameDirectory() {
        final String gameDirectoryStr = properties.getProperty(PROPERTY_GAME_DIRECTORY);
        if (gameDirectoryStr != null && gameDirectoryStr.trim().length() > 0) {
            try {
                return Paths.get(new URI(gameDirectoryStr));
            } catch (URISyntaxException | RuntimeException e) {
                logger.error(WARN_MSG_INVALID_VALUE, gameDirectoryStr, PROPERTY_GAME_DIRECTORY);
            }
        }
        return null;
    }

    @Override
    public synchronized Path getGameDataDirectory() {
        final String gameDataDirectoryStr = properties.getProperty(PROPERTY_GAME_DATA_DIRECTORY);
        if (gameDataDirectoryStr != null && gameDataDirectoryStr.trim().length() > 0) {
            try {
                return Paths.get(new URI(gameDataDirectoryStr));
            } catch (URISyntaxException | RuntimeException e) {
                logger.error(WARN_MSG_INVALID_VALUE, gameDataDirectoryStr, PROPERTY_GAME_DATA_DIRECTORY);
            }
        }
        return null;
    }

    @Override
    public synchronized boolean isSearchForLauncherUpdates() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES));
    }

    @Override
    public synchronized boolean isCloseLauncherAfterGameStart() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START));
    }

    @Override
    public synchronized boolean isKeepDownloadedFiles() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_SAVE_DOWNLOADED_FILES));
    }

    // --------------------------------------------------------------------- //
    // SETTERS
    // --------------------------------------------------------------------- //

    @Override
    public synchronized void setLocale(Locale locale) {
        properties.setProperty(PROPERTY_LOCALE, locale.toString());
    }

    @Override
    public synchronized void setJob(GameJob job) {
        properties.setProperty(PROPERTY_JOB, job.name());
    }

    @Override
    public synchronized void setBuildVersion(int version, GameJob job) {
        properties.setProperty(PROPERTY_PREFIX_BUILD_VERSION + job.name(), String.valueOf(version));
    }

    @Override
    public synchronized void setLastBuildNumber(Integer lastBuildNumber, GameJob job) {
        if (lastBuildNumber != null && lastBuildNumber >= job.getMinBuildNumber()) {
            properties.setProperty(PROPERTY_PREFIX_LAST_BUILD_NUMBER + job.name(), lastBuildNumber.toString());
        } else {
            properties.setProperty(PROPERTY_PREFIX_LAST_BUILD_NUMBER + job.name(), LAST_BUILD_NUMBER_DEFAULT);
        }
    }

    @Override
    public synchronized void setMaxHeapSize(JavaHeapSize maxHeapSize) {
        properties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxHeapSize.name());
    }

    @Override
    public synchronized void setInitialHeapSize(JavaHeapSize initialHeapSize) {
        properties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialHeapSize.name());
    }

    @Override
    public synchronized void setUserJavaParameters(String userJavaParameters) {
        properties.setProperty(PROPERTY_USER_JAVA_PARAMETERS, userJavaParameters);
    }

    @Override
    public synchronized void setUserGameParameters(String userGameParameters) {
        properties.setProperty(PROPERTY_USER_GAME_PARAMETERS, userGameParameters);
    }

    @Override
    public synchronized void setLogLevel(LogLevel logLevel) {
        properties.setProperty(PROPERTY_LOG_LEVEL, logLevel.name());
    }

    @Override
    public synchronized void setSearchForLauncherUpdates(boolean searchForLauncherUpdates) {
        properties.setProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES, Boolean.toString(searchForLauncherUpdates));
    }

    @Override
    public synchronized void setCloseLauncherAfterGameStart(boolean closeLauncherAfterGameStart) {
        properties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, Boolean.toString(closeLauncherAfterGameStart));
    }

    @Override
    public synchronized void setKeepDownloadedFiles(boolean keepDownloadedFiles) {
        properties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, Boolean.toString(keepDownloadedFiles));
    }

    @Override
    public synchronized void setGameDirectory(Path gameDirectory) {
        properties.setProperty(PROPERTY_GAME_DIRECTORY, gameDirectory.toUri().toString());
    }

    @Override
    public synchronized void setGameDataDirectory(Path gameDataDirectory) {
        properties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, gameDataDirectory.toUri().toString());
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + properties.toString() + "]";
    }
}
