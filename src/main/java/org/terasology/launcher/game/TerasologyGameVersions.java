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

package org.terasology.launcher.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.IBuildRepository;
import org.terasology.launcher.util.JobResult;
import org.terasology.launcher.util.LauncherDirectoryUtils;
import org.terasology.launcher.util.OnlineBuildRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TerasologyGameVersions {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyGameVersions.class);

    private static final String FILE_TERASOLOGY_JAR = "Terasology.jar";
    private static final String DIR_LIBS = "libs";
    private static final String FILE_ENGINE_JAR = "engine.*jar";
    private static final long OUTDATED_CACHE_MILLI_SECONDS = 1000L * 60L * 60L * 24L; // one day
    private static final String FILE_SUFFIX_CACHE = ".cache";

    private final Map<GameJob, List<TerasologyGameVersion>> gameVersionLists;
    private final Map<GameJob, SortedMap<Integer, TerasologyGameVersion>> gameVersionMaps;

    private final IBuildRepository buildRepository;

    public TerasologyGameVersions() {
        this(new OnlineBuildRepository());
    }

    public TerasologyGameVersions(IBuildRepository buildRepository) {
        gameVersionLists = new EnumMap<>(GameJob.class);
        gameVersionMaps = new EnumMap<>(GameJob.class);
        assert buildRepository != null;
        this.buildRepository = buildRepository;
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

    private TerasologyGameVersion getGameVersion(int buildNumber, GameJob job, Path cacheDirectory) {
        // Return cached version if it exists
        final Path cacheFile = createCacheFile(job, buildNumber, cacheDirectory);
        try {
            if (Files.exists(cacheFile) && Files.isReadable(cacheFile) && Files.isRegularFile(cacheFile)) {
                logger.debug("Found cached version for build {} of job {}", buildNumber, job);
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(cacheFile))) {
                    return (TerasologyGameVersion) ois.readObject();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.debug("Could not load cached version: {}", cacheFile.getFileName());
        }

        // Else create a new one
        final TerasologyGameVersion gameVersion = new TerasologyGameVersion();
        gameVersion.setJob(job);
        gameVersion.setBuildNumber(buildNumber);
        loadAndSetGameVersionInfo(gameVersion, null, job, buildNumber);
        loadAndSetChangeLog(gameVersion, null, job, buildNumber);
        loadAndSetSuccessful(gameVersion, null, job, buildNumber);

        return gameVersion;
    }

    private List<TerasologyGameVersion> getInstalledGames(Path gameDirectory) {
        try {
            final int maxDepth = 5;
            return Files.find(gameDirectory, maxDepth, (path, attributes) ->
                    path.getFileName().toString().matches(FILE_ENGINE_JAR))
                    .map(this::loadInstalledGameVersion)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Hit an error scanning for existing file directories: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    private TerasologyGameVersion makeLatestFrom(TerasologyGameVersion gameVersion) {
        final TerasologyGameVersion latestGame = new TerasologyGameVersion();
        gameVersion.copyTo(latestGame);
        latestGame.setLatest(true);
        return latestGame;
    }

    public synchronized void loadGameVersions(Path launcherDirectory, Path gameDirectory) {
        final Path cacheDirectory = getAndCheckCacheDirectory(launcherDirectory);

        gameVersionLists.clear();
        gameVersionMaps.clear();

        final Map<GameJob, SortedSet<Integer>> buildNumbersMap = new EnumMap<>(GameJob.class);
        final Map<GameJob, Integer> lastBuildNumbers = new EnumMap<>(GameJob.class);

        // Go over the job lines and figure out available build numbers
        for (GameJob job : GameJob.values()) {
            gameVersionMaps.put(job, new TreeMap<>());
            final SortedSet<Integer> buildNumbers = new TreeSet<>();
            buildNumbersMap.put(job, buildNumbers);

            // Go check Jenkins for the last successful build (so failures are skipped), then add more going backwards
            Integer lastBuildNumberFromJenkins = loadLastSuccessfulBuildNumber(buildNumbers, job);

            // Finally add the mapping to our storage
            lastBuildNumbers.put(job, lastBuildNumberFromJenkins);
        }

        // With the build numbers in hand we can go check for any existing installs locally
        loadInstalledGames(gameDirectory, buildNumbersMap);

        // For each job line now fill in the extra version details needed for each build
        for (GameJob job : GameJob.values()) {
            final SortedMap<Integer, TerasologyGameVersion> gameVersionMap = gameVersionMaps.get(job);
            final SortedSet<Integer> buildNumbers = buildNumbersMap.get(job);
            final Integer lastBuildNumber = lastBuildNumbers.get(job);

            if (job.isStable() && !job.isOnlyInstalled()) {
                fillBuildNumbers(buildNumbers, job.getMinBuildNumber(), lastBuildNumber);
            }

            // Add in more detailed info for any existing local game versions (?)
            SortedMap<Integer, TerasologyGameVersion> cachedGameVersions = null;
            if (cacheDirectory != null) {
                cachedGameVersions = readFromCache(job, buildNumbers, cacheDirectory);
            }

            // Add even more info including defaults if we didn't have a local copy (?)
            loadGameVersions(buildNumbers, job, gameVersionMap, cachedGameVersions);

            // Now go back over the list of good builds and match them to Omega builds if possible
            fillInOmegaBuilds(gameVersionMap, buildNumbers, job);

            // Finally update the local cache if appropriate (?)
            if (cacheDirectory != null) {
                writeToCache(job, cacheDirectory);
            }

            // Prepare the list in memory for display to the user (?)
            final List<TerasologyGameVersion> gameVersionList = createList(lastBuildNumber, job, gameVersionMap);
            gameVersionLists.put(job, gameVersionList);
        }

        if (cacheDirectory != null) {
            deleteOldCache(cacheDirectory);
        }
    }

    private Path getAndCheckCacheDirectory(Path launcherDirectory) {
        Path cacheDirectory = null;
        try {
            cacheDirectory = launcherDirectory.resolve(LauncherDirectoryUtils.CACHE_DIR_NAME);
            FileUtils.ensureWritableDir(cacheDirectory);
        } catch (IOException e) {
            logger.error("Could not create or use cache directory '{}'!", cacheDirectory, e);
            cacheDirectory = null;
        }
        return cacheDirectory;
    }

    private Integer loadLastSuccessfulBuildNumber(SortedSet<Integer> buildNumbers, GameJob job) {
        Integer lastSuccessfulBuildNumber = null;
        if (!job.isOnlyInstalled()) {
            try {
                // Use "successful" and not "stable" for TerasologyGame.
                lastSuccessfulBuildNumber = buildRepository.loadLastSuccessfulBuildNumberJenkins(job.name());
            } catch (DownloadException e) {
                logger.info("Retrieving last successful build number failed. '{}'", job, e);
                lastSuccessfulBuildNumber = null;
            }

            if (lastSuccessfulBuildNumber != null && lastSuccessfulBuildNumber >= job.getMinBuildNumber()) {
                buildNumbers.add(lastSuccessfulBuildNumber);
                // add previous build numbers
                for (int buildNumber = lastSuccessfulBuildNumber - 1; buildNumbers.size() <= job.getPrevBuildNumbers() && buildNumber > job
                        .getMinBuildNumber();
                     buildNumber--) {
                    try {
                        // Skip unavailable builds
                        buildRepository.loadJobResultJenkins(job.name(), buildNumber);
                        buildNumbers.add(buildNumber);
                    } catch (DownloadException e) {
                        logger.info("Cannot find build number '{}' for job '{}'.", buildNumber, job);
                    }

                }
            }
        }
        return lastSuccessfulBuildNumber;
    }

    /**
     * Take an existing set of engine build numbers loaded from Jenkins and look for mapped Omega distributions to add.
     *
     * @param buildNumbers The engine build numbers we know exist
     * @param job          the job line we're working on
     */
    private void fillInOmegaBuilds(SortedMap<Integer, TerasologyGameVersion> gameVersionMap, SortedSet<Integer> buildNumbers, GameJob job) {
        if (logger.isInfoEnabled()) {
            logger.info("Will try to load Omega build numbers from " + job.getOmegaJobName());
        }

        if (buildNumbers.isEmpty()) {
            logger.warn("No build numbers provided for job {}. Skipping omega build resolution,", job);
            return;
        }

        // We more or less redo the original process in looking up the Omega job then later going back in history to map to the engine job
        Integer lastSuccessfulBuildNumber;
        try {
            lastSuccessfulBuildNumber = buildRepository.loadLastSuccessfulBuildNumberJenkins(job.getOmegaJobName());
        } catch (DownloadException e) {
            logger.info("Retrieving last successful Omega build number failed, unable to load Omega distributions. '{}'", job, e);
            return;
        }

        int oldestEngine = buildNumbers.first();
        logger.info("Latest successful Omega build number is {} and oldest engine we care about is {}", lastSuccessfulBuildNumber, oldestEngine);

        // Go through at the most twice as many Omega builds as we have engine builds to care about (not expecting many oddities)
        int omegaRebuild = -1;
        final Map<Integer, Integer> omegaMapping = new HashMap<>();
        for (int i = 0; i < buildNumbers.size() * 2; i++) {
            int omegaBuildNumber = lastSuccessfulBuildNumber - i;
            if (omegaBuildNumber < 1) {
                logger.warn("Searched past the beginning of an Omega line, maybe range is too long? Finishing loop.");
                break;
            }

            // See if the job exists and is successful. If not we don't care so try the next one
            if (!checkJobResult(job, omegaBuildNumber)) {
                continue;
            }

            // We have a successful Omega build. See if it was triggered directly by an engine build
            int matchingEngineBuildNumber = -1;
            try {
                // See if the job exists and is successful. If not we don't care so try the next one
                matchingEngineBuildNumber = buildRepository.loadEngineTriggerJenkins(job, omegaBuildNumber);
                if (matchingEngineBuildNumber == -1) {
                    // In this case we know there is a successful Omega build that didn't trigger from an engine build
                    // By storing the Omega number we can keep looking
                    logger.info("Parking omega build {} for claiming by a later engine", omegaBuildNumber);
                    omegaRebuild = omegaBuildNumber;
                } else {
                    // We have a valid engine trigger. Pair the two together, considering if we had a parked rebuild
                    if (omegaRebuild != -1) {
                        logger.info("Mapping engine build {} with parked Omega build {}", matchingEngineBuildNumber, omegaRebuild);
                        omegaMapping.put(matchingEngineBuildNumber, omegaRebuild);
                        omegaRebuild = -1;
                    } else {
                        //logger.debug("Mapping engine build {} with exact Omega build {}", matchingEngineBuildNumber, omegaBuildNumber);
                        omegaMapping.put(matchingEngineBuildNumber, omegaBuildNumber);
                    }

                    // See if we've searched far enough the Omega line to have matched the oldest engine we care about
                    if (matchingEngineBuildNumber <= oldestEngine) {
                        logger.info("We've reached or passed the number of engine releases we're pairing with Omega, done here");
                        break;
                    }
                }
            } catch (DownloadException e) {
                logger.info("Failed to retrieve a cause for job {} - ignoring.", job.getOmegaJobName());
            }
        }

        logger.info("Checking build number mappings with game versions ...");
        int processed = associateEngineWithOmegaBuilds(gameVersionMap, buildNumbers, omegaMapping);
        logger.info("Done (Matched " + processed + " out of " + gameVersionMap.size() + ")");
    }

    private int associateEngineWithOmegaBuilds(SortedMap<Integer, TerasologyGameVersion> gameVersionMap, SortedSet<Integer> buildNumbers, Map<Integer, Integer> omegaMapping) {
        int processed = 0;
        // TODO: Is it safe to use the entries from buildNumbers as keys or can they go out of sync vs loaded game versions?
        for (Integer engineBuildNumber : buildNumbers) {
            Integer matchingOmega = omegaMapping.get(engineBuildNumber);
            if (updateOmegaForGameVersion(gameVersionMap, engineBuildNumber, matchingOmega)) {
                processed++;
            } else {
                // TODO: Display some sort of warning for the user if this build gets selected
                logger.warn("*WARNING:* No Omega distribution found for engine build {}", engineBuildNumber);
            }
        }
        return processed;
    }

    private boolean updateOmegaForGameVersion(SortedMap<Integer, TerasologyGameVersion> gameVersionMap, Integer engineBuildNumber, Integer matchingOmega) {
        TerasologyGameVersion gameVersion = gameVersionMap.get(engineBuildNumber);
        if (gameVersion == null || matchingOmega == null) {
            logger.warn("Failed to find a game version entry for engine build {} !", engineBuildNumber);
            return false;
        } else {
            gameVersion.setOmegaNumber(matchingOmega);
            return true;
        }
    }

    /**
     * See if the job exists and is successful.
     *
     * @param job              the game job to test
     * @param omegaBuildNumber the omega build number for this job
     * @return true if the job exists and is successful, false if the result is not successful or the lookup failed
     */
    private boolean checkJobResult(GameJob job, int omegaBuildNumber) {
        try {
            JobResult jobResult = buildRepository.loadJobResultJenkins(job.getOmegaJobName(), omegaBuildNumber);
            if (jobResult != JobResult.SUCCESS) {
                logger.info("Retrieved an Omega result of {} for build number {}, skipping", jobResult, omegaBuildNumber);
                return false;
            }
        } catch (DownloadException e) {
            logger.info("Cannot find build number '{}' for job '{}'.", omegaBuildNumber, job.getOmegaJobName());
            return false;
        }
        return true;
    }

    /**
     * Given a job line and a set of build numbers see what if any existing builds we have installed locally.
     *
     * @param directory       The game directory to search
     * @param buildNumbersMap The set of build numbers to look for
     */
    private void loadInstalledGames(Path directory, Map<GameJob, SortedSet<Integer>> buildNumbersMap) {
        final Set<Path> candidateFiles = new HashSet<>();
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (path.getFileName().toString().matches(FILE_ENGINE_JAR)) {
                        logger.debug("Matched path to engine jar file: {}", path.getFileName());
                        candidateFiles.add(path);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Hit an error scanning for existing file directories: {}", e);
        }

        logger.info("Found the following existing engine install dirs: {}", candidateFiles);

        for (Path engineJar : candidateFiles) {
            final TerasologyGameVersion gameVersion = loadInstalledGameVersion(engineJar);
            if (gameVersion != null) {
                final SortedMap<Integer, TerasologyGameVersion> gameVersionMap = gameVersionMaps.get(gameVersion.getJob());
                final SortedSet<Integer> buildNumbers = buildNumbersMap.get(gameVersion.getJob());
                buildNumbers.add(gameVersion.getBuildNumber());
                if (!gameVersionMap.containsKey(gameVersion.getBuildNumber())) {
                    gameVersionMap.put(gameVersion.getBuildNumber(), gameVersion);
                } else {
                    logger.info("Installed game already loaded. '{}'", engineJar);
                }
            }
        }
    }

    private TerasologyGameVersion loadInstalledGameVersion(Path engineJar) {
        TerasologyGameVersion gameVersion = null;
        final TerasologyGameVersionInfo gameVersionInfo = TerasologyGameVersionInfo.loadFromJar(engineJar);

        if (verifyGameVersionInfo(gameVersionInfo)) {
            GameJob installedJob = null;
            try {
                installedJob = GameJob.valueOf(gameVersionInfo.getJobName());
            } catch (IllegalArgumentException e) {
                logger.error("Unknown job '{}' found for game '{}'!", gameVersionInfo.getJobName(), engineJar);
            }

            Integer installedBuildNumber = null;
            try {
                installedBuildNumber = Integer.parseInt(gameVersionInfo.getBuildNumber());
            } catch (NumberFormatException e) {
                logger.error("Could not parse build number '{}'!", gameVersionInfo.getBuildNumber());
            }

            Path terasologyJar = engineJar.getParent().resolve(FILE_TERASOLOGY_JAR);
            if (Files.notExists(terasologyJar)) {
                logger.error("Expected game jar {} did not exist at {} ! ", FILE_TERASOLOGY_JAR, terasologyJar);
            }

            if (installedJob != null && installedBuildNumber != null
                    && gameVersionInfo.getGitBranch().endsWith(installedJob.getGitBranch()) && installedJob.getMinBuildNumber() <= installedBuildNumber) {
                gameVersion = new TerasologyGameVersion();
                gameVersion.setJob(installedJob);
                gameVersion.setBuildNumber(installedBuildNumber);
                gameVersion.setInstallationPath(engineJar.getParent());
                gameVersion.setGameJar(terasologyJar);
                gameVersion.setGameVersionInfo(gameVersionInfo);
                gameVersion.setChangeLog(null);
                gameVersion.setSuccessful(Boolean.TRUE);
                gameVersion.setLatest(false);
            } else {
                logger.warn("The game version info can not be used from the file '{}' !", engineJar);
            }
        } else {
            logger.warn("The game version info can not be loaded from the file '{}' !", engineJar);
        }
        return gameVersion;
    }

    private boolean verifyGameVersionInfo(TerasologyGameVersionInfo gameVersionInfo) {
        return gameVersionInfo != null
                && gameVersionInfo.getJobName() != null && gameVersionInfo.getJobName().length() > 0
                && gameVersionInfo.getBuildNumber() != null && gameVersionInfo.getBuildNumber().length() > 0;
    }

    private static void fillBuildNumbers(SortedSet<Integer> buildNumbers, int minBuildNumber, Integer lastBuildNumber) {
        if (buildNumbers != null && !buildNumbers.isEmpty()) {
            int first = buildNumbers.first();
            if (first < minBuildNumber) {
                first = minBuildNumber;
            }
            int last = buildNumbers.last();
            if (lastBuildNumber != null && last > lastBuildNumber) {
                last = lastBuildNumber;
            }
            // Add all build numbers between first and last
            for (int buildNumber = first + 1; buildNumber < last; buildNumber++) {
                buildNumbers.add(buildNumber);
            }
        }
    }

    private SortedMap<Integer, TerasologyGameVersion> readFromCache(GameJob job, SortedSet<Integer> buildNumbers, Path cacheDirectory) {
        final SortedMap<Integer, TerasologyGameVersion> cachedGameVersions = new TreeMap<>();
        for (Integer buildNumber : buildNumbers) {
            final Path cacheFile = createCacheFile(job, buildNumber, cacheDirectory);
            try {
                if (Files.exists(cacheFile) && Files.isReadable(cacheFile) && Files.isRegularFile(cacheFile)) {
                    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(cacheFile))) {
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

    private Path createCacheFile(GameJob job, Integer buildNumber, Path cacheDirectory) {
        return cacheDirectory.resolve("TerasologyGameVersion_" + job.name() + "_" + buildNumber.toString() + FILE_SUFFIX_CACHE);
    }

    private void loadGameVersions(SortedSet<Integer> buildNumbers, GameJob job, SortedMap<Integer, TerasologyGameVersion> gameVersions,
                                  SortedMap<Integer, TerasologyGameVersion> cachedGameVersionMap) {
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
            if (cachedGameVersionMap != null && cachedGameVersionMap.containsKey(buildNumber)) {
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
            if (cachedGameVersion != null && cachedGameVersion.getSuccessful() != null) {
                gameVersion.setSuccessful(cachedGameVersion.getSuccessful());
            } else if (!job.isOnlyInstalled()) {
                Boolean successful = null;
                try {
                    JobResult jobResult = buildRepository.loadJobResultJenkins(job.name(), buildNumber);
                    successful = jobResult != null && (jobResult == JobResult.SUCCESS || jobResult == JobResult.UNSTABLE);
                } catch (DownloadException e) {
                    logger.warn("Failed to load job result (probably OK): '{}' '{}'", job, buildNumber);
                }
                gameVersion.setSuccessful(successful);
            }
        }
    }

    private void loadAndSetChangeLog(TerasologyGameVersion gameVersion, TerasologyGameVersion cachedGameVersion, GameJob job, Integer buildNumber) {
        if (gameVersion.getChangeLog() == null) {
            if (cachedGameVersion != null && cachedGameVersion.getChangeLog() != null) {
                gameVersion.setChangeLog(cachedGameVersion.getChangeLog());
            } else if (!job.isOnlyInstalled()) {
                try {
                    List<String> changeLog = buildRepository.loadChangeLogJenkins(job.name(), buildNumber);
                    if (changeLog == null || changeLog.isEmpty()) {
                        changeLog = List.of(BundleUtils.getLabel("message_noChangeLog"));
                        gameVersion.setChangeLog(Collections.unmodifiableList(changeLog));
                    }
                } catch (DownloadException e) {
                    logger.warn("Loading change log failed (probably OK). '{}' '{}'", job, buildNumber);
                }
            }
        }
    }

    protected void loadAndSetGameVersionInfo(TerasologyGameVersion gameVersion, TerasologyGameVersion cachedGameVersion, GameJob job, Integer buildNumber) {
        if (gameVersion.getGameVersionInfo() == null) {
            if (cachedGameVersion != null && cachedGameVersion.getGameVersionInfo() != null) {
                gameVersion.setGameVersionInfo(cachedGameVersion.getGameVersionInfo());
            } else if (!job.isOnlyInstalled() && (cachedGameVersion == null || gameVersion.getSuccessful() == null || gameVersion.getSuccessful())) {
                TerasologyGameVersionInfo gameVersionInfo = null;
                URL urlVersionInfo = null;
                try {
                    urlVersionInfo = buildRepository.createFileDownloadUrlJenkins(job.name(), buildNumber, IBuildRepository.ArtifactType.FILE_TERASOLOGY_GAME_VERSION_INFO);
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

    private void writeToCache(GameJob job, Path cacheDirectory) {
        try {
            final SortedMap<Integer, TerasologyGameVersion> gameVersions = gameVersionMaps.get(job);
            for (TerasologyGameVersion gameVersion : gameVersions.values()) {
                final Path cacheFile = createCacheFile(job, gameVersion.getBuildNumber(), cacheDirectory);
                try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(cacheFile))) {
                    oos.writeObject(gameVersion);
                }
            }
        } catch (IOException e) {
            logger.error("Could not write cache data!", e);
        }
    }

    private void deleteOldCache(Path cacheDirectory) {
        try (Stream<Path> directoryStream = Files.list(cacheDirectory)) {
            directoryStream.filter(path -> {
                try {
                    return Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path)
                            && Files.isWritable(path) && path.toString().endsWith(FILE_SUFFIX_CACHE)
                            && Files.getLastModifiedTime(path).toMillis() < (System.currentTimeMillis() - OUTDATED_CACHE_MILLI_SECONDS);
                } catch (IOException e) {
                    logger.error("Failed to find files to delete", e);
                    return false;
                }
            }).forEach(FileUtils::deleteFileSilently);
        } catch (IOException e) {
            logger.error("Failed to delete old cache files", e);
        }
    }

    private List<TerasologyGameVersion> createList(Integer lastBuildNumber, GameJob job, SortedMap<Integer, TerasologyGameVersion> gameVersionMap) {
        final List<TerasologyGameVersion> gameVersionList = new ArrayList<>();
        // add only available builds
        for (TerasologyGameVersion version : gameVersionMap.values()) {
            if (version.getSuccessful() != null) {
                gameVersionList.add(version);
            }
        }

        final TerasologyGameVersion latestGameVersion = new TerasologyGameVersion();
        latestGameVersion.setLatest(true);
        latestGameVersion.setJob(job);
        latestGameVersion.setBuildNumber(lastBuildNumber);
        if (lastBuildNumber != null && gameVersionMap.containsKey(lastBuildNumber)) {
            gameVersionMap.get(lastBuildNumber).copyTo(latestGameVersion);
        } else if (lastBuildNumber == null && !gameVersionMap.isEmpty()) {
            gameVersionMap.get(gameVersionMap.lastKey()).copyTo(latestGameVersion);
        }
        gameVersionList.add(latestGameVersion);

        Collections.reverse(gameVersionList);

        return Collections.unmodifiableList(gameVersionList);
    }

    /**
     * Re-checks version info after a game has been installed.
     *
     * @param terasologyDirectory The directory the game was installed to
     * @return boolean indicating success or failure of installation
     */
    public synchronized boolean updateGameVersionsAfterInstallation(Path terasologyDirectory) {
        Path engineJar = null;
        Path libsDir = terasologyDirectory.resolve(DIR_LIBS);
        if (!Files.exists(libsDir)) {
            logger.error("Failed to find the libs dir in {} - cannot update game versions", terasologyDirectory);
            return false;
        }

        try (Stream<Path> stream = Files.list(libsDir)) {
            Optional<Path> foundJar = stream.filter(f -> f.getFileName().toString().matches(FILE_ENGINE_JAR)).findAny();
            if (foundJar.isPresent()) {
                engineJar = foundJar.get();
            }
        } catch (IOException e) {
            logger.error("Failed to scan directory {} for game versioning", terasologyDirectory, e);
            return false;
        }

        if (engineJar == null) {
            logger.error("Failed to find the engine jar in game install dir {} - cannot update game versions", terasologyDirectory);
            return false;
        }

        final TerasologyGameVersion gameVersion = loadInstalledGameVersion(engineJar);
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
                if (related != null && related.isInstalled() && related.getInstallationPath().equals(gameVersion.getInstallationPath())) {
                    logger.debug("Remove installation info from related game version. '{}'", related);
                    related.setInstallationPath(null);
                    related.setGameJar(null);
                }
            } else {
                final TerasologyGameVersion latest = getGameVersionForBuildVersion(gameVersion.getJob(), TerasologyGameVersion.BUILD_VERSION_LATEST);
                if (latest != null && latest.isInstalled() && latest.getInstallationPath().equals(gameVersion.getInstallationPath())) {
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

    public synchronized List<String> getAggregatedChangeLog(TerasologyGameVersion gameVersion, int builds) {
        List<String> aggregatedChangeLog = new ArrayList<>();
        List<TerasologyGameVersion> gameVersions = gameVersionLists.get(gameVersion.getJob());
        int idx = gameVersions.indexOf(gameVersion) + 1;
        int upper = Math.min(idx + builds, gameVersions.size());
        for (int i = idx; i < upper; i++) {
            final List<String> log = gameVersions.get(i).getChangeLog();

            /* Don't include empty change logs (nothing changed) in the aggregate. */
            if (log.size() == 1) {
                final String msg = log.get(0);
                if (BundleUtils.getLabel("message_noChangeLog").equals(msg)) {
                    continue;
                }
            }

            aggregatedChangeLog.addAll(gameVersions.get(i).getChangeLog());
        }
        return aggregatedChangeLog;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + gameVersionLists + "]";
    }
}
