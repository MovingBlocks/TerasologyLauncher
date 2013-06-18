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

package org.terasology.launcher.util;

import java.io.File;
import java.io.IOException;

public final class DirectoryUtils {

    public static final String LAUNCHER_APPLICATION_DIR_NAME = "terasologylauncher";
    public static final String GAMES_APPLICATION_DIR_NAME = "terasology";
    public static final String DOWNLOAD_DIR_NAME = "download";

    private static final String PROPERTY_USER_HOME = "user.home";

    private DirectoryUtils() {
    }

    public static void checkDirectory(final File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Can not create directory! " + directory);
        }

        if (!directory.isDirectory()) {
            throw new IOException("Directory is not a directory! " + directory);
        }

        if (!directory.canRead() || !directory.canWrite()) {
            throw new IOException("Can not read from or write into directory! " + directory);
        }
    }

    /**
     * Should only be executed once at the start.
     */
    public static File getApplicationDirectory(final OperatingSystem os, final String applicationName) {
        final String userHome = System.getProperty(PROPERTY_USER_HOME, ".");
        File applicationDirectory;

        if (os.isUnix()) {
            applicationDirectory = new File(userHome, '.' + applicationName + '/');
        } else if (os.isWindows()) {
            final String applicationData = System.getenv("APPDATA");
            if (applicationData != null) {
                applicationDirectory = new File(applicationData, "." + applicationName + '/');
            } else {
                applicationDirectory = new File(userHome, '.' + applicationName + '/');
            }
        } else if (os.isMac()) {
            applicationDirectory = new File(userHome, "Library/Application Support/" + applicationName);
        } else {
            applicationDirectory = new File(userHome, applicationName + '/');
        }

        return applicationDirectory;
    }
}
