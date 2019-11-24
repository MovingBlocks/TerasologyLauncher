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

import org.terasology.launcher.packages.PackageManager;
import org.terasology.launcher.settings.BaseLauncherSettings;

import java.nio.file.Path;

/**
 * Immutable launcher configuration object.
 *
 * Provides information on
 *  - directories managed by the launcher
 *  - user settings in form of {@link BaseLauncherSettings}
 *  - the {@link PackageManager} used to download new games
 */
@Deprecated
public class LauncherConfiguration {

    private final Path launcherDirectory;
    private final Path downloadDirectory;
    private final Path tempDirectory;
    private final Path cacheDirectory;
    private final BaseLauncherSettings launcherSettings;
    private final PackageManager packageManager;

    public LauncherConfiguration(final Path launcherDirectory,
                                 final Path downloadDirectory,
                                 final Path tempDirectory,
                                 final Path cacheDirectory,
                                 final BaseLauncherSettings launcherSettings,
                                 final PackageManager packageManager) {
        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.tempDirectory = tempDirectory;
        this.cacheDirectory = cacheDirectory;
        this.launcherSettings = launcherSettings;
        this.packageManager = packageManager;
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

    public Path getCacheDirectory() {
        return cacheDirectory;
    }

    public BaseLauncherSettings getLauncherSettings() {
        return launcherSettings;
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }
}
