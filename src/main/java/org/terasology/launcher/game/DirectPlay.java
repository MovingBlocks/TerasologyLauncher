// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;
import org.terasology.launcher.platform.UnsupportedPlatformException;
import org.terasology.launcher.remote.DownloadException;
import org.terasology.launcher.remote.DownloadUtils;
import org.terasology.launcher.repositories.ReleaseRepository;
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.tasks.ProgressListener;
import org.terasology.launcher.util.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * A minimal, JavaFX-free path to installing (if needed) and launching a game.
 * <p>
 * {@link GameManager} and {@link GameService} cover the same ground but are built on {@code javafx.application.Platform}
 * and {@code javafx.concurrent.Service} for the GUI's benefit - both live in the {@code javafx.graphics} module, which
 * pulls in native platform libraries and the "full" JRE this project otherwise avoids bundling for a plain "launch the
 * game directly" entry point (see {@code Terasology.exe} in build.gradle). Everything here only touches plain-Java and
 * {@code javafx.base} types (via {@link Settings}), so it works with an ordinary Java 17 runtime.
 */
public final class DirectPlay {

    private static final Logger logger = LoggerFactory.getLogger(DirectPlay.class);

    private DirectPlay() {
    }

    /**
     * Scan the game directory for installed releases, the same layout {@link GameManager} uses.
     */
    public static Set<GameIdentifier> scanInstalled(Path gameDirectory) throws IOException {
        try (var directories = Files.walk(gameDirectory, 3)) {
            return directories
                    .filter(Files::isDirectory)
                    .filter(d -> gameDirectory.relativize(d).getNameCount() == 3)
                    .map(GameInstallation::new)
                    .map(GameInstallation::getInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    /**
     * The newest installed Omega release by semantic version, or {@code null} if none is installed.
     * <p>
     * Versions that aren't valid semver (shouldn't happen for Omega releases, but directories can be
     * hand-placed) sort last rather than failing the whole comparison.
     */
    public static GameIdentifier latestInstalled(Set<GameIdentifier> installed) {
        return installed.stream()
                .filter(id -> id.getProfile() == Profile.OMEGA)
                .max(Comparator.comparing(DirectPlay::parseVersionOrMin))
                .orElse(null);
    }

    private static Semver parseVersionOrMin(GameIdentifier id) {
        Semver version = Semver.parse(id.getDisplayVersion());
        return version != null ? version : new Semver("0.0.0");
    }

    /**
     * The newest available stable Omega release from a repository, for auto-install when nothing is installed yet.
     */
    public static GameRelease latestStableRelease(ReleaseRepository releases) {
        return releases.fetchReleases().stream()
                .filter(release -> release.getId().getProfile() == Profile.OMEGA)
                .filter(release -> release.getId().getBuild() == Build.STABLE)
                .max(Comparator.comparing(GameRelease::getTimestamp))
                .orElse(null);
    }

    /**
     * Download (if not already cached) and extract a release, the same layout {@link GameManager#install} uses.
     */
    public static GameInstallation install(GameRelease release, Path cacheDirectory, Path gameDirectory, ProgressListener listener)
            throws IOException, DownloadException, InterruptedException {
        Path cachedZip = cacheDirectory.resolve(release.getFilename());
        if (Files.notExists(cachedZip)) {
            try {
                new DownloadUtils().download(release, cachedZip, listener).get();
            } catch (ExecutionException e) {
                throw new DownloadException("Download failed.", e.getCause());
            }
        }
        Path extractDir = gameDirectory
                .resolve(release.getId().getProfile().name())
                .resolve(release.getId().getBuild().name())
                .resolve(release.getId().getDisplayVersion());
        FileUtils.extractZipTo(cachedZip, extractDir);
        logger.info("Finished installing package: {}", release.getId());
        return GameInstallation.getExisting(extractDir);
    }

    public static GameInstallation getInstallation(Path gameDirectory, GameIdentifier id) throws FileNotFoundException {
        Path dir = gameDirectory.resolve(id.getProfile().name()).resolve(id.getBuild().name()).resolve(id.getDisplayVersion());
        return GameInstallation.getExisting(dir);
    }

    /**
     * Start the game and wait for it to exit.
     *
     * @return the game process's exit code
     */
    public static int launch(GameInstallation installation, Settings settings, List<String> extraGameParams)
            throws IOException, GameVersionNotSupportedException, UnsupportedPlatformException, InterruptedException {
        var gameParams = new ArrayList<>(settings.userGameParameters.get());
        gameParams.addAll(extraGameParams);

        var starter = new GameStarter(installation, settings.gameDataDirectory.get(),
                settings.minHeapSize.get(), settings.maxHeapSize.get(),
                settings.userJavaParameters.get(), gameParams, settings.logLevel.get());
        Process process = starter.call();
        return process.waitFor();
    }
}
