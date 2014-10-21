/*
 * Copyright 2014 MovingBlocks
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

import java.io.File;

public class LauncherConfiguration {
    private final File launcherDirectory;
    private final File downloadDirectory;
    private final File tempDirectory;
    private final LauncherSettings launcherSettings;
    private final TerasologyGameVersions gameVersions;

    public LauncherConfiguration(final File launcherDirectory, final File downloadDirectory, final File tempDirectory, final LauncherSettings launcherSettings,
                                 final TerasologyGameVersions gameVersions) {
        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.tempDirectory = tempDirectory;
        this.launcherSettings = launcherSettings;
        this.gameVersions = gameVersions;
    }

    public File getLauncherDirectory() {
        return launcherDirectory;
    }

    public File getDownloadDirectory() {
        return downloadDirectory;
    }

    public File getTempDirectory() {
        return tempDirectory;
    }

    public LauncherSettings getLauncherSettings() {
        return launcherSettings;
    }

    public TerasologyGameVersions getGameVersions() {
        return gameVersions;
    }
}
