// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;
import org.terasology.launcher.tasks.ProgressListener;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Objects;

public class GameManager {

    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);

    private final Path cacheDirectory;
    private final Path installDirectory;

    //TODO: should this be a map to installation metadata (install date, path, ...)?
    private final ObservableSet<GameIdentifier> installedGames;

    public GameManager(Path cacheDirectory, Path installDirectory) {
        this.cacheDirectory = cacheDirectory;
        this.installDirectory = installDirectory;
        installedGames = FXCollections.observableSet();
        scanInstallationDir();
    }

    /**
     * Installs the given release to the local file system.
     *
     * @param release  the game release to be installed
     * @param listener the object which is to be informed about task progress
     */
    public void install(GameRelease release, ProgressListener listener) throws IOException, DownloadException {
        final String file = "terasology-" + release.getId().getProfile().toString().toLowerCase() + "-" + release.getId().getVersion() + "-" + release.getId().getBuild().toString().toLowerCase() + ".zip";
        final Path cachedZip = cacheDirectory.resolve(file);

        // TODO: Properly validate cache and handle exceptions
        if (Files.notExists(cachedZip)) {
            download(release, cachedZip, listener);
        }

        if (!listener.isCancelled()) {
            final Path extractDir = getInstallDirectory(release.getId());
            FileUtils.extractZipTo(cachedZip, extractDir);
            installedGames.add(release.getId());
            logger.info("Finished installing package: {}", release.getId());
        }
    }

    private void download(GameRelease release, Path targetLocation, ProgressListener listener) throws DownloadException, IOException {
        final URL downloadUrl = release.getUrl();

        final long contentLength = DownloadUtils.getContentLength(downloadUrl);
        final long availableSpace = targetLocation.getParent().toFile().getUsableSpace();

        if (availableSpace >= contentLength) {
            final Path cacheZipPart = targetLocation.resolveSibling(targetLocation.getFileName().toString() + ".part");
            Files.deleteIfExists(cacheZipPart);
            DownloadUtils.downloadToFile(downloadUrl, cacheZipPart, listener);

            if (!listener.isCancelled()) {
                Files.move(cacheZipPart, targetLocation, StandardCopyOption.ATOMIC_MOVE);
            }
        } else {
            throw new DownloadException("Insufficient space for downloading package");
        }

        logger.info("Finished downloading package: {}", release.getId());
    }

    /**
     * Removes the given release from the local file system.
     *
     * @param game the game release to be removed
     */
    public void remove(GameIdentifier game) throws IOException {
        // Recursively delete all files
        Files.walk(getInstallDirectory(game))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        installedGames.remove(game);
        logger.info("Finished removing package: {}", game);
    }

    /**
     * Retrieve the set of installed games as observable set.
     *
     * @return the set of all installed game releases
     */
    public ObservableSet<GameIdentifier> getInstalledGames() {
        return installedGames;
    }

    public Path getInstallDirectory(GameIdentifier id) {
        return installDirectory.resolve(id.getProfile().name()).resolve(id.getBuild().name()).resolve(id.getVersion());
    }

    /**
     * Scans the installation directory and collects the installed games.
     */
    private void scanInstallationDir() {
        if (Files.exists(installDirectory)) {
            for (File profileDirectory : Objects.requireNonNull(installDirectory.toFile().listFiles())) {
                Profile profile;
                try {
                    profile = Profile.valueOf(profileDirectory.getName());
                } catch (IllegalArgumentException e) {
                    continue;
                }
                for (File buildDirectory : Objects.requireNonNull(profileDirectory.listFiles())) {
                    Build build;
                    try {
                        build = Build.valueOf(buildDirectory.getName());
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    for (File versionDirectory : Objects.requireNonNull(buildDirectory.listFiles())) {
                        String version = versionDirectory.getName();
                        installedGames.add(new GameIdentifier(version, build, profile));
                    }
                }
            }
        }
    }
}
