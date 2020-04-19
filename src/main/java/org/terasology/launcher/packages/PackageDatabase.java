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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.packages.db.DatabaseRepositoryDeserializer;
import org.terasology.launcher.packages.db.RepositoryConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides package details from all online and local repositories.
 */
class PackageDatabase {

    private static final Logger logger = LoggerFactory.getLogger(PackageDatabase.class);

    private final Path databaseFile;
    private final Path installDir;
    private final Gson gson;
    private final List<Package> database;

    PackageDatabase(Path databaseFile, Path installDir) {
        this.databaseFile = databaseFile;
        this.installDir = installDir;
        gson = new GsonBuilder()
                .registerTypeAdapter(RepositoryConfiguration.class, new DatabaseRepositoryDeserializer())
                .create();
        database = loadDatabase();
        markInstalled();
    }

    /**
     * Fetches details of all packages for each repository specified in {@code sourcesFile}.
     *
     * @param sourcesFile
     */
    void sync(Path sourcesFile) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(sourcesFile))
        )) {
            final List<Package> newDatabase = new LinkedList<>();
            // TODO: read the DatabaseRepository list before-hand and pass to sync()
            for (RepositoryConfiguration source : gson.fromJson(reader, RepositoryConfiguration[].class)) {
                logger.trace("Fetching package list from: {}", source.getUrl());
                newDatabase.addAll(packageListOf(source));
            }

            database.clear();
            database.addAll(newDatabase);
        } catch (IOException | JsonSyntaxException e) {
            logger.error("Failed to read sources file '{}': {}", sourcesFile, e.getMessage());
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

    private List<Package> packageListOf(RepositoryConfiguration source) {
        final Repository repository;
        switch (source.getType().toLowerCase()) {
            case "jenkins":
                repository = new JenkinsRepository();
                break;
            default:
                repository = null;
        }

        return Objects.requireNonNull(repository, "Invalid repository type")
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
    
    Optional<Package> getLatestInstalledPackageForId(String packageId) {
        return database.stream()
                        .filter(pkg -> pkg.getId().equals(packageId) && pkg.isInstalled())
                        .sorted(Comparator.comparing(Package::getVersion).reversed())
                        .findFirst();
    }

}
