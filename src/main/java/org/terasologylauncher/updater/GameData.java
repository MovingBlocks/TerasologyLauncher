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
import org.terasologylauncher.util.DownloadUtils;
import org.terasologylauncher.version.TerasologyGameVersionInfo;

import java.io.File;

/**
 * The GameData class provides access to information on the installed game version and type.
 *
 * @author Skaldarnar
 */
public final class GameData {

    private static final Logger logger = LoggerFactory.getLogger(GameData.class);

    private static final String TERASOLOGY_JAR = "Terasology.jar";

    private static File gameJar;
    private static BuildType installedBuildType;
    private static int installedBuildVersion = -1;
    private static TerasologyGameVersionInfo versionInfo;

    private GameData() {
    }

    public static boolean isGameInstalled(final File terasologyDirectory) {
        return getGameJar(terasologyDirectory).exists();
    }

    public static File getGameJar(final File terasologyDirectory) {
        if (gameJar == null) {
            gameJar = new File(terasologyDirectory, TERASOLOGY_JAR);
        }
        return gameJar;
    }

    public static BuildType getInstalledBuildType(final File terasologyDirectory) {
        if (versionInfo == null) {
            readVersionFile(terasologyDirectory);
        }
        return installedBuildType;
    }

    public static int getInstalledBuildVersion(final File terasologyDirectory) {
        if (versionInfo == null) {
            readVersionFile(terasologyDirectory);
        }
        return installedBuildVersion;
    }

    public static TerasologyGameVersionInfo getVersionInfo(final File terasologyDirectory) {
        if (versionInfo == null) {
            readVersionFile(terasologyDirectory);
        }
        return versionInfo;
    }

    private static void readVersionFile(final File terasologyDirectory) {
        installedBuildType = null;
        installedBuildVersion = -1;
        versionInfo = TerasologyGameVersionInfo.loadFromJar(getGameJar(terasologyDirectory));

        if ((versionInfo.getJobName() != null) && (versionInfo.getJobName().length() > 0)) {
            if (versionInfo.getJobName().equals(DownloadUtils.TERASOLOGY_STABLE_JOB_NAME)) {
                installedBuildType = BuildType.STABLE;
            } else if (versionInfo.getJobName().equals(DownloadUtils.TERASOLOGY_NIGHTLY_JOB_NAME)) {
                installedBuildType = BuildType.NIGHTLY;
            }
        }

        if ((installedBuildType == null) && (versionInfo.getGitBranch() != null) &&
            (versionInfo.getGitBranch().length() > 0)) {
            if (versionInfo.getGitBranch().equals("master")) {
                installedBuildType = BuildType.STABLE;
            } else if (versionInfo.getGitBranch().equals("develop")) {
                installedBuildType = BuildType.NIGHTLY;
            }
        }

        if ((versionInfo.getBuildNumber() != null) && (versionInfo.getBuildNumber().length() > 0)) {
            try {
                installedBuildVersion = Integer.parseInt(versionInfo.getBuildNumber());
            } catch (NumberFormatException e) {
                logger.error("Could not parse build number! " + versionInfo.getBuildNumber(), e);
            }
        }
    }

    public static void forceReReadVersionFile(final File terasologyDirectory) {
        readVersionFile(terasologyDirectory);
    }
}
