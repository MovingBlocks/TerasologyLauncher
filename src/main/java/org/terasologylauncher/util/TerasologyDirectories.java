/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasologylauncher.util;

import java.io.File;

/**
 * This class collects references to important working directories, like game backups and launcher directory.
 *
 * @author Skaldarnar
 */
public final class TerasologyDirectories {

    public static final String BACKUP_DIR_NAME = "backups";
    public static final String LAUNCHER_DIR_NAME = "launcher";
    public static final String SAVED_WORLDS_DIR_NAME = "SAVED_WORLDS";
    public static final String SCREENSHOTS_DIR_NAME = "screens";
    public static final String MODS_DIR_NAME = "mods";


    private static final File BACKUP_DIR = new File(Utils.getWorkingDirectory(), BACKUP_DIR_NAME);
    private static final File LAUNCHER_DIR = new File(Utils.getWorkingDirectory(), LAUNCHER_DIR_NAME);

    private static final File SAVED_WORLDS_DIR = new File(Utils.getWorkingDirectory(), SAVED_WORLDS_DIR_NAME);
    private static final File SCREENSHOTS_DIR = new File(Utils.getWorkingDirectory(), SCREENSHOTS_DIR_NAME);
    private static final File MODS_DIR = new File(Utils.getWorkingDirectory(), MODS_DIR_NAME);

    private TerasologyDirectories() {
    }

    public static File getBackupDir() {
        return BACKUP_DIR;
    }

    public static File getLauncherDir() {
        return LAUNCHER_DIR;
    }

    public static File getSavedWorldsDir() {
        return SAVED_WORLDS_DIR;
    }

    public static File getScreenshotsDir() {
        return SCREENSHOTS_DIR;
    }

    public static File getModsDir() {
        return MODS_DIR;
    }
}
