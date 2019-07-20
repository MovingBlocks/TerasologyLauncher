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
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.util.DirectoryUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Handles installation, removal and update of game packages.
 */
public class PackageManager {

    private static final Logger logger = LoggerFactory.getLogger(PackageManager.class);

    private final Storage onlineStorage;
    private LocalStorage localStorage;

    public PackageManager() {
        onlineStorage = new JenkinsStorage();
    }

    public void initLocalStorage(Path gameDirectory, Path cacheDirectory) {
        try {
            DirectoryUtils.checkDirectory(gameDirectory);
            DirectoryUtils.checkDirectory(cacheDirectory);
            localStorage = new LocalStorage(gameDirectory, cacheDirectory);
            localStorage.loadCache();
        } catch (IOException e) {
            logger.error("Error initialising local storage: {}", e.getMessage());
        }
    }

    public void sync() {
        Objects.requireNonNull(localStorage, "Local storage uninitialized");

        for (GamePackageType pkgType : GamePackageType.values()) {
            localStorage.updateCache(pkgType, onlineStorage.getPackageVersions(pkgType));
        }
        localStorage.saveCache();
    }

    public void install(GamePackageType pkgType, int version) {
        // TODO: Install via cache
    }

    public void remove(GamePackageType pkgType, int version) {
        Objects.requireNonNull(localStorage, "Local storage uninitialized")
                .getPackage(pkgType, version)
                .ifPresent(localStorage::remove);
    }

    public List<Integer> getPackageVersions(GamePackageType pkgType) {
        return Objects.requireNonNull(localStorage, "Local storage uninitialized")
                .getPackageVersions(pkgType);
    }
}
