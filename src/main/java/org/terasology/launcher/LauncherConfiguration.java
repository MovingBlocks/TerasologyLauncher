/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.launcher;

import org.terasology.launcher.local.GameManager;
import org.terasology.launcher.repositories.RepositoryManager;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.settings.LauncherSettings;

import java.nio.file.Path;

/**
 * Immutable launcher configuration object.
 * <p>
 * Provides information on
 * - directories managed by the launcher
 * - user settings in form of {@link BaseLauncherSettings}
 */
public class LauncherConfiguration {

    private final Path launcherDirectory;
    private final Path downloadDirectory;
    private final LauncherSettings launcherSettings;
    private final GameManager gameManager;
    private final RepositoryManager repositoryManager;

    public LauncherConfiguration(final Path launcherDirectory,
                                 final Path downloadDirectory,
                                 final LauncherSettings launcherSettings,
                                 GameManager gameManager, RepositoryManager repositoryManager) {
        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.launcherSettings = launcherSettings;
        this.gameManager = gameManager;
        this.repositoryManager = repositoryManager;
    }

    public Path getLauncherDirectory() {
        return launcherDirectory;
    }

    public Path getDownloadDirectory() {
        return downloadDirectory;
    }

    public LauncherSettings getLauncherSettings() {
        return launcherSettings;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }
}
