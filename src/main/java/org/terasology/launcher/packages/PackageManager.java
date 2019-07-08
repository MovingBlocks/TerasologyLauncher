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

import java.nio.file.Path;

/**
 * Handles installation, removal and update of game packages.
 */
public class PackageManager {
    private final Storage onlineStorage;
    private final LocalStorage localStorage;

    public PackageManager(Path localDir) {
        onlineStorage = new JenkinsStorage();
        localStorage = new LocalStorage(localDir);
    }

    public void sync() {
        for (GamePackageType pkgType : GamePackageType.values()) {
            localStorage.updateCache(pkgType, onlineStorage.getPackageVersions(pkgType));
        }
    }

    public void install(GamePackageType pkgType, int version) {
        // TODO: Install via cache
    }

    public void remove(GamePackageType pkgType, int version) {
        localStorage.getPackage(pkgType, version)
                .ifPresent(localStorage::remove);
    }
}
