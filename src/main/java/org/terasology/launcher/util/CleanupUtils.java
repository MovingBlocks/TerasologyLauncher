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

package org.terasology.launcher.util;

import org.terasology.launcher.settings.AbstractLauncherSettings;

import java.io.File;

/**
 * Utilities to remove directories created by the launcher.
 */
public final class CleanupUtils {

    public enum Directory {
        LAUNCHER,
        GAME,
        GAME_DATA
    }

    private CleanupUtils() {
    }

    public static File getDirectory(AbstractLauncherSettings launcherSettings, Directory directory) {
        switch (directory) {
            case LAUNCHER:
                return launcherSettings.getLauncherSettingsFile().getParentFile();
            case GAME:
                return launcherSettings.getGameDirectory();
            case GAME_DATA:
                return launcherSettings.getGameDataDirectory();
        }
        throw new IllegalArgumentException("Requested invalid directory");
    }

}
