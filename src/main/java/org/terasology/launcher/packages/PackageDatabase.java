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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Provides package details from all online and local repositories.
 */
class PackageDatabase {

    private static final Logger logger = LoggerFactory.getLogger(PackageDatabase.class);
    private static final String PACKAGES_DIRECTORY = "packages";
    private static final String SOURCES_FILENAME = "sources.json";
    private static final String DATABASE_FILENAME = "packages.db";

    private final Path sourcesFile;
    private final Path databaseFile;
    private final Gson gson;
    private final Map<Repository, List<Package>> database;

    PackageDatabase(Path launcherDir) {
        sourcesFile = launcherDir.resolve(PACKAGES_DIRECTORY).resolve(SOURCES_FILENAME);
        databaseFile = launcherDir.resolve(PACKAGES_DIRECTORY).resolve(DATABASE_FILENAME);
        gson = new Gson();

        database = Files.exists(databaseFile) ? loadDatabase() : new HashMap<>();
    }

    /**
     * Fetches package metadata from all repositories specified in {@link #sourcesFile}.
     */
    void sync() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(sourcesFile))
        )) {
            for (Repository source : gson.fromJson(reader, Repository[].class)) {
                database.put(source, getPackages(source));
            }
        } catch (IOException e) {
            logger.error("Failed to read sources: {}", sourcesFile.toAbsolutePath());
            logger.warn("Aborting database synchronisation");
        }
    }

    private List<Package> getPackages(Repository source) {
        return Objects.requireNonNull(RepositoryHandler.ofType(source.type))
                .getPackages(source);
    }

    private Map<Repository, List<Package>> loadDatabase() {
        try (ObjectInputStream in = new ObjectInputStream(
                Files.newInputStream(databaseFile)
        )) {
            return (Map<Repository, List<Package>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load database file: {}", databaseFile.toAbsolutePath());
        }
        return new HashMap<>();
    }

    private void saveDatabase() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(databaseFile)
        )) {
            out.writeObject(database);
        } catch (IOException e) {
            logger.error("Failed to write database file: {}", databaseFile.toAbsolutePath());
        }
    }

    static class Repository implements Serializable {
        private String url;
        private String type;
        private String[] trackedPackages;

        String getUrl() {
            return url;
        }

        String[] getTrackedPackages() {
            return trackedPackages;
        }
    }
}
