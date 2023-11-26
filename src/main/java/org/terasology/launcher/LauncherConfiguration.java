// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher;

import org.terasology.launcher.game.GameManager;
import org.terasology.launcher.repositories.ReleaseRepository;
import org.terasology.launcher.settings.Settings;

import java.nio.file.Path;

/**
 * Immutable launcher configuration object.
 *
 * Provides information on
 * - directories managed by the launcher
 * - user settings in form of {@link Settings}
 */
public class LauncherConfiguration {

    private final Path launcherDirectory;
    private final Path downloadDirectory;
    private final Settings launcherSettings;
    private final GameManager gameManager;
    private final ReleaseRepository releaseRepository;

    public LauncherConfiguration(final Path launcherDirectory,
                                 final Path downloadDirectory,
                                 final Settings launcherSettings,
                                 GameManager gameManager, ReleaseRepository releaseRepository) {
        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.launcherSettings = launcherSettings;
        this.gameManager = gameManager;
        this.releaseRepository = releaseRepository;
    }

    public Path getLauncherDirectory() {
        return launcherDirectory;
    }

    public Path getDownloadDirectory() {
        return downloadDirectory;
    }

    public Settings getLauncherSettings() {
        return launcherSettings;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ReleaseRepository getReleaseRepository() {
        return releaseRepository;
    }
}
