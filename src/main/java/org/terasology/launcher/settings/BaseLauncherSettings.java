// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * Provides access to launcher settings.
 */
public final class BaseLauncherSettings extends LauncherSettings {

    public static final String USER_JAVA_PARAMETERS_DEFAULT = "-XX:MaxGCPauseMillis=20";
    public static final String USER_GAME_PARAMETERS_DEFAULT = "";

    public static final String PROPERTY_LOCALE = "locale";
    public static final String PROPERTY_MAX_HEAP_SIZE = "maxHeapSize";
    public static final String PROPERTY_INITIAL_HEAP_SIZE = "initialHeapSize";
    public static final String PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START = "closeLauncherAfterGameStart";
    public static final String PROPERTY_GAME_DIRECTORY = "gameDirectory";
    public static final String PROPERTY_GAME_DATA_DIRECTORY = "gameDataDirectory";
    public static final String PROPERTY_SAVE_DOWNLOADED_FILES = "saveDownloadedFiles";
    public static final String PROPERTY_BASE_JAVA_PARAMETERS = "baseJavaParameters";
    public static final String PROPERTY_USER_JAVA_PARAMETERS = "userJavaParameters";
    public static final String PROPERTY_USER_GAME_PARAMETERS = "userGameParameters";
    public static final String PROPERTY_LOG_LEVEL = "logLevel";
    public static final String PROPERTY_DEFAULT_GAME_JOB = "defaultGameJob";
    public static final String PROPERTY_LAST_PLAYED_GAME_JOB = "lastPlayedGameJob";
    public static final String PROPERTY_LAST_PLAYED_GAME_VERSION = "lastPlayedGameVersion";
    public static final String PROPERTY_LAST_INSTALLED_GAME_JOB = "lastInstalledGameJob";
    public static final String PROPERTY_LAST_INSTALLED_GAME_VERSION = "lastInstalledGameVersion";

    public static final JavaHeapSize MAX_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    public static final JavaHeapSize INITIAL_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    public static final boolean SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT = true;
    public static final boolean CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT = true;
    public static final boolean SAVE_DOWNLOADED_FILES_DEFAULT = false;
    public static final String DEFAULT_GAME_JOB_DEFAULT = "DistroOmegaRelease";
    public static final String LAST_PLAYED_GAME_JOB_DEFAULT = "";
    public static final String LAST_PLAYED_GAME_VERSION_DEFAULT = "";
    public static final String LAST_INSTALLED_GAME_JOB_DEFAULT = "";
    public static final String LAST_INSTALLED_GAME_VERSION_DEFAULT = "";

    public static final String LAUNCHER_SETTINGS_FILE_NAME = "TerasologyLauncherSettings.properties";

    private static final Logger logger = LoggerFactory.getLogger(BaseLauncherSettings.class);


    private static final String WARN_MSG_INVALID_VALUE = "Invalid value '{}' for the parameter '{}'!";
    private static final Level LOG_LEVEL_DEFAULT = Level.INFO;

    private final Properties properties;

    BaseLauncherSettings(Properties properties) {
        this.properties = properties;
    }

    @Override
    Properties getProperties() {
        return properties;
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

    protected void initBaseJavaParameters() {
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
        Level logLevel = LOG_LEVEL_DEFAULT;
        if (logLevelStr != null) {
            try {
                logLevel = Level.valueOf(logLevelStr);
            } catch (IllegalArgumentException e) {
                logger.warn(WARN_MSG_INVALID_VALUE, logLevelStr, PROPERTY_LOG_LEVEL);
            }
        }
        properties.setProperty(PROPERTY_LOG_LEVEL, logLevel.name());
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

    protected void initDefaultGameJob() {
        final String defaultGameJobStr = properties.getProperty(PROPERTY_DEFAULT_GAME_JOB);
        if (defaultGameJobStr == null || defaultGameJobStr.isEmpty()) {
            properties.setProperty(PROPERTY_DEFAULT_GAME_JOB, DEFAULT_GAME_JOB_DEFAULT);
        }
    }

    protected void initLastPlayedGameJob() {
        final String lastPlayedGameJobStr = properties.getProperty(PROPERTY_LAST_PLAYED_GAME_JOB);
        if (lastPlayedGameJobStr == null || lastPlayedGameJobStr.isEmpty()) {
            properties.setProperty(PROPERTY_LAST_PLAYED_GAME_JOB, LAST_PLAYED_GAME_JOB_DEFAULT);
        }
    }

    protected void initLastPlayedGameVersion() {
        final String lastPlayedGameVersionStr = properties.getProperty(PROPERTY_LAST_PLAYED_GAME_VERSION);
        if (lastPlayedGameVersionStr == null || lastPlayedGameVersionStr.isEmpty()) {
            properties.setProperty(PROPERTY_LAST_PLAYED_GAME_VERSION, LAST_PLAYED_GAME_VERSION_DEFAULT);
        }
    }

    protected void initLastInstalledGameJob() {
        final String lastInstalledGameJobStr = properties.getProperty(PROPERTY_LAST_INSTALLED_GAME_JOB);
        if (lastInstalledGameJobStr == null || lastInstalledGameJobStr.isEmpty()) {
            properties.setProperty(PROPERTY_LAST_INSTALLED_GAME_JOB, LAST_INSTALLED_GAME_JOB_DEFAULT);
        }
    }

    protected void initLastInstalledGameVersion() {
        final String lastInstalledGameVersionStr = properties.getProperty(PROPERTY_LAST_INSTALLED_GAME_VERSION);
        if (lastInstalledGameVersionStr == null || lastInstalledGameVersionStr.isEmpty()) {
            properties.setProperty(PROPERTY_LAST_INSTALLED_GAME_VERSION, LAST_INSTALLED_GAME_VERSION_DEFAULT);
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
    public synchronized JavaHeapSize getMaxHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_MAX_HEAP_SIZE));
    }

    @Override
    public synchronized JavaHeapSize getInitialHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE));
    }

    @Override
    public synchronized String getBaseJavaParameters() {
        return properties.getProperty(PROPERTY_BASE_JAVA_PARAMETERS);
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
    public synchronized Level getLogLevel() {
        return Level.valueOf(properties.getProperty(PROPERTY_LOG_LEVEL));
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
    public synchronized boolean isCloseLauncherAfterGameStart() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START));
    }

    @Override
    public synchronized boolean isKeepDownloadedFiles() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_SAVE_DOWNLOADED_FILES));
    }

    @Override
    public synchronized String getDefaultGameJob() {
        return properties.getProperty(PROPERTY_DEFAULT_GAME_JOB);
    }

    @Override
    public synchronized String getLastPlayedGameJob() {
        return properties.getProperty(PROPERTY_LAST_PLAYED_GAME_JOB);
    }

    @Override
    public synchronized Optional<GameIdentifier> getLastPlayedGameVersion() {
        String property = properties.getProperty(PROPERTY_LAST_PLAYED_GAME_VERSION);
        return Optional.ofNullable(GameIdentifier.fromString(property));
    }

    @Override
    public synchronized String getLastInstalledGameJob() {
        return properties.getProperty(PROPERTY_LAST_INSTALLED_GAME_JOB);
    }

    @Override
    public synchronized Optional<GameIdentifier> getLastInstalledGameVersion() {
        String property = properties.getProperty(PROPERTY_LAST_INSTALLED_GAME_VERSION);
        return Optional.ofNullable(GameIdentifier.fromString(property));
    }

    // --------------------------------------------------------------------- //
    // SETTERS
    // --------------------------------------------------------------------- //

    @Override
    public synchronized void setLocale(Locale locale) {
        properties.setProperty(PROPERTY_LOCALE, locale.toString());
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
    public synchronized void setLogLevel(Level logLevel) {
        properties.setProperty(PROPERTY_LOG_LEVEL, logLevel.name());
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
    public synchronized void setDefaultGameJob(String defaultGameJob) {
        properties.setProperty(PROPERTY_DEFAULT_GAME_JOB, defaultGameJob);
    }

    @Override
    public synchronized void setLastPlayedGameJob(String lastPlayedGameJob) {
        properties.setProperty(PROPERTY_LAST_PLAYED_GAME_JOB, lastPlayedGameJob);
    }

    @Override
    public synchronized void setLastPlayedGameVersion(GameIdentifier lastPlayedGameVersion) {
        if (lastPlayedGameVersion == null) {
            properties.setProperty(PROPERTY_LAST_PLAYED_GAME_VERSION, "");
        } else {
            properties.setProperty(PROPERTY_LAST_PLAYED_GAME_VERSION, lastPlayedGameVersion.toString());
        }
    }

    @Override
    public synchronized void setLastInstalledGameJob(String lastInstalledGameJob) {
        properties.setProperty(PROPERTY_LAST_INSTALLED_GAME_JOB, lastInstalledGameJob);
    }

    @Override
    public synchronized void setLastInstalledGameVersion(GameIdentifier lastInstalledGameVersion) {
        if (lastInstalledGameVersion == null) {
            properties.setProperty(PROPERTY_LAST_INSTALLED_GAME_VERSION, "");
        } else {
            properties.setProperty(PROPERTY_LAST_INSTALLED_GAME_VERSION, lastInstalledGameVersion.toString());
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + properties.toString() + "]";
    }
}
