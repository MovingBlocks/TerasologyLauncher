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

/** @author Skaldarnar */
public class Utils {
    // TODO: collaborate with TerasologyDirectories!
    private static File workDir = null;

    public static File getWorkingDirectory() {
        if (workDir == null) {
            workDir = getWorkingDirectory("terasology");
        }
        return workDir;
    }

    private static File getWorkingDirectory(String applicationName) {
        String userHome = System.getProperty("user.home", ".");
        File workingDirectory;

        OperatingSystem os = OperatingSystem.getOS();
        if (os.isUnix()) {
            workingDirectory = new File(userHome, '.' + applicationName + '/');
        } else if (os.isWindows()) {
            String applicationData = System.getenv("APPDATA");
            if (applicationData != null) {
                workingDirectory = new File(applicationData, "." + applicationName + '/');
            } else {
                workingDirectory = new File(userHome, '.' + applicationName + '/');
            }
        } else if (os.isMac()) {
            workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
        } else {
            workingDirectory = new File(userHome, applicationName + '/');
        }
        if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs())) {
            throw new RuntimeException("The working directory could not be created: " + workingDirectory);
        }
        return workingDirectory;
    }
}
