// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.remote.DownloadException;
import org.terasology.launcher.remote.DownloadUtils;
import org.terasology.launcher.remote.RemoteResource;
import org.terasology.launcher.tasks.ProgressListener;
import org.terasology.launcher.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GameManager {

    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);

    private final Path cacheDirectory;
    private final Path installDirectory;

    //TODO: should this be a map to installation metadata (install date, path, ...)?
    private final ObservableSet<GameIdentifier> installedGames;

    /**
     * Create a game manager and immediately scan the installation directory for installed games.
     *
     * @param cacheDirectory directory for cached downloads
     * @param installDirectory directory for installed games
     */
    public GameManager(Path cacheDirectory, Path installDirectory) {
        this.cacheDirectory = cacheDirectory;
        this.installDirectory = installDirectory;
        installedGames = FXCollections.observableSet();
        //TODO: separate IO operation/remote call from construction of the manager object?
        scanInstallationDir();
    }

    /**
     * Installs the given release to the local file system.
     *
     * @param release  the game release to be installed
     * @param listener the object which is to be informed about task progress
     */
    public void install(GameRelease release, ProgressListener listener) throws IOException, DownloadException, InterruptedException {
        final Path cachedZip = cacheDirectory.resolve(release.getFilename());

        // TODO: Properly validate cache and handle exceptions
        if (Files.notExists(cachedZip)) {
            download(release, cachedZip, listener);
        }

        if (!listener.isCancelled()) {
            final Path extractDir = getInstallDirectory(release.getId());
            FileUtils.extractZipTo(cachedZip, extractDir);
            Platform.runLater(() -> installedGames.add(release.getId()));
            logger.info("Finished installing package: {}", release.getId());
        }
    }

    /**
     * @deprecated Use {@link DownloadUtils#download(RemoteResource, Path, ProgressListener)} instead.
     */
    @Deprecated
    private void download(GameRelease release, Path targetLocation, ProgressListener listener)
            throws DownloadException, IOException, InterruptedException {
        DownloadUtils downloader = new DownloadUtils();
        try {
            downloader.download(release, targetLocation, listener).get();
        } catch (ExecutionException e) {
            throw new DownloadException("Download failed.", e.getCause());
        }
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

        Platform.runLater(() -> installedGames.remove(game));
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
        return installDirectory.resolve(id.getProfile().name()).resolve(id.getBuild().name()).resolve(id.getDisplayVersion());
    }

    public GameInstallation getInstallation(GameIdentifier id) throws FileNotFoundException {
        return GameInstallation.getExisting(getInstallDirectory(id));
    }

    /**
     * Scans the installation directory and collects the installed games.
     */
    private void scanInstallationDir() {
        Set<GameIdentifier> localGames;
        try (var directories = Files.walk(installDirectory, 3)) {
            var gameDirectories = directories
                    .filter(Files::isDirectory)
                    // Skip the intermediate directories.
                    .filter(d -> installDirectory.relativize(d).getNameCount() == 3);
            localGames = gameDirectories
                    .map(GameInstallation::new)
                    .map(GameInstallation::getInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            logger.warn("Error while scanning installation directory {}:", installDirectory, e);
            return;
        }
        Platform.runLater(() -> installedGames.addAll(localGames));
    }
}
