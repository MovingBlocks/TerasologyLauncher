// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.DirectPlay;
import org.terasology.launcher.game.GameInstallation;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.platform.Platform;
import org.terasology.launcher.platform.UnsupportedPlatformException;
import org.terasology.launcher.repositories.CombinedRepository;
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.tasks.ProgressListener;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.LauncherDirectoryUtils;
import org.terasology.launcher.util.LauncherManagedDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Entry point for {@code Terasology.exe} - launches the game directly, skipping the {@link TerasologyLauncher} GUI.
 * <p>
 * If a game is already installed, the most recently played one (falling back to the newest installed) is launched
 * immediately, no network access needed. Otherwise, the newest stable Omega release is installed first. Command line
 * arguments are passed through to the game, same as {@code Terasology.bat} in the game's own distribution.
 * <p>
 * Deliberately independent of {@code javafx.graphics} (see {@link DirectPlay}) - unlike {@link TerasologyLauncher},
 * this entry point only needs a plain Java 17 runtime, not one bundling JavaFX.
 */
public final class Terasology {

    private static final Logger logger = LoggerFactory.getLogger(Terasology.class);

    private Terasology() {
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    private static int run(String[] args) {
        final Platform platform;
        try {
            platform = Platform.getPlatform();
        } catch (UnsupportedPlatformException e) {
            logger.error("Unsupported OS or architecture: {}", e.getMessage());
            return 1;
        }

        try {
            Path installationDirectory = LauncherDirectoryUtils.getInstallationDirectory();
            Path userDataDirectory = LauncherDirectoryUtils.getApplicationDirectory(
                    platform, LauncherDirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
            Path cacheDirectory = prepare(LauncherManagedDirectory.CACHE, userDataDirectory);
            Path gameDirectory = prepare(LauncherManagedDirectory.GAMES, installationDirectory);

            Settings settings = Settings.load(userDataDirectory);
            if (settings == null) {
                settings = Settings.getDefault();
            }
            if (settings.gameDataDirectory.get() == null) {
                Path defaultGameData = LauncherDirectoryUtils.getGameDataDirectory(platform);
                FileUtils.ensureWritableDir(defaultGameData);
                settings.gameDataDirectory.set(defaultGameData);
            }

            GameInstallation installation = resolveInstallation(settings, cacheDirectory, gameDirectory);
            if (installation == null) {
                logger.error("No game installed, and no stable Omega release could be found to install.");
                return 1;
            }

            Settings.store(settings, userDataDirectory);

            logger.info("Launching game from {}", installation.getPath());
            return DirectPlay.launch(installation, settings, Arrays.asList(args));
        } catch (IOException | InterruptedException | UnsupportedPlatformException e) {
            logger.error("The game could not be started!", e);
            return 1;
        }
    }

    /**
     * Find (or install) the game to launch: the last-played version if it's still installed, else the newest
     * installed version, else a freshly auto-installed latest stable release.
     */
    private static GameInstallation resolveInstallation(Settings settings, Path cacheDirectory, Path gameDirectory)
            throws IOException, InterruptedException {
        Set<GameIdentifier> installed = DirectPlay.scanInstalled(gameDirectory);

        GameIdentifier lastPlayed = settings.lastPlayedGameVersion.get();
        GameIdentifier toLaunch = (lastPlayed != null && installed.contains(lastPlayed))
                ? lastPlayed
                : DirectPlay.latestInstalled(installed);

        if (toLaunch != null) {
            return DirectPlay.getInstallation(gameDirectory, toLaunch);
        }

        logger.info("No installed game found, fetching available releases ...");
        var client = new OkHttpClient.Builder()
                .cache(new Cache(cacheDirectory.toFile(), 10L * 1024L * 1024L /*10 MiB*/))
                .callTimeout(10, TimeUnit.SECONDS)
                .build();
        CombinedRepository releaseRepository = new CombinedRepository(client);
        GameRelease release = DirectPlay.latestStableRelease(releaseRepository);
        if (release == null) {
            return null;
        }

        logger.info("Installing {} ...", release.getId());
        GameInstallation installation = DirectPlay.install(release, cacheDirectory, gameDirectory, new LoggingProgressListener());
        settings.lastPlayedGameVersion.set(release.getId());
        return installation;
    }

    private static Path prepare(LauncherManagedDirectory directory, Path base) throws IOException {
        Path path = directory.getDirectoryPath(base);
        for (var creator : directory.getCreators()) {
            creator.apply(path);
        }
        return path;
    }

    private static final class LoggingProgressListener implements ProgressListener {
        private int lastLoggedPercent = -1;

        @Override
        public void update() {
        }

        @Override
        public void update(int progress) {
            if (progress != lastLoggedPercent) {
                lastLoggedPercent = progress;
                logger.info("Downloading... {}%", progress);
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
