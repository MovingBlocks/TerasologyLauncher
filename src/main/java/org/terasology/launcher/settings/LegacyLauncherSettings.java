// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * User settings for the launcher, backed by Java {@link Properties}.
 */
public class LegacyLauncherSettings {

    private static final Logger logger = LoggerFactory.getLogger(LegacyLauncherSettings.class);

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
    public static final String PROPERTY_LAST_PLAYED_GAME_VERSION = "lastPlayedGameVersion";
    public static final String PROPERTY_LAST_INSTALLED_GAME_VERSION = "lastInstalledGameVersion";

    static final JavaHeapSize MAX_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    static final JavaHeapSize INITIAL_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    static final boolean CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT = true;
    static final String LAUNCHER_SETTINGS_FILE_NAME = "TerasologyLauncherSettings.properties";

    private static final String WARN_MSG_INVALID_VALUE = "Invalid value '{}' for the parameter '{}'!";

    private final Properties properties;

    LegacyLauncherSettings(Properties properties) {
        this.properties = properties;
    }

    // --------------------------------------------------------------------- //
    // INIT
    // --------------------------------------------------------------------- //

    public synchronized void init() {
        logger.trace("Init launcher settings ...");
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
        return Boolean.parseBoolean(properties.getProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START));
    }

    public synchronized boolean isKeepDownloadedFiles() {
        return Boolean.parseBoolean(properties.getProperty(PROPERTY_SAVE_DOWNLOADED_FILES));
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

    public synchronized void setGameDirectory(Path gameDirectory) {
        properties.setProperty(PROPERTY_GAME_DIRECTORY, gameDirectory.toUri().toString());
    }

    public synchronized void setGameDataDirectory(Path gameDataDirectory) {
        properties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, gameDataDirectory.toUri().toString());
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
