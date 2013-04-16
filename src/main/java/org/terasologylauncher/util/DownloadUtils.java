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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.version.TerasologyLauncherVersionInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public final class DownloadUtils {

    public static final String TERASOLOGY_STABLE_JOB_NAME = "TerasologyStable";
    public static final String TERASOLOGY_NIGHTLY_JOB_NAME = "Terasology";
    public static final String TERASOLOGY_LAUNCHER_STABLE_JOB_NAME = "TerasologyLauncherStable";
    public static final String TERASOLOGY_LAUNCHER_NIGHTLY_JOB_NAME = "TerasologyLauncherNightly";

    public static final String FILE_TERASOLOGY_GAME_ZIP = "distributions/Terasology.zip";
    public static final String FILE_TERASOLOGY_LAUNCHER_ZIP = "distributions/TerasologyLauncher.zip";
    public static final String FILE_TERASOLOGY_GAME_VERSION_INFO =
        "resources/main/org/terasology/version/versionInfo.properties";
    public static final String FILE_TERASOLOGY_LAUNCHER_VERSION_INFO =
        "resources/main/org/terasologylauncher/version/versionInfo.properties";

    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    private static final String JENKINS_JOB_URL = "http://jenkins.movingblocks.net/job/";
    private static final String LAST_STABLE_BUILD = "/lastStableBuild";
    private static final String LAST_SUCCESSFUL_BUILD = "/lastSuccessfulBuild";
    private static final String BUILD_NUMBER = "/buildNumber/";
    private static final String ARTIFACT_BUILD = "/artifact/build/";

    private DownloadUtils() {
    }

    /**
     * Download the file from the given URL and store it to the specified file.
     *
     * @param downloadURL - remote location of file to download
     * @param file        - where to store downloaded file
     * @throws DownloadException
     */
    public static void downloadToFile(final URL downloadURL, final File file) throws DownloadException {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(downloadURL.openStream());
            out = new BufferedOutputStream(new FileOutputStream(file));

            final byte[] buffer = new byte[2048];

            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }

            out.flush();
        } catch (IOException e) {
            throw new DownloadException("Could not download file! URL='" + downloadURL + "', file='" + file + "'", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.warn("Closing InputStream for '{}' failed!", downloadURL, e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.warn("Closing OutputStream for '{}' failed!", file, e);
                }
            }
        }
    }

    public static int loadLatestStableVersion(final String jobName) throws DownloadException {
        return loadVersion(jobName, LAST_STABLE_BUILD);
    }

    public static int loadLatestSuccessfulVersion(final String jobName) throws DownloadException {
        return loadVersion(jobName, LAST_SUCCESSFUL_BUILD);
    }

    private static int loadVersion(final String jobName, final String latestBuild) throws DownloadException {
        int version;
        URL urlVersion = null;
        BufferedReader reader = null;
        try {
            urlVersion = new URL(JENKINS_JOB_URL + jobName + latestBuild + BUILD_NUMBER);
            reader = new BufferedReader(new InputStreamReader(urlVersion.openStream()));
            version = Integer.parseInt(reader.readLine());
        } catch (MalformedURLException e) {
            throw new DownloadException("The version could not be loaded! " + jobName + " " + urlVersion, e);
        } catch (IOException e) {
            throw new DownloadException("The version could not be loaded! " + jobName + " " + urlVersion, e);
        } catch (RuntimeException e) {
            // NullPointerException, NumberFormatException
            throw new DownloadException("The version could not be loaded! " + jobName + " " + urlVersion, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Closing reader for '{}' failed!", urlVersion, e);
                }
            }
        }
        return version;
    }

    public static TerasologyLauncherVersionInfo loadTerasologyLauncherVersionInfo(final String jobName,
                                                                                  final Integer version)
        throws DownloadException {
        URL urlVersionInfo = null;
        TerasologyLauncherVersionInfo versionInfo;
        try {
            urlVersionInfo = DownloadUtils.getDownloadURL(jobName, version, FILE_TERASOLOGY_LAUNCHER_VERSION_INFO);
            versionInfo = TerasologyLauncherVersionInfo.loadFromInputStream(urlVersionInfo.openStream());
        } catch (MalformedURLException e) {
            throw new DownloadException("The version info could not be loaded! " + jobName + " " + urlVersionInfo, e);
        } catch (IOException e) {
            throw new DownloadException("The version info could not be loaded! " + jobName + " " + urlVersionInfo, e);
        }
        return versionInfo;
    }

    public static URL getDownloadURL(final String jobName, final Integer version, final String fileName)
        throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JENKINS_JOB_URL);
        urlBuilder.append(jobName);
        urlBuilder.append("/");
        urlBuilder.append(version);
        urlBuilder.append(ARTIFACT_BUILD);
        urlBuilder.append(fileName);

        return new URL(urlBuilder.toString());
    }

    public static URL getURL(final String jobName, final Integer version, final String subPath)
        throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JENKINS_JOB_URL);
        urlBuilder.append(jobName);
        urlBuilder.append("/");
        urlBuilder.append(version);
        urlBuilder.append(subPath);

        return new URL(urlBuilder.toString());
    }
}
