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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Provides package details from all online and local repositories.
 */
class PackageDatabase {

    private static final Logger logger = LoggerFactory.getLogger(PackageDatabase.class);

    private final Path sourcesFile;
    private final Path databaseFile;
    private final Path installDir;
    private final Gson gson;
    private final List<Package> database;

    PackageDatabase(Path sourcesFile, Path databaseFile, Path installDir) {
        this.sourcesFile = sourcesFile;
        this.databaseFile = databaseFile;
        this.installDir = installDir;
        gson = new Gson();
        database = loadDatabase();
        markInstalled();
    }

    /**
     * Fetches details of all packages for each repository specified
     * in {@link #sourcesFile}.
     */
    void sync() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(sourcesFile))
        )) {
            final List<Package> newDatabase = new LinkedList<>();
            for (Repository source : gson.fromJson(reader, Repository[].class)) {
                logger.trace("Fetching package list from: {}", source.url);
                newDatabase.addAll(packageListOf(source));
            }

            database.clear();
            database.addAll(newDatabase);
        } catch (IOException e) {
            logger.error("Failed to read sources: {}", sourcesFile);
            logger.warn("Aborting database synchronisation");
        } finally {
            markInstalled();
            saveDatabase();
        }
    }

    /**
     * Scans the installation directory and marks the
     * detected packages as installed.
     */
    private void markInstalled() {
        if (Files.exists(installDir)) {
            for (File pkgDir : Objects.requireNonNull(installDir.toFile().listFiles())) {
                for (File versionDir : Objects.requireNonNull(pkgDir.listFiles())) {
                    database.stream()
                            .filter(pkg -> pkg.getId().equals(pkgDir.getName())
                                    && pkg.getVersion().equals(versionDir.getName()))
                            .findFirst()
                            .ifPresent(pkg -> pkg.setInstalled(true));
                }
            }
        }
    }

    private List<Package> packageListOf(Repository source) {
        return Objects.requireNonNull(RepositoryHandler.ofType(source.type), "Invalid repository type")
                .getPackageList(source);
    }

    private List<Package> loadDatabase() {
        if (Files.exists(databaseFile)) {
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(databaseFile)
            )) {
                return (List<Package>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Failed to load database file: {}", databaseFile);
            }
        }
        logger.info("Using empty database");
        return new LinkedList<>();
    }

    private void saveDatabase() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(databaseFile)
        )) {
            out.writeObject(database);
            logger.info("Saved database file: {}", databaseFile);
        } catch (IOException e) {
            logger.error("Failed to write database file: {}", databaseFile);
        }
    }

    List<Package> getPackages() {
        return Collections.unmodifiableList(database);
    }

    static class PackageMetadata implements Serializable {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    static class Repository implements Serializable {
        private String url;
        private String type;
        private PackageMetadata[] trackedPackages;

        String getUrl() {
            return url;
        }

        PackageMetadata[] getTrackedPackages() {
            return trackedPackages;
        }
    }
}
