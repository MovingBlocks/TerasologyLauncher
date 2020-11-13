// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.terasology.launcher.util.JavaHeapSize;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public abstract class LauncherSettings {

    private static final Logger logger = LoggerFactory.getLogger(LauncherSettings.class);

    public abstract Properties getProperties();

    public synchronized void init() {
        logger.trace("Init launcher settings ...");

        initLocale();
        initMaxHeapSize();
        initInitialHeapSize();
        initBaseJavaParameters();
        initCloseLauncherAfterGameStart();
        initSaveDownloadedFiles();
        initGameDirectory();
        initGameDataDirectory();
        initUserJavaParameters();
        initUserGameParameters();
        initLogLevel();
        initDefaultGameJob();
        initLastPlayedGameJob();
        initLastPlayedGameVersion();
        initLastInstalledGameJob();
        initLastInstalledGameVersion();
    }

    // --------------------------------------------------------------------- //
    // INIT
    // --------------------------------------------------------------------- //

    protected abstract void initInitialHeapSize();

    protected abstract void initCloseLauncherAfterGameStart();

    protected abstract void initSaveDownloadedFiles();

    protected abstract void initGameDirectory();

    protected abstract void initGameDataDirectory();

    protected abstract void initBaseJavaParameters();

    protected abstract void initUserJavaParameters();

    protected abstract void initUserGameParameters();

    protected abstract void initLogLevel();

    protected abstract void initMaxHeapSize();

    protected abstract void initLocale();

    protected abstract void initDefaultGameJob();

    protected abstract void initLastPlayedGameJob();

    protected abstract void initLastPlayedGameVersion();

    protected abstract void initLastInstalledGameJob();

    protected abstract void initLastInstalledGameVersion();

    // --------------------------------------------------------------------- //
    // GETTERS
    // --------------------------------------------------------------------- //

    public abstract Locale getLocale();

    public abstract JavaHeapSize getMaxHeapSize();

    public abstract JavaHeapSize getInitialHeapSize();

    public abstract String getUserJavaParameters();

    public abstract String getBaseJavaParameters();

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

    public abstract String getUserGameParameters();

    public synchronized List<String> getUserGameParameterList() {
        return Arrays.asList(getUserGameParameters().split("\\s+"));
    }

    public abstract Level getLogLevel();

    public abstract Path getGameDirectory();

    public abstract Path getGameDataDirectory();

    public abstract boolean isCloseLauncherAfterGameStart();

    public abstract boolean isKeepDownloadedFiles();

    public abstract String getDefaultGameJob();

    public abstract String getLastPlayedGameJob();

    public abstract String getLastPlayedGameVersion();

    public abstract String getLastInstalledGameJob();

    public abstract String getLastInstalledGameVersion();

    // --------------------------------------------------------------------- //
    // SETTERS
    // --------------------------------------------------------------------- //

    public abstract void setLocale(Locale locale);

    public abstract void setMaxHeapSize(JavaHeapSize maxHeapSize);

    public abstract void setInitialHeapSize(JavaHeapSize initialHeapSize);

    public abstract void setUserJavaParameters(String userJavaParameters);

    public abstract void setUserGameParameters(String userGameParameters);

    public abstract void setLogLevel(Level logLevel);

    public abstract void setCloseLauncherAfterGameStart(boolean closeLauncherAfterGameStart);

    public abstract void setKeepDownloadedFiles(boolean keepDownloadedFiles);

    public abstract void setGameDirectory(Path gameDirectory);

    public abstract void setGameDataDirectory(Path gameDataDirectory);

    public abstract void setDefaultGameJob(String lastPlayedGameJob);

    public abstract void setLastPlayedGameJob(String lastPlayedGameJob);

    public abstract void setLastPlayedGameVersion(String lastPlayedGameVersion);

    public abstract void setLastInstalledGameJob(String lastInstalledGameJob);

    public abstract void setLastInstalledGameVersion(String lastInstalledGameVersion);
}
