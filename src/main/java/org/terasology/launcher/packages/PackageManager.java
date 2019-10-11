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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.DirectoryUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Handles installation, removal and update of game packages.
 */
public class PackageManager {

    private static final Logger logger = LoggerFactory.getLogger(PackageManager.class);

    private final Repository onlineRepository;
    private LocalRepository localRepository;
    private PackageDatabase database;

    public PackageManager() {
        onlineRepository = new JenkinsRepository();
    }

    /**
     * Sets up local storage for working with game packages and cache files.
     *
     * @param gameDirectory directory path for storing game packages
     * @param cacheDirectory directory path for storing cache files
     */
    public void initLocalStorage(Path gameDirectory, Path cacheDirectory) {
        try {
            DirectoryUtils.checkDirectory(gameDirectory);
            DirectoryUtils.checkDirectory(cacheDirectory);
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

    // TODO: Move to constructor
    public void initDatabase(Path launcherDir) {
        database = new PackageDatabase(launcherDir);
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
     */
    public void install(Package target) {
        // TODO: Implement this
    }

    /**
     * Removes the mentioned package from the local repository.
     *
     * @param target the package to be removed
     */
    public void remove(Package target) {
        // TODO: Implement this
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
}
