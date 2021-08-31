// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher;

import org.terasology.launcher.game.GameManager;
import org.terasology.launcher.repositories.RepositoryManager;
import org.terasology.launcher.settings.LegacyLauncherSettings;
import org.terasology.launcher.settings.Settings;

import java.nio.file.Path;

/**
 * Immutable launcher configuration object.
 *
 * Provides information on
 * - directories managed by the launcher
 * - user settings in form of {@link LegacyLauncherSettings}
 */
public class LauncherConfiguration {

    private final Path launcherDirectory;
    private final Path downloadDirectory;
    private final Settings settings;
    private final GameManager gameManager;
    private final RepositoryManager repositoryManager;

    public LauncherConfiguration(final Path launcherDirectory,
                                 final Path downloadDirectory,
                                 final Settings settings,
                                 GameManager gameManager, RepositoryManager repositoryManager) {
        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.settings = settings;
        this.gameManager = gameManager;
        this.repositoryManager = repositoryManager;
    }

    public Path getLauncherDirectory() {
        return launcherDirectory;
    }

    public Path getDownloadDirectory() {
        return downloadDirectory;
    }

    public Settings getLauncherSettings() {
        return settings;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }
}
