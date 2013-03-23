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

package org.terasologylauncher.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.BuildType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * The GameData class provides access to information on the installed game version and type, if an internet connection
 * is available and whether the game could be updated to a newer version.
 *
 * @author Skaldarnar
 */
public final class GameData {

    private static final Logger logger = LoggerFactory.getLogger(GameData.class);

    private static File gameJar;

    private static BuildType installedBuildType;
    private static int installedBuildVersion = -1;

    private GameData() {
    }

    public static boolean isGameInstalled(final File terasologyDirectory) {
        return getGameJar(terasologyDirectory).exists();
    }

    public static File getGameJar(final File terasologyDirectory) {
        if (gameJar == null) {
            gameJar = new File(terasologyDirectory, "Terasology.jar");
        }
        return gameJar;
    }

    public static BuildType getInstalledBuildType(final File terasologyDirectory) {
        if (installedBuildType == null) {
            readVersionFile(terasologyDirectory);
        }
        return installedBuildType;
    }

    public static int getInstalledBuildVersion(final File terasologyDirectory) {
        if (installedBuildVersion == -1) {
            readVersionFile(terasologyDirectory);
        }
        return installedBuildVersion;
    }

    private static void readVersionFile(final File terasologyDirectory) {
        // TODO Wrong version file. This is the human readable file. Replace with "versionInfo.properties"
        try {
            final File installedVersionFile = new File(terasologyDirectory, "VERSION");
            if (installedVersionFile.isFile()) {
                final Scanner scanner = new Scanner(installedVersionFile);
                while (scanner.hasNextLine()) {
                    final String line = scanner.nextLine();
                    if (line.contains("Build number:")) {
                        installedBuildVersion = Integer.parseInt(line.split(":")[1].trim());
                    } else if (line.contains("GIT branch:")) {
                        final String branch = line.split(":")[1].trim();
                        if (branch.equals("develop")) {
                            installedBuildType = BuildType.NIGHTLY;
                        } else {
                            installedBuildType = BuildType.STABLE;
                        }
                    }
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not read version file!", e);
        }
    }

    public static void forceReReadVersionFile(final File terasologyDirectory) {
        readVersionFile(terasologyDirectory);
    }
}
