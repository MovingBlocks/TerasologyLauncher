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
public class LocalRepository implements Repository {

    private static final Logger logger = LoggerFactory.getLogger(LocalRepository.class);

    private static final String VERSIONS_CACHE_FILENAME = "versions.cache";

    private final Path gameDirectory;
    private final Path cacheDirectory;
    private final Path versionsCache;
    private final Map<GamePackageType, List<Integer>> cachedVersions;

    LocalRepository(Path gameDirectory, Path cacheDirectory) {
        this.gameDirectory = gameDirectory;
        this.cacheDirectory = cacheDirectory;
        versionsCache = cacheDirectory.resolve(VERSIONS_CACHE_FILENAME);
        cachedVersions = new EnumMap<>(GamePackageType.class);
    }

    void updateCache(GamePackageType pkgType, List<Integer> versions) {
        cachedVersions.put(pkgType, versions);
        logger.trace("Updating cached version list for {} ...", pkgType);
    }

    void saveCache() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(versionsCache))) {
            out.writeObject(cachedVersions);
            logger.info("Saved cache file: {}", versionsCache.toAbsolutePath());
        } catch (IOException e) {
            logger.warn("Failed to save cache file: {}", versionsCache.toAbsolutePath());
        }
    }

    void loadCache() {
        // TODO: Do this in a background thread
        if (Files.exists(versionsCache)) {
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(versionsCache))) {
                cachedVersions.putAll(
                        (Map<GamePackageType, List<Integer>>) in.readObject());
                logger.info("Loaded cache file: {}", versionsCache.toAbsolutePath());
            } catch (ClassNotFoundException | IOException e) {
                logger.warn("Failed to load cache file: {}", versionsCache.toAbsolutePath());
            }
        }
    }

    void install(GamePackage pkg) {
        // TODO: Implement this
    }

    void remove(GamePackage pkg) {
        // TODO: Implement this
    }

    @Override
    public List<Integer> getPackageVersions(GamePackageType pkgType) {
        return cachedVersions.getOrDefault(pkgType, Collections.emptyList());
    }

    @Override
    public Optional<GamePackage> getPackage(GamePackageType pkgType, int version) {
        // TODO: Implement this
        return Optional.empty();
    }
}
