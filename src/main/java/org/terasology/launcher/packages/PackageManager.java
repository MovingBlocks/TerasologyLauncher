/*
 * Copyright 2019 MovingBlocks
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

package org.terasology.launcher.packages;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Handles installation, removal and update of game packages.
 */
public class PackageManager {

    private static final Logger logger = LoggerFactory.getLogger(PackageManager.class);
    private static final String INSTALL_DIRECTORY = "games";
    private static final String SOURCES_FILENAME = "sources.json";
    private static final String SOURCES_SCHEMA = "schema.json";
    private static final String DATABASE_FILENAME = "packages.db";
    private static final String CACHE_DIRECTORY = "cache";

    private final Repository onlineRepository;
    private LocalRepository localRepository;
    private PackageDatabase database;
    private Path cacheDir;
    private Path installDir;
    private Path sourcesFile;

    public PackageManager(Path launcherDir, Path gameDir) {
        onlineRepository = new JenkinsRepository();
        cacheDir = launcherDir.resolve(CACHE_DIRECTORY);
        installDir = gameDir.resolve(INSTALL_DIRECTORY);
        sourcesFile = launcherDir.resolve(SOURCES_FILENAME);
    }

    /**
     * Sets up local storage for working with game packages and cache files.
     *
     * @param gameDirectory directory path for storing game packages
     * @param cacheDirectory directory path for storing cache files
     */
    public void initLocalStorage(Path gameDirectory, Path cacheDirectory) {
        try {
            FileUtils.ensureWritableDir(gameDirectory);
            FileUtils.ensureWritableDir(cacheDirectory);
            localRepository = new LocalRepository(gameDirectory, cacheDirectory);
            localRepository.loadCache();
        } catch (IOException e) {
            logger.error("Error initialising local storage: {}", e.getMessage());
        }
    }

    /**
     * Synchronizes the cached game version list with the list of game versions
     * available online.
     */
    public void sync() {
        Objects.requireNonNull(localRepository, "Local storage uninitialized");

        for (PackageBuild pkgBuild : PackageBuild.values()) {
            final List<Integer> versions = onlineRepository.getPackageVersions(pkgBuild);
            logger.debug("Versions for job {}: {}", pkgBuild.getJobName(), versions.toString());
            localRepository.updateCache(pkgBuild, versions);
        }
        localRepository.saveCache();
    }

    /**
     * Checks if the sources file contains any syntax errors or
     * schema violations. Note that the default values are copied
     * over and used when no sources file is found.
     *
     * @return whether the sources file is valid
     */
    public boolean validateSources() {
        if (Files.notExists(sourcesFile)) {
            logger.warn("sources.json not found: {}", sourcesFile);
            copyDefaultSources();
            return true;
        }

        logger.trace("Validating sources.json using its schema");
        try (
                InputStream schemaIn = getClass().getResourceAsStream(SOURCES_SCHEMA);
                InputStream jsonIn = Files.newInputStream(sourcesFile)
        ) {
            final Schema schema = SchemaLoader.builder()
                    .schemaClient(SchemaClient.classPathAwareClient())
                    .resolutionScope("classpath://org/terasology/launcher/packages/schema.json")
                    .schemaJson(new JSONObject(new JSONTokener(schemaIn)))
                    .build().load().build();

            schema.validate(new JSONArray(new JSONTokener(jsonIn)));
            return true;
        } catch (ValidationException e) {
            logger.error("sources.json has invalid value at: {}", e.getPointerToViolation());
        } catch (JSONException e) {
            logger.error("sources.json has syntax error: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("Failed to validate sources.json: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Copies the default (bundled) sources file into the appropriate
     * directory, overwriting any existing sources file if necessary.
     */
    public void copyDefaultSources() {
        logger.info("Copying default sources file to {}", sourcesFile);
        try {
            Files.copy(getClass().getResourceAsStream(SOURCES_FILENAME),
                    sourcesFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to copy default sources file to {}", sourcesFile);
            throw new RuntimeException("Default sources file could not be copied to " + sourcesFile);
        }
    }

    /**
     * Initializes a new {@link PackageDatabase}.
     */
    public void initDatabase() {
        database = new PackageDatabase(
                sourcesFile,
                sourcesFile.resolveSibling(DATABASE_FILENAME),
                installDir
        );
    }

    // TODO: Replace similar methods
    public void syncDatabase() {
        Objects.requireNonNull(database)
                .sync();
    }

    /**
     * Installs the mentioned package into the local repository.
     *
     * @param target the package to be installed
     * @param listener the object which is to be informed about task progress
     */
    public void install(Package target, ProgressListener listener) throws IOException, DownloadException {
        final Path cachedZip = cacheDir.resolve(target.zipName());

        // TODO: Properly validate cache and handle exceptions
        if (Files.notExists(cachedZip)) {
            download(target, cachedZip, listener);
        }

        if (!listener.isCancelled()) {
            final Path extractDir = installDir.resolve(target.getId()).resolve(target.getVersion());
            FileUtils.extractZipTo(cachedZip, extractDir);
            target.setInstalled(true);
            logger.info("Finished installing package: {}-{}", target.getId(), target.getVersion());
        }
    }

    private void download(Package target, Path cacheZip, ProgressListener listener) throws DownloadException, IOException {
        final URL downloadUrl = new URL(target.getUrl());

        final long contentLength = DownloadUtils.getContentLength(downloadUrl);
        final long availableSpace = cacheZip.getParent().toFile().getUsableSpace();

        if (availableSpace >= contentLength) {
            final Path cacheZipPart = cacheZip.resolveSibling(target.zipName() + ".part");
            Files.deleteIfExists(cacheZipPart);
            DownloadUtils.downloadToFile(downloadUrl, cacheZipPart, listener);

            if (!listener.isCancelled()) {
                Files.move(cacheZipPart, cacheZip, StandardCopyOption.ATOMIC_MOVE);
            }
        } else {
            throw new DownloadException("Insufficient space for downloading package");
        }

        logger.info("Finished downloading package: {}-{}", target.getId(), target.getVersion());
        // TODO: Implement download functionality locally
    }

    /**
     * Removes the mentioned package from the local repository.
     *
     * @param target the package to be removed
     */
    public void remove(Package target) throws IOException {
        // Recursively delete all files
        Files.walk(resolveInstallDir(target))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        target.setInstalled(false);
        logger.info("Finished removing package: {}-{}", target.getId(), target.getVersion());
    }

    /**
     * Provides a list of all packages tracked by the package
     * database.
     *
     * @return the list of all packages
     */
    public List<Package> getPackages() {
        return Objects.requireNonNull(database)
                .getPackages();
    }

    /**
     * Provides the list of package versions available for a build by querying
     * the cached version list.
     *
     * @param pkgBuild the build of the game packages
     * @return the list of versions available for that package
     */
    public List<Integer> getPackageVersions(PackageBuild pkgBuild) {
        return Objects.requireNonNull(localRepository, "Local storage uninitialized")
                .getPackageVersions(pkgBuild);
    }

    public Path resolveInstallDir(Package target) {
        return installDir.resolve(target.getId()).resolve(target.getVersion());
    }
}
