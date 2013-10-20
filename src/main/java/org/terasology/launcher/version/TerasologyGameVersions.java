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

package org.terasology.launcher.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.LauncherSettings;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.JobResult;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    private final Map<GameJob, List<TerasologyGameVersion>> gameVersionLists;
    private final Map<GameJob, SortedMap<Integer, TerasologyGameVersion>> gameVersionMaps;

    public TerasologyGameVersions() {
        gameVersionLists = new HashMap<>();
        gameVersionMaps = new HashMap<>();
    }

    public synchronized List<TerasologyGameVersion> getGameVersionList(final GameJob job) {
        return gameVersionLists.get(job);
    }

    public synchronized TerasologyGameVersion getGameVersionForBuildVersion(final GameJob job, final int buildVersion) {
        final List<TerasologyGameVersion> gameVersionList = getGameVersionList(job);
        for (TerasologyGameVersion gameVersion : gameVersionList) {
            if (buildVersion == gameVersion.getBuildVersion()) {
                return gameVersion;
            }
        }
        logger.warn("GameVersion not found for '{}' '{}'.", job, buildVersion);
        return null;
    }

    public synchronized void loadGameVersions(final LauncherSettings launcherSettings, final File launcherDirectory,
                                              final File gamesDirectory) {
        final File cacheDirectory = getAndCheckCacheDirectory(launcherDirectory);

        gameVersionLists.clear();
        gameVersionMaps.clear();

        final Map<GameJob, SortedSet<Integer>> buildNumbersMap = new HashMap<>();
        final Map<GameJob, Integer> lastBuildNumbers = new HashMap<>();
        for (GameJob job : GameJob.values()) {
            gameVersionMaps.put(job, new TreeMap<Integer, TerasologyGameVersion>());
            final SortedSet<Integer> buildNumbers = new TreeSet<>();
            buildNumbersMap.put(job, buildNumbers);

            loadSettingsBuildNumber(launcherSettings, buildNumbers, job);
            lastBuildNumbers.put(job, loadLastSuccessfulBuildNumber(launcherSettings, buildNumbers, job));
        }

        loadInstalledGames(gamesDirectory, buildNumbersMap);

        for (GameJob job : GameJob.values()) {
            final SortedMap<Integer, TerasologyGameVersion> gameVersionMap = gameVersionMaps.get(job);
            final SortedSet<Integer> buildNumbers = buildNumbersMap.get(job);
            final Integer lastBuildNumber = lastBuildNumbers.get(job);

            fillBuildNumbers(buildNumbers, job.getMinBuildNumber(), lastBuildNumber);
            SortedMap<Integer, TerasologyGameVersion> cachedGameVersions = null;
            if (cacheDirectory != null) {
                cachedGameVersions = readFromCache(job, buildNumbers, cacheDirectory);
            }
            loadGameVersions(buildNumbers, job, gameVersionMap, cachedGameVersions);
            if (cacheDirectory != null) {
                writeToCache(job, cacheDirectory);
            }
            final List<TerasologyGameVersion> gameVersionList = createList(lastBuildNumber, job, gameVersionMap);
            gameVersionLists.put(job, gameVersionList);
        }
    }

    public synchronized void fixSettingsBuildVersion(final LauncherSettings launcherSettings) {
        for (GameJob job : GameJob.values()) {
            final SortedMap<Integer, TerasologyGameVersion> gameVersions = gameVersionMaps.get(job);
            fixSettingsBuildVersion(launcherSettings, job, gameVersions);
        }
    }

    private File getAndCheckCacheDirectory(final File launcherDirectory) {
        File cacheDirectory;
        try {
            cacheDirectory = new File(launcherDirectory, DirectoryUtils.CACHE_DIR_NAME);
            DirectoryUtils.checkDirectory(cacheDirectory);
        } catch (IOException e) {
            cacheDirectory = null;
            logger.error("The cache directory can not be created or used! '{}'", cacheDirectory, e);
        }
        return cacheDirectory;
    }

    private void loadSettingsBuildNumber(final LauncherSettings launcherSettings,
                                         final SortedSet<Integer> buildNumbers, final GameJob job) {
        final int buildVersion = launcherSettings.getBuildVersion(job);
        if ((buildVersion >= job.getMinBuildNumber()) && (TerasologyGameVersion.BUILD_VERSION_LATEST != buildVersion)) {
            buildNumbers.add(buildVersion);
        }
    }

    private Integer loadLastSuccessfulBuildNumber(LauncherSettings launcherSettings, final SortedSet<Integer> buildNumbers, final GameJob job) {
        Integer lastSuccessfulBuildNumber = null;
        try {
            // Use "successful" and not "stable" for TerasologyGame.
            lastSuccessfulBuildNumber = DownloadUtils.loadLastSuccessfulBuildNumber(launcherSettings, job.name());
            if (lastSuccessfulBuildNumber >= job.getMinBuildNumber()) {
                buildNumbers.add(lastSuccessfulBuildNumber);
                // add previous build numbers
                buildNumbers.add(Math.max(job.getMinBuildNumber(),
                    lastSuccessfulBuildNumber - job.getPrevBuildNumbers()));
            }
        } catch (DownloadException e) {
            logger.info("Retrieving last successful build number failed. '{}'", job, e);
        }
        return lastSuccessfulBuildNumber;
    }

    private void loadInstalledGames(final File directory, final Map<GameJob, SortedSet<Integer>> buildNumbersMap) {
        final File[] gameJar = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isFile() && file.canRead() && FILE_TERASOLOGY_JAR.equals(file.getName());
            }
        }
        );

        if ((gameJar != null) && (gameJar.length == 1)) {
            final TerasologyGameVersion gameVersion = loadInstalledGameVersion(gameJar[0]);
            if (gameVersion != null) {
                final SortedMap<Integer, TerasologyGameVersion> gameVersionMap =
                    gameVersionMaps.get(gameVersion.getJob());
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
                public boolean accept(final File file) {
                    return file.isDirectory() && file.canRead();
                }
            });
            if (subDirectories != null) {
                for (File subDirectory : subDirectories) {
                    loadInstalledGames(subDirectory, buildNumbersMap);
                }
            }
        }
    }

    private TerasologyGameVersion loadInstalledGameVersion(final File gameJar) {
        TerasologyGameVersion gameVersion = null;
        if (gameJar.exists() && gameJar.canRead() && gameJar.isFile()) {
            final TerasologyGameVersionInfo gameVersionInfo = TerasologyGameVersionInfo.loadFromJar(gameJar);
            if ((gameVersionInfo.getJobName() != null) && (gameVersionInfo.getJobName().length() > 0)
                && (gameVersionInfo.getBuildNumber() != null) && (gameVersionInfo.getBuildNumber().length() > 0)) {
                GameJob installedJob = null;
                try {
                    installedJob = GameJob.valueOf(gameVersionInfo.getJobName());
                } catch (IllegalArgumentException e) {
                    logger.error("Unknown job '{}'!", gameVersionInfo.getJobName(), e);
                }
                Integer installedBuildNumber = null;
                try {
                    installedBuildNumber = Integer.parseInt(gameVersionInfo.getBuildNumber());
                } catch (NumberFormatException e) {
                    logger.error("The build number can not be parsed! '{}'!", gameVersionInfo.getBuildNumber(), e);
                }

                if ((installedJob != null) && (installedBuildNumber != null)
                    && (installedJob.getGitBranch().equals(gameVersionInfo.getGitBranch()))
                    && (installedJob.getMinBuildNumber() <= installedBuildNumber)) {
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
                    logger.warn("The game version info can not be used from the file '{}'!", gameJar);
                }
            } else {
                logger.warn("The game version info can not be loaded from the file '{}'!", gameJar);
            }
        }
        return gameVersion;
    }

    private void fillBuildNumbers(final SortedSet<Integer> buildNumbers, final int minBuildNumber,
                                  final Integer lastBuildNumber) {
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

    private SortedMap<Integer, TerasologyGameVersion> readFromCache(final GameJob job,
                                                                    final SortedSet<Integer> buildNumbers,
                                                                    final File cacheDirectory) {
        final SortedMap<Integer, TerasologyGameVersion> cachedGameVersions = new TreeMap<>();
        try {
            for (Integer buildNumber : buildNumbers) {
                final File cacheFile = createCacheFile(job, buildNumber, cacheDirectory);
                if (cacheFile.exists() && cacheFile.canRead() && cacheFile.isFile()) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
                        final TerasologyGameVersion gameVersion = (TerasologyGameVersion) ois.readObject();
                        cachedGameVersions.put(buildNumber, gameVersion);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("The cached data can not be loaded!", e);
        }
        return cachedGameVersions;
    }

    private File createCacheFile(final GameJob job, final Integer buildNumber, final File cacheDirectory) {
        return new File(cacheDirectory, "TerasologyGameVersion_" + job.name() + "_" + buildNumber.toString()
            + ".cache");
    }

    private void loadGameVersions(final SortedSet<Integer> buildNumbers, final GameJob job,
                                  final SortedMap<Integer, TerasologyGameVersion> gameVersions,
                                  final SortedMap<Integer, TerasologyGameVersion> cachedGameVersionMap) {
        for (Integer buildNumber : buildNumbers) {
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
                if (!buildNumber.equals(cachedGameVersion.getBuildNumber())
                    || !job.equals(cachedGameVersion.getJob())) {
                    logger.warn("The cached game version can not be used! '{}'", cachedGameVersion);
                    cachedGameVersion = null;
                }
            }

            // load and set successful
            if (gameVersion.getSuccessful() == null) {
                if ((cachedGameVersion != null) && (cachedGameVersion.getSuccessful() != null)) {
                    gameVersion.setSuccessful(cachedGameVersion.getSuccessful());
                } else {
                    Boolean successful = null;
                    try {
                        JobResult jobResult = DownloadUtils.loadJobResult(job.name(), buildNumber);
                        successful = (jobResult != null
                            && ((jobResult == JobResult.SUCCESS) || (jobResult == JobResult.UNSTABLE)));
                    } catch (DownloadException e) {
                        logger.debug("Load job result failed. '{}' '{}'", job, buildNumber, e);
                    }
                    gameVersion.setSuccessful(successful);
                }
            }

            // load and set changeLog
            if (gameVersion.getChangeLog() == null) {
                if ((cachedGameVersion != null) && (cachedGameVersion.getChangeLog() != null)) {
                    gameVersion.setChangeLog(cachedGameVersion.getChangeLog());
                } else {
                    List<String> changeLog = null;
                    try {
                        changeLog = DownloadUtils.loadChangeLog(job.name(), buildNumber);
                    } catch (DownloadException e) {
                        logger.debug("Load change log failed. '{}' '{}'", job, buildNumber, e);
                    }
                    if ((changeLog != null) && !changeLog.isEmpty()) {
                        gameVersion.setChangeLog(Collections.unmodifiableList(changeLog));
                    }
                }
            }

            // load and set gameVersionInfo
            if (gameVersion.getGameVersionInfo() == null) {
                if ((cachedGameVersion != null) && (cachedGameVersion.getGameVersionInfo() != null)) {
                    gameVersion.setGameVersionInfo(cachedGameVersion.getGameVersionInfo());
                } else {
                    TerasologyGameVersionInfo gameVersionInfo = null;
                    try {
                        gameVersionInfo = DownloadUtils.loadTerasologyGameVersionInfo(job.name(), buildNumber);
                    } catch (DownloadException e) {
                        logger.debug("Load game version info failed. '{}' '{}'", job, buildNumber, e);
                    }
                    gameVersion.setGameVersionInfo(gameVersionInfo);
                }
            }
        }
    }

    private void writeToCache(final GameJob job, final File cacheDirectory) {
        try {
            final SortedMap<Integer, TerasologyGameVersion> gameVersions = gameVersionMaps.get(job);
            for (TerasologyGameVersion gameVersion : gameVersions.values()) {
                final File cacheFile = createCacheFile(job, gameVersion.getBuildNumber(), cacheDirectory);
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
                    oos.writeObject(gameVersion);
                }
            }
        } catch (IOException e) {
            logger.error("The cache data can not be written!", e);
        }
    }

    private List<TerasologyGameVersion> createList(final Integer lastBuildNumber, final GameJob job,
                                                   final SortedMap<Integer, TerasologyGameVersion> gameVersionMap) {
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

    private void fixSettingsBuildVersion(final LauncherSettings launcherSettings, final GameJob job,
                                         final SortedMap<Integer, TerasologyGameVersion> gameVersionMap) {
        final int buildVersion = launcherSettings.getBuildVersion(job);
        if ((buildVersion != TerasologyGameVersion.BUILD_VERSION_LATEST) && !gameVersionMap.containsKey(buildVersion)) {
            Integer newBuildVersion = TerasologyGameVersion.BUILD_VERSION_LATEST;
            for (TerasologyGameVersion gameVersion : gameVersionMap.values()) {
                if (gameVersion.isInstalled()) {
                    newBuildVersion = gameVersion.getBuildNumber();
                    // no break => find highest installed version
                }
            }
            launcherSettings.setBuildVersion(newBuildVersion, job);
            // don't store settings
        }
    }

    public synchronized boolean updateGameVersionsAfterInstallation(final File terasologyDirectory) {
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
                }
            }
            return true;
        } else {
            logger.error("The game version can not be loaded from directory '{}'!", terasologyDirectory);
        }
        return false;
    }

    public synchronized void removeInstallationInfo(final TerasologyGameVersion gameVersion) {
        if (gameVersion.isInstalled()) {
            if (gameVersion.isLatest()) {
                final TerasologyGameVersion related = getGameVersionForBuildVersion(gameVersion.getJob(),
                    gameVersion.getBuildNumber());
                if ((related != null) && related.isInstalled()
                    && (related.getInstallationPath().equals(gameVersion.getInstallationPath()))) {
                    logger.trace("Remove installation info from related game version. '{}'", related);
                    related.setInstallationPath(null);
                    related.setGameJar(null);
                }
            } else {
                final TerasologyGameVersion latest = getGameVersionForBuildVersion(gameVersion.getJob(),
                    TerasologyGameVersion.BUILD_VERSION_LATEST);
                if ((latest != null) && latest.isInstalled()
                    && (latest.getInstallationPath().equals(gameVersion.getInstallationPath()))) {
                    logger.trace("Remove installation info from latest game version. '{}'", latest);
                    latest.setInstallationPath(null);
                    latest.setGameJar(null);
                }
            }
            logger.trace("Remove installation info from game version. '{}'", gameVersion);
            gameVersion.setInstallationPath(null);
            gameVersion.setGameJar(null);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + gameVersionLists + "]";
    }
}
