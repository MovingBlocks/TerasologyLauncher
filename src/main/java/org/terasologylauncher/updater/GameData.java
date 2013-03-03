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
import org.terasologylauncher.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * The GameData class provides access to information on the installed game version and type, if an internet connection is available and
 * whether the game could be updated to a newer version.
 *
 * @author Skaldarnar
 */
public class GameData {

    private static final Logger logger = LoggerFactory.getLogger(GameData.class);

    public static final String JENKINS = "http://jenkins.movingblocks.net/job/";
    public static final String STABLE_JOB_NAME = "TerasologyStable";
    public static final String NIGHTLY_JOB_NAME = "Terasology";
    public static final String LAST_SUCCESSFUL_BUILD_NUMBER = "lastSuccessfulBuild/buildNumber";

    private static File gameJar;

    private static int upstreamVersionStable = -1;
    private static int upstreamVersionNightly = -1;

    private static BuildType installedBuildType;
    private static int installedBuildVersion = -1;

    public static boolean isGameInstalled() {
        return getGameJar().exists();
    }

    public static File getGameJar() {
        if (gameJar == null) {
            gameJar = new File(Utils.getWorkingDirectory(), "Terasology.jar");
        }
        return gameJar;
    }

    public static int getUpStreamNightlyVersion() {
        if (upstreamVersionNightly == -1) {
            URL url;
            try {
                url = new URL(JENKINS + NIGHTLY_JOB_NAME + "/" + LAST_SUCCESSFUL_BUILD_NUMBER);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                upstreamVersionNightly = Integer.parseInt(in.readLine());
                try {
                    in.close();
                } catch (Exception e) {
                    logger.info("Closing failed", e);
                }
            } catch (MalformedURLException e) {
                logger.error("Could not read nightly version!", e);
            } catch (IOException e) {
                logger.error("Could not read nightly version!", e);
            }
        }
        return upstreamVersionNightly;
    }

    public static int getUpStreamStableVersion() {
        if (upstreamVersionStable == -1) {
            URL url;
            try {
                url = new URL(JENKINS + STABLE_JOB_NAME + "/" + LAST_SUCCESSFUL_BUILD_NUMBER);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                upstreamVersionStable = Integer.parseInt(in.readLine());
                try {
                    in.close();
                } catch (Exception e) {
                    logger.info("Closing failed", e);
                }
            } catch (MalformedURLException e) {
                logger.error("Could not read stable version!", e);
            } catch (IOException e) {
                logger.error("Could not read stable version!", e);
            }
        }
        return upstreamVersionStable;
    }

    public static int getUpStreamVersion(BuildType type) {
        switch (type) {
            case STABLE:
                return getUpStreamStableVersion();
            case NIGHTLY:
                return getUpStreamNightlyVersion();
        }
        return -1;
    }

    public static boolean checkInternetConnection() {
        //TODO: test jenkins and terasologymods.net
        try {
            final URL testURL = new URL("http://www.google.com");
            final URLConnection connection = testURL.openConnection();
            connection.setConnectTimeout(5000);
            connection.getInputStream();
            return true;
        } catch (MalformedURLException e) {
            logger.error("Could not check internet connection!", e);
        } catch (IOException e) {
            logger.info("No internet connection.", e);
        }
        return false;
    }

    public static BuildType getInstalledBuildType() {
        if (installedBuildType == null) {
            readVersionFile();
        }
        return installedBuildType;
    }

    public static int getInstalledBuildVersion() {
        if (installedBuildVersion == -1) {
            readVersionFile();
        }
        return installedBuildVersion;
    }

    private static void readVersionFile() {
        // TODO Wrong version file. This is the human readable file. Replace with "versionInfo.properties"
        try {
            File installedVersionFile = new File(Utils.getWorkingDirectory(), "VERSION");
            if (installedVersionFile.isFile()) {
                Scanner scanner = new Scanner(installedVersionFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("Build number:")) {
                        installedBuildVersion = Integer.parseInt(line.split(":")[ 1 ].trim());
                    } else if (line.contains("GIT branch:")) {
                        String branch = line.split(":")[ 1 ].trim();
                        if (branch.equals("develop")) {
                            installedBuildType = BuildType.NIGHTLY;
                        } else {
                            installedBuildType = BuildType.STABLE;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not read version file!", e);
        }
    }

    public static void forceReReadVersionFile() {
        readVersionFile();
    }
}
