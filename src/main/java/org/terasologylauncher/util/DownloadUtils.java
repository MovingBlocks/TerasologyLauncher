/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasologylauncher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public final class DownloadUtils {

    public static final String TERASOLOGY_STABLE_JOB_NAME = "TerasologyStable";
    public static final String TERASOLOGY_NIGHTLY_JOB_NAME = "Terasology";
    public static final String TERASOLOGY_LAUNCHER_STABLE_JOB_NAME = "TerasologyLauncherStable";
    public static final String TERASOLOGY_LAUNCHER_NIGHTLY_JOB_NAME = "TerasologyLauncher";

    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    private static final String JENKINS = "http://jenkins.movingblocks.net/job/";
    private static final String LAST_SUCCESSFUL_BUILD_NUMBER = "lastSuccessfulBuild/buildNumber";
    private static final String LAST_SUCCESSFUL_BUILD = "lastSuccessfulBuild";
    private static final String ARTIFACT = "/artifact/build/distributions/";

    private DownloadUtils() {
    }

    /**
     * Download the file from the given URL and store it to the specified file.
     *
     * @param downloadURL - remote location of file to download
     * @param file        - where to store downloaded file
     * @throws IOException
     */
    public static void downloadToFile(URL downloadURL, File file) throws IOException {
        InputStream in = downloadURL.openStream();
        OutputStream out = new FileOutputStream(file);
        final byte[] buffer = new byte[2048];

        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }

        // TODO try/catch/finally and close

        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }

    public static int loadVersion(final String jobName) {
        int version = -1;
        final URL url;
        try {
            url = new URL(JENKINS + jobName + "/" + LAST_SUCCESSFUL_BUILD_NUMBER);
            final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            version = Integer.parseInt(in.readLine());
            try {
                in.close();
            } catch (Exception e) {
                logger.info("Closing failed", e);
            }
        } catch (MalformedURLException e) {
            logger.error("Could not read version!", e);
        } catch (IOException e) {
            logger.error("Could not read version!", e);
        }
        return version;
    }

    public static URL getLatestDownloadURL(final String jobName, final String fileName) throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JENKINS);
        urlBuilder.append(jobName);
        urlBuilder.append("/");
        urlBuilder.append(LAST_SUCCESSFUL_BUILD);
        urlBuilder.append(ARTIFACT);
        urlBuilder.append(fileName);

        return new URL(urlBuilder.toString());
    }

    public static URL getDownloadURL(final String jobName, final Integer version, final String fileName)
        throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JENKINS);
        urlBuilder.append(jobName);
        urlBuilder.append("/");
        urlBuilder.append(version);
        urlBuilder.append(ARTIFACT);
        urlBuilder.append(fileName);

        return new URL(urlBuilder.toString());
    }
}
