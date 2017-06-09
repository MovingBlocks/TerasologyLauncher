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

import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.settings.BaseLauncherSettings;

import java.nio.file.Path;

public class LauncherConfiguration {

    private final Path launcherDirectory;
    private final Path downloadDirectory;
    private final Path tempDirectory;
    private final BaseLauncherSettings launcherSettings;
    private final TerasologyGameVersions gameVersions;

    public LauncherConfiguration(final Path launcherDirectory, final Path downloadDirectory, final Path tempDirectory, final BaseLauncherSettings launcherSettings,
                                 final TerasologyGameVersions gameVersions) {
        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.tempDirectory = tempDirectory;
        this.launcherSettings = launcherSettings;
        this.gameVersions = gameVersions;
    }

    public Path getLauncherDirectory() {
        return launcherDirectory;
    }

    public Path getDownloadDirectory() {
        return downloadDirectory;
    }

    public Path getTempDirectory() {
        return tempDirectory;
    }

    public BaseLauncherSettings getLauncherSettings() {
        return launcherSettings;
    }

    public TerasologyGameVersions getGameVersions() {
        return gameVersions;
    }
}
