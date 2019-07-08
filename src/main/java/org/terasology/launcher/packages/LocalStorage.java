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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides game packages from the local filesystem.
 */
public class LocalStorage implements Storage {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorage.class);

    private static final String VERSIONS_CACHE_FILENAME = "versions.cache";

    private final Path versionsCache;
    private final Map<GamePackageType, List<Integer>> cachedVersions;

    LocalStorage(Path localDir) {
        versionsCache = localDir.resolve(VERSIONS_CACHE_FILENAME);

        final Map<GamePackageType, List<Integer>> cache = loadCache();
        if (!cache.isEmpty()) {
            cachedVersions = cache;
        } else {
            cachedVersions = new EnumMap<>(GamePackageType.class);
            for (GamePackageType pkgType : GamePackageType.values()) {
                cachedVersions.put(pkgType, Collections.emptyList());
            }
        }
    }

    void updateCache(GamePackageType pkgType, List<Integer> versions) {
        cachedVersions.put(pkgType, versions);
        logger.trace("Updating cached version list for {} ...", pkgType);
        saveCache();
    }

    private void saveCache() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(versionsCache))) {
            out.writeObject(cachedVersions);
        } catch (IOException e) {
            logger.warn("Failed to write cache file: {}", versionsCache.toAbsolutePath());
        }
    }

    private Map<GamePackageType, List<Integer>> loadCache() {
        // TODO: Do this in a background thread
        try (ObjectInputStream in = new ObjectInputStream(
                Files.newInputStream(versionsCache))) {
            return (Map<GamePackageType, List<Integer>>) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            logger.warn("Failed to load cache file: {}", versionsCache.toAbsolutePath());
        }
        return Collections.emptyMap();
    }

    void install(GamePackage pkg) {
        // TODO: Implement this
    }

    void remove(GamePackage pkg) {
        // TODO: Implement this
    }

    @Override
    public List<Integer> getPackageVersions(GamePackageType pkgType) {
        // TODO: Implement this
        return Collections.emptyList();
    }

    @Override
    public Optional<GamePackage> getPackage(GamePackageType pkgType, int version) {
        // TODO: Implement this
        return Optional.empty();
    }
}
