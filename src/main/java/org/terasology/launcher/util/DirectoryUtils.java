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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * @author Skaldarnar
 */
public final class DirectoryUtils {

    public static final String BACKUP_DIR_NAME = "backups";
    public static final String LAUNCHER_DIR_NAME = "launcher";
    public static final String SAVED_WORLDS_DIR_NAME = "SAVED_WORLDS";
    public static final String SCREENSHOTS_DIR_NAME = "screens";
    public static final String MODS_DIR_NAME = "mods";
    public static final String LOGS_DIR_NAME = "logs";

    public static final String TMP = "tmp";

    public static final String LAUNCHER_APPLICATION_DIR_NAME = "terasologylauncher";
    public static final String GAMES_APPLICATION_DIR_NAME = "terasology";

    private static final Logger logger = LoggerFactory.getLogger(DirectoryUtils.class);

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

    public static void showInFileManager(final File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                logger.error("Could not open file/directory '{}'.", file, e);
            }
        }
    }
}
