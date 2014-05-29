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

package org.terasology.launcher.game;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.JobResult;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public final class TerasologyGameVersions {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyGameVersions.class);

    private static final String FILE_TERASOLOGY_JAR = "Terasology.jar";
    private static final String DIR_LIBS = "libs";
    private static final String FILE_ENGINE_JAR = "engine.*jar";
    private static final long OUTDATED_CACHE_MILLI_SECONDS = 1000L * 60L * 60L * 24L; // one day
    private static final String FILE_SUFFIX_CACHE = ".cache";

    private final Map<GameJob, List<TerasologyGameVersion>> gameVersionLists;
    private final Map<GameJob, SortedMap<Integer, TerasologyGameVersion>> gameVersionMaps;

    public TerasologyGameVersions() {
        gameVersionLists = new HashMap<>();
        gameVersionMaps = new HashMap<>();
    }

    public synchronized List<TerasologyGameVersion> getGameVersionList(GameJob job) {
        return gameVersionLists.get(job);
    }

    public synchronized TerasologyGameVersion getGameVersionForBuildVersion(GameJob job, int buildVersion) {
        final List<TerasologyGameVersion> gameVersionList = getGameVersionList(job);
        for (TerasologyGameVersion gameVersion : gameVersionList) {
            if (buildVersion == gameVersion.getBuildVersion()) {
                return gameVersion;
            }
        }
        logger.warn("GameVersion not found for '{}' '{}'.", job, buildVersion);
        return null;
    }

    public synchronized void loadGameVersions(GameSettings gameSettings, File launcherDirectory, File gameDirectory, Application app) {
        final File cacheDirectory = getAndCheckCacheDirectory(launcherDirectory);

        gameVersionLists.clear();
        gameVersionMaps.clear();

        final Map<GameJob, SortedSet<Integer>> buildNumbersMap = new HashMap<>();
        final Map<GameJob, Integer> lastBuildNumbers = new HashMap<>();
        for (GameJob job : GameJob.values()) {
            // TODO JavaFX Preloader
            // app.notifyPreloader();
            // progressListener.update();

            gameVersionMaps.put(job, new TreeMap<Integer, TerasologyGameVersion>());
            final SortedSet<Integer> buildNumbers = new TreeSet<>();
            buildNumbersMap.put(job, buildNumbers);

            loadSettingsBuildNumber(gameSettings, buildNumbers, job);
            lastBuildNumbers.put(job, loadLastSuccessfulBuildNumber(getLastBuildNumberFromSettings(gameSettings, job), buildNumbers, job));
        }

        loadInstalledGames(gameDirectory, buildNumbersMap, app);

        for (GameJob job : GameJob.values()) {
            // TODO JavaFX Preloader
            // app.notifyPreloader();
            // progressListener.update();

            final SortedMap<Integer, TerasologyGameVersion> gameVersionMap = gameVersionMaps.get(job);
            final SortedSet<Integer> buildNumbers = buildNumbersMap.get(job);
            final Integer lastBuildNumber = lastBuildNumbers.get(job);

            gameSettings.setLastBuildNumber(lastBuildNumber, job);
            if (job.isStable() && !job.isOnlyInstalled()) {
                fillBuildNumbers(buildNumbers, job.getMinBuildNumber(), lastBuildNumber);
            }
            SortedMap<Integer, TerasologyGameVersion> cachedGameVersions = null;
            if (cacheDirectory != null) {
                cachedGameVersions = readFromCache(job, buildNumbers, cacheDirectory);
            }
            loadGameVersions(buildNumbers, job, gameVersionMap, cachedGameVersions, app);
            if (cacheDirectory != null) {
                writeToCache(job, cacheDirectory);
            }
            final List<TerasologyGameVersion> gameVersionList = createList(lastBuildNumber, job, gameVersionMap);
            gameVersionLists.put(job, gameVersionList);
        }

        if (cacheDirectory != null) {
            deleteOldCache(cacheDirectory);
        }
    }

    public synchronized void fixSettingsBuildVersion(GameSettings gameSettings) {
        for (GameJob job : GameJob.values()) {
            final SortedMap<Integer, TerasologyGameVersion> gameVersions = gameVersionMaps.get(job);
            fixSettingsBuildVersion(gameSettings, job, gameVersions);
        }
    }

    private File getAndCheckCacheDirectory(File launcherDirectory) {
        File cacheDirectory = null;
        try {
            cacheDirectory = new File(launcherDirectory, DirectoryUtils.CACHE_DIR_NAME);
            DirectoryUtils.checkDirectory(cacheDirectory);
        } catch (IOException e) {
            logger.error("Could not create or use cache directory '{}'!", cacheDirectory, e);
            cacheDirectory = null;
        }
        return cacheDirectory;
    }

    private void loadSettingsBuildNumber(GameSettings gameSettings, SortedSet<Integer> buildNumbers, GameJob job) {
        final int buildVersion = gameSettings.getBuildVersion(job);
        if ((TerasologyGameVersion.BUILD_VERSION_LATEST != buildVersion) && (buildVersion >= job.getMinBuildNumber())) {
            buildNumbers.add(buildVersion);
        }
    }

    private Integer getLastBuildNumberFromSettings(GameSettings gameSettings, GameJob job) {
        final Integer lastBuildNumber = gameSettings.getLastBuildNumber(job);
        final int buildVersion = gameSettings.getBuildVersion(job);
        final int lastBuildVersion;
        if (lastBuildNumber == null) {
            lastBuildVersion = buildVersion;
        } else {
            lastBuildVersion = Math.max(lastBuildNumber, buildVersion);
        }
        if ((TerasologyGameVersion.BUILD_VERSION_LATEST != lastBuildVersion) && (lastBuildVersion >= job.getMinBuildNumber())) {
            return lastBuildVersion;
        }
        return null;
    }

    private Integer loadLastSuccessfulBuildNumber(Integer lastBuildNumber, SortedSet<Integer> buildNumbers, GameJob job) {
        Integer lastSuccessfulBuildNumber = null;
        if (!job.isOnlyInstalled()) {
            try {
                // Use "successful" and not "stable" for TerasologyGame.
                lastSuccessfulBuildNumber = DownloadUtils.loadLastSuccessfulBuildNumberJenkins(job.name());
            } catch (DownloadException e) {
                logger.info("Retrieving last successful build number failed. '{}'", job, e);
                lastSuccessfulBuildNumber = lastBuildNumber;
            }

            if ((lastSuccessfulBuildNumber != null) && (lastSuccessfulBuildNumber >= job.getMinBuildNumber())) {
                buildNumbers.add(lastSuccessfulBuildNumber);
                // add previous build numbers
                final int prevBuildNumber = Math.max(job.getMinBuildNumber(), lastSuccessfulBuildNumber - job.getPrevBuildNumbers());
                for (int buildNumber = prevBuildNumber; buildNumber < lastSuccessfulBuildNumber; buildNumber++) {
                    buildNumbers.add(buildNumber);
                }
            }
        }
        return lastSuccessfulBuildNumber;
    }

    private void loadInstalledGames(File directory, Map<GameJob, SortedSet<Integer>> buildNumbersMap, Application app) {
        // TODO JavaFX Preloader
        // app.notifyPreloader();
        // progressListener.update();

        final File[] gameJar = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.canRead() && FILE_TERASOLOGY_JAR.equals(file.getName());
            }
        }
        );

        if ((gameJar != null) && (gameJar.length == 1)) {
            final TerasologyGameVersion gameVersion = loadInstalledGameVersion(gameJar[0]);
            if (gameVersion != null) {
                final SortedMap<Integer, TerasologyGameVersion> gameVersionMap = gameVersionMaps.get(gameVersion.getJob());
                final SortedSet<Integer> buildNumbers = buildNumbersMap.get(gameVersion.getJob());
                buildNumbers.add(gameVersion.getBuildNumber());
                if (!gameVersionMap.containsKey(gameVersion.getBuildNumber())) {
                    gameVersionMap.put(gameVersion.getBuildNumber(), gameVersion);
                } else {
                    logger.info("Installed game already loaded. '{}'", gameJar[0]);
                }
            }
        } else {
            final File[] subDirectories = directory.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    return file.isDirectory() && file.canRead();
                }
            });
            if (subDirectories != null) {
                for (File subDirectory : subDirectories) {
                    loadInstalledGames(subDirectory, buildNumbersMap, app);
                }
            }
        }
    }

    private TerasologyGameVersion loadInstalledGameVersion(File gameJar) {
        TerasologyGameVersion gameVersion = null;
        final TerasologyGameVersionInfo gameVersionInfo = loadInstalledGameVersionInfo(gameJar);

        if ((gameVersionInfo != null)
            && (gameVersionInfo.getJobName() != null) && (gameVersionInfo.getJobName().length() > 0)
            && (gameVersionInfo.getBuildNumber() != null) && (gameVersionInfo.getBuildNumber().length() > 0)) {
            GameJob installedJob = null;
            try {
                installedJob = GameJob.valueOf(gameVersionInfo.getJobName());
            } catch (IllegalArgumentException e) {
                logger.error("Unknown job '{}' found for game '{}'!", gameVersionInfo.getJobName(), gameJar);
            }
            Integer installedBuildNumber = null;
            try {
                installedBuildNumber = Integer.parseInt(gameVersionInfo.getBuildNumber());
            } catch (NumberFormatException e) {
                logger.error("Could not parse build number '{}'!", gameVersionInfo.getBuildNumber());
            }

            if ((installedJob != null) && (installedBuildNumber != null)
                && (gameVersionInfo.getGitBranch().endsWith(installedJob.getGitBranch())) && (installedJob.getMinBuildNumber() <= installedBuildNumber)) {
                gameVersion = new TerasologyGameVersion();
                gameVersion.setJob(installedJob);
                gameVersion.setBuildNumber(installedBuildNumber);
                gameVersion.setInstallationPath(gameJar.getParentFile());
                gameVersion.setGameJar(gameJar);
                gameVersion.setGameVersionInfo(gameVersionInfo);
                gameVersion.setChangeLog(null);
                gameVersion.setSuccessful(Boolean.TRUE);
                gameVersion.setLatest(false);
            } else {
                logger.warn("The game version info can not be used from the file '{}' or '{}'!", gameJar, FILE_ENGINE_JAR);
            }
        } else {
            logger.warn("The game version info can not be loaded from the file '{}' or '{}'!", gameJar, FILE_ENGINE_JAR);
        }
        return gameVersion;
    }

    private TerasologyGameVersionInfo loadInstalledGameVersionInfo(File gameJar) {
        TerasologyGameVersionInfo gameVersionInfo = null;

        if (gameJar.exists() && gameJar.canRead() && gameJar.isFile()) {
            final File libsDirectory = new File(gameJar.getParentFile(), DIR_LIBS);
            if (libsDirectory.isDirectory() && libsDirectory.canRead()) {
                final File[] engineJars = libsDirectory.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() && file.canRead() && file.getName().matches(FILE_ENGINE_JAR);
                    }
                }
                );

                if ((engineJars != null) && (engineJars.length == 1)) {
                    gameVersionInfo = TerasologyGameVersionInfo.loadFromJar(engineJars[0]);
                }
            }

            if (gameVersionInfo == null) {
                gameVersionInfo = TerasologyGameVersionInfo.loadFromJar(gameJar);
            }
        }

        return gameVersionInfo;
    }

    private void fillBuildNumbers(SortedSet<Integer> buildNumbers, int minBuildNumber, Integer lastBuildNumber) {
        if ((buildNumbers != null) && !buildNumbers.isEmpty()) {
            int first = buildNumbers.first();
            if (first < minBuildNumber) {
                first = minBuildNumber;
            }
            int last = buildNumbers.last();
            if ((lastBuildNumber != null) && (last > lastBuildNumber)) {
                last = lastBuildNumber;
            }
            // Add all build numbers between first and last
            for (int buildNumber = first + 1; buildNumber < last; buildNumber++) {
                buildNumbers.add(buildNumber);
            }
        }
    }

    private SortedMap<Integer, TerasologyGameVersion> readFromCache(GameJob job, SortedSet<Integer> buildNumbers, File cacheDirectory) {
        final SortedMap<Integer, TerasologyGameVersion> cachedGameVersions = new TreeMap<>();
        for (Integer buildNumber : buildNumbers) {
            final File cacheFile = createCacheFile(job, buildNumber, cacheDirectory);
            try {
                if (cacheFile.exists() && cacheFile.canRead() && cacheFile.isFile()) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
                        final TerasologyGameVersion gameVersion = (TerasologyGameVersion) ois.readObject();
                        cachedGameVersions.put(buildNumber, gameVersion);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.warn("Could not load cached data! '{}'", cacheFile);
            }
        }
        return cachedGameVersions;
    }

    private File createCacheFile(GameJob job, Integer buildNumber, File cacheDirectory) {
        return new File(cacheDirectory, "TerasologyGameVersion_" + job.name() + "_" + buildNumber.toString() + FILE_SUFFIX_CACHE);
    }

    private void loadGameVersions(SortedSet<Integer> buildNumbers, GameJob job, SortedMap<Integer, TerasologyGameVersion> gameVersions,
                                  SortedMap<Integer, TerasologyGameVersion> cachedGameVersionMap, Application app) {
        for (Integer buildNumber : buildNumbers) {
            // TODO JavaFX Preloader
            // app.notifyPreloader();
            // progressListener.update();

            final TerasologyGameVersion gameVersion;
            if (gameVersions.containsKey(buildNumber)) {
                gameVersion = gameVersions.get(buildNumber);
            } else {
                gameVersion = new TerasologyGameVersion();
                gameVersion.setBuildNumber(buildNumber);
                gameVersion.setJob(job);
                gameVersions.put(buildNumber, gameVersion);
            }

            TerasologyGameVersion cachedGameVersion = null;
            if ((cachedGameVersionMap != null) && cachedGameVersionMap.containsKey(buildNumber)) {
                cachedGameVersion = cachedGameVersionMap.get(buildNumber);
                if (!buildNumber.equals(cachedGameVersion.getBuildNumber()) || !job.equals(cachedGameVersion.getJob())) {
                    logger.warn("The cached game version can not be used! '{}'", cachedGameVersion);
                    cachedGameVersion = null;
                }
            }

            loadAndSetSuccessful(gameVersion, cachedGameVersion, job, buildNumber);

            loadAndSetChangeLog(gameVersion, cachedGameVersion, job, buildNumber);

            loadAndSetGameVersionInfo(gameVersion, cachedGameVersion, job, buildNumber);
        }
    }

    private void loadAndSetSuccessful(TerasologyGameVersion gameVersion, TerasologyGameVersion cachedGameVersion, GameJob job, Integer buildNumber) {
        if (gameVersion.getSuccessful() == null) {
            if ((cachedGameVersion != null) && (cachedGameVersion.getSuccessful() != null)) {
                gameVersion.setSuccessful(cachedGameVersion.getSuccessful());
            } else if (!job.isOnlyInstalled()) {
                Boolean successful = null;
                try {
                    JobResult jobResult = DownloadUtils.loadJobResultJenkins(job.name(), buildNumber);
                    successful = (jobResult != null && ((jobResult == JobResult.SUCCESS) || (jobResult == JobResult.UNSTABLE)));
                } catch (DownloadException e) {
                    logger.debug("Load job result failed. '{}' '{}'", job, buildNumber, e);
                }
                gameVersion.setSuccessful(successful);
            }
        }
    }

    private void loadAndSetChangeLog(TerasologyGameVersion gameVersion, TerasologyGameVersion cachedGameVersion, GameJob job, Integer buildNumber) {
        if (gameVersion.getChangeLog() == null) {
            if ((cachedGameVersion != null) && (cachedGameVersion.getChangeLog() != null)) {
                gameVersion.setChangeLog(cachedGameVersion.getChangeLog());
            } else if (!job.isOnlyInstalled()) {
                try {
                    final List<String> changeLog = DownloadUtils.loadChangeLogJenkins(job.name(), buildNumber);
                    if (changeLog != null) {
                        if (changeLog.isEmpty()) {
                            changeLog.add(BundleUtils.getLabel("message_noChangeLog"));
                        }
                        gameVersion.setChangeLog(Collections.unmodifiableList(changeLog));
                    }
                } catch (DownloadException e) {
                    logger.debug("Load change log failed. '{}' '{}'", job, buildNumber, e);
                }
            }
        }
    }

    private void loadAndSetGameVersionInfo(TerasologyGameVersion gameVersion, TerasologyGameVersion cachedGameVersion, GameJob job, Integer buildNumber) {
        if (gameVersion.getGameVersionInfo() == null) {
            if ((cachedGameVersion != null) && (cachedGameVersion.getGameVersionInfo() != null)) {
                gameVersion.setGameVersionInfo(cachedGameVersion.getGameVersionInfo());
            } else if (!job.isOnlyInstalled() && ((cachedGameVersion == null) || (gameVersion.getSuccessful() == null) || gameVersion.getSuccessful())) {
                TerasologyGameVersionInfo gameVersionInfo = null;
                URL urlVersionInfo = null;
                try {
                    urlVersionInfo = DownloadUtils.createFileDownloadUrlJenkins(job.name(), buildNumber, DownloadUtils.FILE_TERASOLOGY_GAME_VERSION_INFO);
                    gameVersionInfo = TerasologyGameVersionInfo.loadFromInputStream(urlVersionInfo.openStream());
                } catch (IOException e) {
                    if (e instanceof FileNotFoundException) {
                        logger.debug("Load game version info failed. '{}' '{}' '{}'", job, buildNumber, urlVersionInfo);
                        gameVersionInfo = TerasologyGameVersionInfo.getEmptyGameVersionInfo();
                    } else {
                        logger.info("Load game version info failed. '{}' '{}' '{}'", job, buildNumber, urlVersionInfo, e);
                    }
                }
                gameVersion.setGameVersionInfo(gameVersionInfo);
            }
        }
    }

    private void writeToCache(GameJob job, File cacheDirectory) {
        try {
            final SortedMap<Integer, TerasologyGameVersion> gameVersions = gameVersionMaps.get(job);
            for (TerasologyGameVersion gameVersion : gameVersions.values()) {
                final File cacheFile = createCacheFile(job, gameVersion.getBuildNumber(), cacheDirectory);
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
                    oos.writeObject(gameVersion);
                }
            }
        } catch (IOException e) {
            logger.error("Could not write cache data!", e);
        }
    }

    private void deleteOldCache(File cacheDirectory) {
        final File[] cacheFiles = cacheDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.exists() && pathname.isFile()
                    && pathname.canRead() && pathname.canWrite()
                    && pathname.getName().endsWith(FILE_SUFFIX_CACHE)
                    && pathname.lastModified() < (System.currentTimeMillis() - OUTDATED_CACHE_MILLI_SECONDS);
            }
        });
        if ((cacheFiles != null) && (cacheFiles.length > 0)) {
            logger.debug("Delete {} outdated cache files on exit.", cacheFiles.length);
            for (File cacheFile : cacheFiles) {
                cacheFile.deleteOnExit();
            }
        }
    }

    private List<TerasologyGameVersion> createList(Integer lastBuildNumber, GameJob job, SortedMap<Integer, TerasologyGameVersion> gameVersionMap) {
        final List<TerasologyGameVersion> gameVersionList = new ArrayList<>();
        gameVersionList.addAll(gameVersionMap.values());

        final TerasologyGameVersion latestGameVersion = new TerasologyGameVersion();
        latestGameVersion.setLatest(true);
        latestGameVersion.setJob(job);
        latestGameVersion.setBuildNumber(lastBuildNumber);
        if ((lastBuildNumber != null) && gameVersionMap.containsKey(lastBuildNumber)) {
            gameVersionMap.get(lastBuildNumber).copyTo(latestGameVersion);
        } else if ((lastBuildNumber == null) && !gameVersionMap.isEmpty()) {
            gameVersionMap.get(gameVersionMap.lastKey()).copyTo(latestGameVersion);
        }
        gameVersionList.add(latestGameVersion);

        Collections.reverse(gameVersionList);

        return Collections.unmodifiableList(gameVersionList);
    }

    private void fixSettingsBuildVersion(GameSettings gameSettings, GameJob job, SortedMap<Integer, TerasologyGameVersion> gameVersionMap) {
        final int buildVersion = gameSettings.getBuildVersion(job);
        if ((buildVersion != TerasologyGameVersion.BUILD_VERSION_LATEST) && !gameVersionMap.containsKey(buildVersion)) {
            Integer newBuildVersion = TerasologyGameVersion.BUILD_VERSION_LATEST;
            for (TerasologyGameVersion gameVersion : gameVersionMap.values()) {
                if (gameVersion.isInstalled()) {
                    newBuildVersion = gameVersion.getBuildNumber();
                    // no break => find highest installed version
                }
            }
            gameSettings.setBuildVersion(newBuildVersion, job);
            // don't store settings
        }
    }

    public synchronized boolean updateGameVersionsAfterInstallation(File terasologyDirectory) {
        final File gameJar = new File(terasologyDirectory, FILE_TERASOLOGY_JAR);
        final TerasologyGameVersion gameVersion = loadInstalledGameVersion(gameJar);
        if (gameVersion != null) {
            final List<TerasologyGameVersion> gameVersionList = getGameVersionList(gameVersion.getJob());
            for (TerasologyGameVersion currentGameVersion : gameVersionList) {
                if (gameVersion.getBuildNumber().equals(currentGameVersion.getBuildNumber())) {
                    if (gameVersion.getGameVersionInfo() != null) {
                        currentGameVersion.setGameVersionInfo(gameVersion.getGameVersionInfo());
                    }
                    currentGameVersion.setInstallationPath(gameVersion.getInstallationPath());
                    currentGameVersion.setGameJar(gameVersion.getGameJar());
                    logger.debug("Update game version with new installation: {}", currentGameVersion);
                }
            }
            return true;
        } else {
            logger.error("The game version can not be loaded from directory '{}'!", terasologyDirectory);
        }
        return false;
    }

    public synchronized void removeInstallationInfo(TerasologyGameVersion gameVersion) {
        if (gameVersion.isInstalled()) {
            if (gameVersion.isLatest()) {
                final TerasologyGameVersion related = getGameVersionForBuildVersion(gameVersion.getJob(), gameVersion.getBuildNumber());
                if ((related != null) && related.isInstalled() && (related.getInstallationPath().equals(gameVersion.getInstallationPath()))) {
                    logger.debug("Remove installation info from related game version. '{}'", related);
                    related.setInstallationPath(null);
                    related.setGameJar(null);
                }
            } else {
                final TerasologyGameVersion latest = getGameVersionForBuildVersion(gameVersion.getJob(), TerasologyGameVersion.BUILD_VERSION_LATEST);
                if ((latest != null) && latest.isInstalled() && (latest.getInstallationPath().equals(gameVersion.getInstallationPath()))) {
                    logger.debug("Remove installation info from latest game version. '{}'", latest);
                    latest.setInstallationPath(null);
                    latest.setGameJar(null);
                }
            }
            logger.debug("Remove installation info from game version. '{}'", gameVersion);
            gameVersion.setInstallationPath(null);
            gameVersion.setGameJar(null);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + gameVersionLists + "]";
    }
}
