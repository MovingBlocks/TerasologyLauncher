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
import org.slf4j.event.Level;
import org.terasology.launcher.util.JavaHeapSize;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * @deprecated to be replaced by {@link org.terasology.launcher.config.Config}
 */
@Deprecated
public abstract class LauncherSettings {

    private static final Logger logger = LoggerFactory.getLogger(LauncherSettings.class);

    public abstract Properties getProperties();

    public synchronized void init() {
        logger.trace("Init launcher settings ...");

        initLocale();
        initMaxHeapSize();
        initInitialHeapSize();
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

    public synchronized List<String> getUserJavaParameterList() {
        return Arrays.asList(getUserJavaParameters().split("\\s+"));
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
