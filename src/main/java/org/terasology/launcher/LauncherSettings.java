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

package org.terasology.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.version.GameJob;
import org.terasology.launcher.version.TerasologyGameVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;

/**
 * Provides access to launcher settings.
 */
public final class LauncherSettings {

    private static final Logger logger = LoggerFactory.getLogger(LauncherSettings.class);

    private static final String LAUNCHER_SETTINGS_FILE_NAME = "TerasologyLauncherSettings.properties";
    private static final String COMMENT_SETTINGS = "Terasology Launcher - Settings";

    private static final GameJob JOB_DEFAULT = GameJob.TerasologyStable;
    private static final JavaHeapSize MAX_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    private static final JavaHeapSize INITIAL_HEAP_SIZE_DEFAULT = JavaHeapSize.NOT_USED;
    private static final boolean SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT = true;
    private static final boolean CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT = true;

    private static final String PROPERTY_LOCALE = "locale";
    private static final String PROPERTY_JOB = "job";
    private static final String PROPERTY_MAX_HEAP_SIZE = "maxHeapSize";
    private static final String PROPERTY_INITIAL_HEAP_SIZE = "initialHeapSize";
    private static final String PROPERTY_PREFIX_BUILD_VERSION = "buildVersion_";
    private static final String PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES = "searchForLauncherUpdates";
    private static final String PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START = "closeLauncherAfterGameStart";
    private static final String PROPERTY_GAMES_DIRECTORY = "gameDirectory";
    private static final String PROPERTY_PROXY_HOST = "proxyHost";
    private static final String PROPERTY_PROXY_PORT = "proxyPort";
    private static final String PROPERTY_PROXY_USERNAME = "proxyUsername";
    private static final String PROPERTY_PROXY_PASSWORD = "proxyPassword";

    private final File launcherSettingsFile;
    private final Properties properties;

    public LauncherSettings(final File launcherDirectory) {
        launcherSettingsFile = new File(launcherDirectory, LAUNCHER_SETTINGS_FILE_NAME);
        properties = new Properties();
    }

    public String getLauncherSettingsFilePath() {
        return launcherSettingsFile.getPath();
    }

    public synchronized void load() throws IOException {
        if (launcherSettingsFile.exists()) {
            logger.trace("Load the launcher settings from the file '{}'.", launcherSettingsFile);

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
            throw new IOException("The directory could not be created. " + launcherSettingsFile.getParentFile());
        }

        // store settings
        try (OutputStream outputStream = new FileOutputStream(launcherSettingsFile)) {
            properties.store(outputStream, COMMENT_SETTINGS);
        }
    }

    public synchronized void init() {
        logger.trace("Init launcher settings. {}", properties);

        // locale
        final String localeStr = properties.getProperty(PROPERTY_LOCALE);
        if (localeStr != null) {
            Languages.init(localeStr);

            if (!Languages.getCurrentLocale().toString().equals(localeStr)) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", localeStr, PROPERTY_LOCALE);
            }
        }
        properties.setProperty(PROPERTY_LOCALE, Languages.getCurrentLocale().toString());

        // jobName
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

        // buildVersion
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

        // max heap size
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

        // initial heap size
        final String initialHeapSizeStr = properties.getProperty(PROPERTY_INITIAL_HEAP_SIZE);
        JavaHeapSize initialJavaHeapSize = INITIAL_HEAP_SIZE_DEFAULT;
        if (initialHeapSizeStr != null) {
            try {
                initialJavaHeapSize = JavaHeapSize.valueOf(initialHeapSizeStr);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", initialHeapSizeStr,
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

        // gamesDirectory
        final String gamesDirectoryStr = properties.getProperty(PROPERTY_GAMES_DIRECTORY);
        File gamesDirectory = null;
        if ((gamesDirectoryStr != null) && (gamesDirectoryStr.trim().length() > 0)) {
            try {
                gamesDirectory = new File(new URI(gamesDirectoryStr));
            } catch (Exception e) {
                logger.warn("Invalid value '{}' for the parameter '{}'!", gamesDirectoryStr, PROPERTY_GAMES_DIRECTORY);
            }
        }
        if (gamesDirectory != null) {
            properties.setProperty(PROPERTY_GAMES_DIRECTORY, gamesDirectory.toURI().toString());
        } else {
            properties.setProperty(PROPERTY_GAMES_DIRECTORY, "");
        }

        // proxy stuff
		String proxyHost = properties.getProperty(PROPERTY_PROXY_HOST);
		if (proxyHost == null) {
			proxyHost = System.getProperty("http.proxyHost");
        }
		if (proxyHost == null) {
			proxyHost = "";
        }
		properties.setProperty(PROPERTY_PROXY_HOST, proxyHost);
		String proxyPort = properties.getProperty(PROPERTY_PROXY_PORT);
		if (proxyPort == null) {
			proxyPort = System.getProperty("http.proxyPort");
		}
		if (proxyPort == null) {
			proxyPort = "";
        }
		properties.setProperty(PROPERTY_PROXY_PORT, proxyPort);
    }

    public synchronized void setLocale(final Locale locale) {
        properties.setProperty(PROPERTY_LOCALE, locale.toString());
    }

    public synchronized void setJob(final GameJob job) {
        properties.setProperty(PROPERTY_JOB, job.name());
    }

    public synchronized void setProxyHost(final String proxyHost) {
        properties.setProperty(PROPERTY_PROXY_HOST, proxyHost);
    }
    public synchronized void setProxyPort(final String proxyPort) {
    	properties.setProperty(PROPERTY_PROXY_PORT, proxyPort);
    }
    public synchronized void setProxyUsername(final String proxyUsername) {
    	properties.setProperty(PROPERTY_PROXY_USERNAME, proxyUsername);
    }
    public synchronized void setProxyPassword(final String proxyPassword) {
    	properties.setProperty(PROPERTY_PROXY_PASSWORD, proxyPassword);
    }

    public synchronized GameJob getJob() {
        return GameJob.valueOf(properties.getProperty(PROPERTY_JOB));
    }

    public synchronized String getProxyHost() {
    	return properties.getProperty(PROPERTY_PROXY_HOST);
    }

    public synchronized String getProxyPort() {
    	return properties.getProperty(PROPERTY_PROXY_PORT);
    }

    public synchronized String getProxyUsername() {
    	return properties.getProperty(PROPERTY_PROXY_USERNAME);
    }

    public synchronized String getProxyPassword() {
    	return properties.getProperty(PROPERTY_PROXY_PASSWORD);
    }

    public synchronized void setBuildVersion(final int version, final GameJob job) {
        properties.setProperty(PROPERTY_PREFIX_BUILD_VERSION + job.name(), String.valueOf(version));
    }

    public synchronized int getBuildVersion(final GameJob job) {
        return Integer.parseInt(properties.getProperty(PROPERTY_PREFIX_BUILD_VERSION + job.name()));
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

    public synchronized void setGamesDirectory(final File gamesDirectory) {
        properties.setProperty(PROPERTY_GAMES_DIRECTORY, gamesDirectory.toURI().toString());
    }

    public synchronized File getGamesDirectory() {
        final String gamesDirectoryStr = properties.getProperty(PROPERTY_GAMES_DIRECTORY);
        if ((gamesDirectoryStr != null) && (gamesDirectoryStr.trim().length() > 0)) {
            try {
                return new File(new URI(gamesDirectoryStr));
            } catch (Exception e) {
                logger.error("Couldn't convert URI-String into File! {}", gamesDirectoryStr, e);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + properties.toString() + "]";
    }
}
