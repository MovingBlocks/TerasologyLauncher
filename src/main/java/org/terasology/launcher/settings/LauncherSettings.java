// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.util.I18N;
import org.terasology.launcher.util.JavaHeapSize;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * User settings for the launcher, backed by Java {@link Properties}.
 */
@SuppressWarnings("checkstyle:DeclarationOrder")
public class LauncherSettings {

    public static final String USER_JAVA_PARAMETERS_DEFAULT = "-XX:MaxGCPauseMillis=20";
    public static final String USER_GAME_PARAMETERS_DEFAULT = "";

    public static final String PROPERTY_LOCALE = "locale";
    public static final String PROPERTY_MAX_HEAP_SIZE = "maxHeapSize";
    public static final String PROPERTY_INITIAL_HEAP_SIZE = "initialHeapSize";
    public static final String PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START = "closeLauncherAfterGameStart";
    public static final String PROPERTY_GAME_DIRECTORY = "gameDirectory";
    public static final String PROPERTY_GAME_DATA_DIRECTORY = "gameDataDirectory";
    public static final String PROPERTY_SAVE_DOWNLOADED_FILES = "saveDownloadedFiles";
    public static final String PROPERTY_SHOW_PRE_RELEASES = "showPreReleases";
    public static final String PROPERTY_BASE_JAVA_PARAMETERS = "baseJavaParameters";
    public static final String PROPERTY_USER_JAVA_PARAMETERS = "userJavaParameters";
    public static final String PROPERTY_USER_GAME_PARAMETERS = "userGameParameters";
    public static final String PROPERTY_LOG_LEVEL = "logLevel";
    public static final String PROPERTY_DEFAULT_GAME_JOB = "defaultGameJob";
    public static final String PROPERTY_LAST_PLAYED_GAME_VERSION = "lastPlayedGameVersion";
    public static final String PROPERTY_LAST_INSTALLED_GAME_JOB = "lastInstalledGameJob";
    public static final String PROPERTY_LAST_INSTALLED_GAME_VERSION = "lastInstalledGameVersion";

    private static final Logger logger = LoggerFactory.getLogger(LauncherSettings.class);

    private static final String WARN_MSG_INVALID_VALUE = "Invalid value '{}' for the parameter '{}'!";
    private static final Level LOG_LEVEL_DEFAULT = Level.INFO;

    static final JavaHeapSize MAX_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    static final JavaHeapSize INITIAL_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    static final boolean CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT = true;
    static final boolean SAVE_DOWNLOADED_FILES_DEFAULT = false;
    static final boolean SHOW_PRE_RELEASES_DEFAULT = false;
    static final String LAST_PLAYED_GAME_VERSION_DEFAULT = "";
    static final String LAST_INSTALLED_GAME_VERSION_DEFAULT = "";

    static final String LAUNCHER_LEGACY_SETTINGS_FILE_NAME = "TerasologyLauncherSettings.properties";

    private final Properties properties;

    private final Property<Boolean> showPreReleases = new SimpleBooleanProperty(SHOW_PRE_RELEASES_DEFAULT);

    LauncherSettings(Properties properties) {
        this.properties = properties;
    }

    Properties getProperties() {
        return properties;
    }

    // --------------------------------------------------------------------- //
    // INIT
    // --------------------------------------------------------------------- //

    public synchronized void init() {
        logger.trace("Init launcher settings ...");

        initLocale();
        initMaxHeapSize();
        initInitialHeapSize();
        initBaseJavaParameters();
        initCloseLauncherAfterGameStart();
        initSaveDownloadedFiles();
        initShowPreReleases();
        initGameDirectory();
        initGameDataDirectory();
        initUserJavaParameters();
        initUserGameParameters();
        initLogLevel();
        initLastPlayedGameVersion();
        initLastInstalledGameVersion();
    }

    void initLocale() {
        final String localeStr = properties.getProperty(PROPERTY_LOCALE);
        if (localeStr != null) {
            Locale l = I18N.getSupportedLocales().stream()
                    .filter(x -> x.toLanguageTag().equals(localeStr))
                    .findFirst().orElse(I18N.getDefaultLocale());
            I18N.setLocale(l);

            if (!I18N.getCurrentLocale().toString().equals(localeStr)) {
                logger.warn(WARN_MSG_INVALID_VALUE, localeStr, PROPERTY_LOCALE);
            }
        }
        properties.setProperty(PROPERTY_LOCALE, I18N.getCurrentLocale().toString());
    }

    void initMaxHeapSize() {
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

    void initInitialHeapSize() {
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

    void initBaseJavaParameters() {
    }

    void initUserJavaParameters() {
        final String userJavaParsStr = properties.getProperty(PROPERTY_USER_JAVA_PARAMETERS);
        if (userJavaParsStr == null || userJavaParsStr.isEmpty()) {
            properties.setProperty(PROPERTY_USER_JAVA_PARAMETERS, USER_JAVA_PARAMETERS_DEFAULT);
        }
    }

    void initUserGameParameters() {
        final String userJavaParsStr = properties.getProperty(PROPERTY_USER_GAME_PARAMETERS);
        if (userJavaParsStr == null || userJavaParsStr.isEmpty()) {
            properties.setProperty(PROPERTY_USER_GAME_PARAMETERS, USER_GAME_PARAMETERS_DEFAULT);
        }
    }

    void initLogLevel() {
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

    void initCloseLauncherAfterGameStart() {
        final String closeLauncherAfterGameStartStr = properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START);
        boolean closeLauncherAfterGameStart = CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT;
        if (closeLauncherAfterGameStartStr != null) {
            closeLauncherAfterGameStart = Boolean.valueOf(closeLauncherAfterGameStartStr);
        }
        properties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, Boolean.toString(closeLauncherAfterGameStart));
    }

    void initSaveDownloadedFiles() {
        final String saveDownloadedFilesStr = properties.getProperty(PROPERTY_SAVE_DOWNLOADED_FILES);
        boolean saveDownloadedFiles = SAVE_DOWNLOADED_FILES_DEFAULT;
        if (saveDownloadedFilesStr != null) {
            saveDownloadedFiles = Boolean.valueOf(saveDownloadedFilesStr);
        }
        properties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, Boolean.toString(saveDownloadedFiles));
    }

    void initShowPreReleases() {
        final String showPreReleasesStr = properties.getProperty(PROPERTY_SHOW_PRE_RELEASES);
        if (showPreReleasesStr != null) {
            setShowPreReleases(Boolean.parseBoolean(showPreReleasesStr));
        } else {
            setShowPreReleases(SHOW_PRE_RELEASES_DEFAULT);
        }
    }

    void initGameDirectory() {
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

    void initGameDataDirectory() {
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

    void initLastPlayedGameVersion() {
        final String lastPlayedGameVersionStr = properties.getProperty(PROPERTY_LAST_PLAYED_GAME_VERSION);
        if (lastPlayedGameVersionStr == null || lastPlayedGameVersionStr.isEmpty()) {
            properties.setProperty(PROPERTY_LAST_PLAYED_GAME_VERSION, LAST_PLAYED_GAME_VERSION_DEFAULT);
        }
    }

    void initLastInstalledGameVersion() {
        final String lastInstalledGameVersionStr = properties.getProperty(PROPERTY_LAST_INSTALLED_GAME_VERSION);
        if (lastInstalledGameVersionStr == null || lastInstalledGameVersionStr.isEmpty()) {
            properties.setProperty(PROPERTY_LAST_INSTALLED_GAME_VERSION, LAST_INSTALLED_GAME_VERSION_DEFAULT);
        }
    }

    // --------------------------------------------------------------------- //
    // GETTERS
    // --------------------------------------------------------------------- //

    public synchronized Locale getLocale() {
        return Locale.forLanguageTag(properties.getProperty(PROPERTY_LOCALE));
    }

    public synchronized JavaHeapSize getMaxHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_MAX_HEAP_SIZE));
    }

    public synchronized JavaHeapSize getInitialHeapSize() {
        return JavaHeapSize.valueOf(properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE));
    }

    public synchronized String getBaseJavaParameters() {
        return properties.getProperty(PROPERTY_BASE_JAVA_PARAMETERS);
    }

    public synchronized String getUserJavaParameters() {
        return properties.getProperty(PROPERTY_USER_JAVA_PARAMETERS);
    }

    public synchronized String getUserGameParameters() {
        return properties.getProperty(PROPERTY_USER_GAME_PARAMETERS);
    }

    public synchronized Level getLogLevel() {
        return Level.valueOf(properties.getProperty(PROPERTY_LOG_LEVEL));
    }

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

    public synchronized boolean isCloseLauncherAfterGameStart() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START));
    }

    public synchronized boolean isKeepDownloadedFiles() {
        return Boolean.valueOf(properties.getProperty(PROPERTY_SAVE_DOWNLOADED_FILES));
    }

    public synchronized boolean isShowPreReleases() {
        return Boolean.parseBoolean(properties.getProperty(PROPERTY_SHOW_PRE_RELEASES));
    }

    public synchronized Optional<GameIdentifier> getLastPlayedGameVersion() {
        String property = properties.getProperty(PROPERTY_LAST_PLAYED_GAME_VERSION);
        if (property == null || property.isEmpty()) {
            return Optional.empty();
        }
        GameIdentifier id;
        try {
            id = new Gson().fromJson(property, GameIdentifier.class);
        } catch (JsonParseException e) {
            logger.warn("Failed to parse a game version from \"{}\". This should automatically resolve when starting a game.", property, e);
            return Optional.empty();
        }
        return Optional.ofNullable(id);
    }

    public synchronized String getLastInstalledGameJob() {
        return properties.getProperty(PROPERTY_LAST_INSTALLED_GAME_JOB);
    }

    public synchronized List<String> getJavaParameterList() {
        List<String> javaParameters = Lists.newArrayList();
        String baseParams = getBaseJavaParameters();
        if (baseParams != null) {
            javaParameters.addAll(Arrays.asList(baseParams.split("\\s+")));
        }

        String userParams = getUserJavaParameters();
        if (userParams != null) {
            javaParameters.addAll(Arrays.asList(userParams.split("\\s+")));
        }

        return javaParameters;
    }

    public synchronized List<String> getUserGameParameterList() {
        return Arrays.stream(getUserGameParameters().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }


    // --------------------------------------------------------------------- //
    // SETTERS
    // --------------------------------------------------------------------- //

    public synchronized void setLocale(Locale locale) {
        properties.setProperty(PROPERTY_LOCALE, locale.toString());
    }

    public synchronized void setMaxHeapSize(JavaHeapSize maxHeapSize) {
        properties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxHeapSize.name());
    }

    public synchronized void setInitialHeapSize(JavaHeapSize initialHeapSize) {
        properties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialHeapSize.name());
    }

    public synchronized void setUserJavaParameters(String userJavaParameters) {
        properties.setProperty(PROPERTY_USER_JAVA_PARAMETERS, userJavaParameters);
    }

    public synchronized void setUserGameParameters(String userGameParameters) {
        properties.setProperty(PROPERTY_USER_GAME_PARAMETERS, userGameParameters);
    }

    public synchronized void setLogLevel(Level logLevel) {
        properties.setProperty(PROPERTY_LOG_LEVEL, logLevel.name());
    }

    public synchronized void setCloseLauncherAfterGameStart(boolean closeLauncherAfterGameStart) {
        properties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, Boolean.toString(closeLauncherAfterGameStart));
    }

    public synchronized void setKeepDownloadedFiles(boolean keepDownloadedFiles) {
        properties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, Boolean.toString(keepDownloadedFiles));
    }

    public synchronized void setShowPreReleases(boolean selected) {
        showPreReleases.setValue(selected);
        properties.setProperty(PROPERTY_SHOW_PRE_RELEASES, Boolean.toString(selected));
    }

    public synchronized void setGameDirectory(Path gameDirectory) {
        properties.setProperty(PROPERTY_GAME_DIRECTORY, gameDirectory.toUri().toString());
    }

    public synchronized void setGameDataDirectory(Path gameDataDirectory) {
        properties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, gameDataDirectory.toUri().toString());
    }

    public synchronized void setDefaultGameJob(String defaultGameJob) {
        properties.setProperty(PROPERTY_DEFAULT_GAME_JOB, defaultGameJob);
    }

    public synchronized void setLastPlayedGameVersion(GameIdentifier lastPlayedGameVersion) {
        if (lastPlayedGameVersion == null) {
            properties.setProperty(PROPERTY_LAST_PLAYED_GAME_VERSION, "");
        } else {
            properties.setProperty(PROPERTY_LAST_PLAYED_GAME_VERSION, new Gson().toJson(lastPlayedGameVersion));
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + properties.toString() + "]";
    }
}
