/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class DownloadUtils {

    public static final String TERASOLOGY_LAUNCHER_NIGHTLY_JOB_NAME = "TerasologyLauncherNightly";

    public static final String FILE_TERASOLOGY_GAME_ZIP = "distributions/Terasology.zip";
    public static final String FILE_TERASOLOGY_LAUNCHER_ZIP = "distributions/TerasologyLauncher.zip";
    public static final String FILE_TERASOLOGY_GAME_VERSION_INFO = "resources/main/org/terasology/version/versionInfo.properties";
    public static final String FILE_TERASOLOGY_LAUNCHER_VERSION_INFO = "resources/main/org/terasology/launcher/version/versionInfo.properties";
    private static final String FILE_TERASOLOGY_LAUNCHER_CHANGE_LOG = "distributions/CHANGELOG.txt";

    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    private static final String JENKINS_JOB_URL = "http://jenkins.terasology.org/job/";
    private static final String LAST_STABLE_BUILD = "/lastStableBuild";
    private static final String LAST_SUCCESSFUL_BUILD = "/lastSuccessfulBuild";
    private static final String BUILD_NUMBER = "/buildNumber/";
    private static final String ARTIFACT_BUILD = "/artifact/build/";

    private static final String API_JSON_RESULT = "/api/json?tree=result";
    private static final String API_XML_CHANGE_LOG = "/api/xml?xpath=//changeSet/item/msg[1]&wrapper=msgs";

    private static final int CONNECT_TIMEOUT = 1000 * 30;
    private static final int READ_TIMEOUT = 1000 * 60 * 5;

    private DownloadUtils() {
    }

    public static void downloadToFile(URL downloadURL, File file, ProgressListener listener) throws DownloadException {
        listener.update(0);

        final HttpURLConnection connection = getConnectedDownloadConnection(downloadURL);

        final long contentLength = connection.getContentLengthLong();
        if (contentLength <= 0) {
            throw new DownloadException("Wrong content length! URL=" + downloadURL + ", contentLength=" + contentLength);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Download file '{}' ({}; {}) from URL '{}'.", file, contentLength, connection.getContentType(), downloadURL);
        }

        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            downloadToFile(listener, contentLength, in, out);
        } catch (IOException e) {
            throw new DownloadException("Could not download file from URL! URL=" + downloadURL + ", file=" + file, e);
        } finally {
            connection.disconnect();
        }

        if (!listener.isCancelled()) {
            if (file.length() != contentLength) {
                throw new DownloadException("Wrong file length after download! " + file.length() + " != " + contentLength);
            }
            listener.update(100);
        }
    }

    private static HttpURLConnection getConnectedDownloadConnection(URL downloadURL) throws DownloadException {
        final HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) downloadURL.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.connect();
        } catch (ClassCastException | IOException e) {
            throw new DownloadException("Could not open/connect HTTP-URL connection! URL=" + downloadURL, e);
        }
        return connection;
    }

    private static void downloadToFile(ProgressListener listener, long contentLength, BufferedInputStream in, BufferedOutputStream out) throws IOException {
        final byte[] buffer = new byte[2048];
        final float sizeFactor = 100f / contentLength;
        long writtenBytes = 0;
        int n;
        if (!listener.isCancelled()) {
            while ((n = in.read(buffer)) != -1) {
                if (listener.isCancelled()) {
                    break;
                }

                out.write(buffer, 0, n);
                writtenBytes += n;

                int percentage = (int) (sizeFactor * writtenBytes);
                if (percentage < 1) {
                    percentage = 1;
                } else if (percentage >= 100) {
                    percentage = 99;
                }
                listener.update(percentage);

                if (listener.isCancelled()) {
                    break;
                }
            }
        }
    }

    public static URL createFileDownloadUrlJenkins(String jobName, int buildNumber, String fileName) throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JENKINS_JOB_URL);
        urlBuilder.append(jobName);
        urlBuilder.append("/");
        urlBuilder.append(buildNumber);
        urlBuilder.append(ARTIFACT_BUILD);
        urlBuilder.append(fileName);

        return new URL(urlBuilder.toString());
    }

    public static URL createUrlJenkins(String jobName, int buildNumber, String subPath) throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JENKINS_JOB_URL);
        urlBuilder.append(jobName);
        urlBuilder.append("/");
        urlBuilder.append(buildNumber);
        urlBuilder.append(subPath);

        return new URL(urlBuilder.toString());
    }

    /**
     * Loads the <code>buildNumber</code> of the last <b>stable</b> build.
     */
    public static int loadLastStableBuildNumberJenkins(String jobName) throws DownloadException {
        return loadBuildNumberJenkins(jobName, LAST_STABLE_BUILD);
    }

    /**
     * Loads the <code>buildNumber</code> of the last <b>successful</b> build.
     */
    public static int loadLastSuccessfulBuildNumberJenkins(String jobName) throws DownloadException {
        return loadBuildNumberJenkins(jobName, LAST_SUCCESSFUL_BUILD);
    }

    private static int loadBuildNumberJenkins(String jobName, String lastBuild) throws DownloadException {
        int buildNumber;
        URL urlVersion = null;
        BufferedReader reader = null;
        try {
            urlVersion = new URL(JENKINS_JOB_URL + jobName + lastBuild + BUILD_NUMBER);
            reader = new BufferedReader(new InputStreamReader(urlVersion.openStream(), StandardCharsets.US_ASCII));
            buildNumber = Integer.parseInt(reader.readLine());
        } catch (IOException | RuntimeException e) {
            throw new DownloadException("The build number could not be loaded! job=" + jobName + ", URL=" + urlVersion, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Closing BufferedReader for '{}' failed!", urlVersion, e);
                }
            }
        }
        return buildNumber;
    }

    public static JobResult loadJobResultJenkins(String jobName, int buildNumber) throws DownloadException {
        JobResult jobResult = null;
        URL urlResult = null;
        BufferedReader reader = null;
        try {
            urlResult = DownloadUtils.createUrlJenkins(jobName, buildNumber, API_JSON_RESULT);
            reader = new BufferedReader(new InputStreamReader(urlResult.openStream(), StandardCharsets.US_ASCII));
            final String jsonResult = reader.readLine();
            if (jsonResult != null) {
                for (JobResult result : JobResult.values()) {
                    if (jsonResult.indexOf(result.name()) == 11) {
                        jobResult = result;
                        break;
                    }
                }
                if (jobResult == null) {
                    logger.error("Unknown job result '{}' for '{}'!", jsonResult, urlResult);
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new DownloadException("The job result could not be loaded! job=" + jobName + ", URL=" + urlResult, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Closing BufferedReader for '{}' failed!", urlResult, e);
                }
            }
        }
        return jobResult;
    }

    public static List<String> loadChangeLogJenkins(String jobName, int buildNumber) throws DownloadException {
        List<String> changeLog = null;
        URL urlChangeLog = null;
        try {
            urlChangeLog = DownloadUtils.createUrlJenkins(jobName, buildNumber, API_XML_CHANGE_LOG);
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            try (InputStream stream = urlChangeLog.openStream()) {
                final Document document = builder.parse(stream);
                final NodeList nodeList = document.getElementsByTagName("msg");
                if (nodeList != null) {
                    changeLog = new ArrayList<>();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        final Node item = nodeList.item(i);
                        if (item != null) {
                            final Node lastChild = item.getLastChild();
                            if (lastChild != null) {
                                final String textContent = lastChild.getTextContent();
                                if ((textContent != null) && (textContent.trim().length() > 0)) {
                                    changeLog.add(textContent.trim());
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | RuntimeException e) {
            throw new DownloadException("The change log could not be loaded! job=" + jobName + ", URL=" + urlChangeLog, e);
        }
        return changeLog;
    }

    public static String loadLauncherChangeLogJenkins(String jobName, Integer buildNumber) throws DownloadException {
        URL urlChangeLog = null;
        BufferedReader reader = null;
        final StringBuilder changeLog = new StringBuilder();
        try {
            urlChangeLog = DownloadUtils.createFileDownloadUrlJenkins(jobName, buildNumber, FILE_TERASOLOGY_LAUNCHER_CHANGE_LOG);
            reader = new BufferedReader(new InputStreamReader(urlChangeLog.openStream(), StandardCharsets.US_ASCII));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (changeLog.length() > 0) {
                    changeLog.append("\n");
                }
                changeLog.append(line);
            }
        } catch (IOException | RuntimeException e) {
            throw new DownloadException("The launcher change log could not be loaded! job=" + jobName + ", URL=" + urlChangeLog, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Closing BufferedReader for '{}' failed!", urlChangeLog, e);
                }
            }
        }
        return changeLog.toString();
    }
}
