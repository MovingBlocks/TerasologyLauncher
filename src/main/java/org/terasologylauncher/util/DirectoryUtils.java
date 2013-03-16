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
import java.io.IOException;

/** @author Skaldarnar */
public final class DirectoryUtils {

    public static final String BACKUP_DIR_NAME = "backups";
    public static final String LAUNCHER_DIR_NAME = "launcher";
    public static final String SAVED_WORLDS_DIR_NAME = "SAVED_WORLDS";
    public static final String SCREENSHOTS_DIR_NAME = "screens";
    public static final String MODS_DIR_NAME = "mods";

    public static final String TMP = "tmp";

    private static final String APPLICATION_DIR_NAME = "terasology";

    private DirectoryUtils() {
    }

    public static void checkDirectory(File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Can not create directory! " + directory);
        }

        if (!directory.isDirectory()) {
            throw new IOException("Directory is not a directory! " + directory);
        }

        if (!directory.canRead() || !directory.canWrite() || !directory.canExecute()) {
            throw new IOException("Can not read/write/execute directory! " + directory);
        }
    }

    public static File getApplicationDirectory(final OSUtils os) {
        final String userHome = System.getProperty("user.home", ".");
        File applicationDirectory;

        if (os.isUnix()) {
            applicationDirectory = new File(userHome, '.' + APPLICATION_DIR_NAME + '/');
        } else if (os.isWindows()) {
            final String applicationData = System.getenv("APPDATA");
            if (applicationData != null) {
                applicationDirectory = new File(applicationData, "." + APPLICATION_DIR_NAME + '/');
            } else {
                applicationDirectory = new File(userHome, '.' + APPLICATION_DIR_NAME + '/');
            }
        } else if (os.isMac()) {
            applicationDirectory = new File(userHome, "Library/Application Support/" + APPLICATION_DIR_NAME);
        } else {
            applicationDirectory = new File(userHome, APPLICATION_DIR_NAME + '/');
        }

        return applicationDirectory;
    }
}
