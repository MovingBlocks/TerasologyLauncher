/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.launcher.game.GameJob;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnlineBuildRepository implements IBuildRepository {

    private static final Logger logger = LoggerFactory.getLogger(OnlineBuildRepository.class);

    private static final String JENKINS_URL = "http://jenkins.terasology.org";
    private static final String JENKINS_JOB_URL = "http://jenkins.terasology.org/job/";
    private static final int JENKINS_TIMEOUT = 3000; // milliseconds

    private static final String BUILD_NUMBER = "/buildNumber";
    private static final String LAST_STABLE_BUILD = "/lastStableBuild";
    private static final String LAST_SUCCESSFUL_BUILD = "/lastSuccessfulBuild";

    private static final String API_JSON_RESULT = "/api/json?tree=result";
    private static final String API_JSON_CAUSE = "/api/json?tree=actions[causes[upstreamBuild]]";
    private static final String API_XML_CHANGE_LOG = "/api/xml?xpath=//changeSet/item/msg[1]&wrapper=msgs";

//    private static final String TERASOLOGY_LAUNCHER_DEVELOP_JOB_NAME = "TerasologyLauncher";
//    private static final String FILE_TERASOLOGY_LAUNCHER_VERSION_INFO =
//            "/artifact/build/resources/main/org/terasology/launcher/version/versionInfo.properties";
//    private static final String FILE_TERASOLOGY_LAUNCHER_CHANGE_LOG = "/artifact/build/distributions/CHANGELOG.txt";
//    private static final String FILE_TERASOLOGY_LAUNCHER_ZIP = "/artifact/build/distributions/TerasologyLauncher.zip";

    public OnlineBuildRepository() {
        assert false;  // not during tests!!!
    }

    @Override
    public boolean isJenkinsAvailable() {
        logger.trace("Checking Jenkins availability...");
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(JENKINS_URL).openConnection();
            try (AutoCloseable ac = conn::disconnect) {
                conn.setConnectTimeout(JENKINS_TIMEOUT);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    logger.trace("Jenkins is available at {}", JENKINS_URL);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not connect to Jenkins at {} - {}", JENKINS_URL, e.getMessage());
        }
        return false;
    }

    /**
     * Get the build number of the last stable build on the Jenkins server.
     * <p>
     * Jenkins Terminology: "A build is stable if it was built successfully and no publisher reports it as unstable. A build is unstable
     * if it was built successfully and one or more publishers report it unstable. For example if the JUnit publisher is configured and a
     * test fails then the build will be marked unstable. "
     * </p>
     *
     * @param jobName the Jenkins job name
     * @return the <code>buildNumber</code> of the last <b>stable</b> build
     * @throws DownloadException if something goes wrong
     */
    @Override
    public int loadLastStableBuildNumberJenkins(String jobName) throws DownloadException {
        return loadBuildNumberJenkins(jobName, LAST_STABLE_BUILD);
    }

    /**
     * Get the build number of the last successful build on the Jenkins server.
     * <p>
     * Jenkins Terminology: "A build is successful when the compilation reported no errors."
     * </p>
     *
     * @param jobName the Jenkins job name
     * @return the <code>buildNumber</code> of the last <b>successful</b> build
     * @throws DownloadException if something goes wrong
     */
    @Override
    public int loadLastSuccessfulBuildNumberJenkins(String jobName) throws DownloadException {
        return loadBuildNumberJenkins(jobName, LAST_SUCCESSFUL_BUILD);
    }

    private static int loadBuildNumberJenkins(String jobName, String lastBuild) throws DownloadException {
        int buildNumber;
        // The build number page in Jenkins simply contains the number and nothing else, so we can simply read and parse it
        URL urlVersion;
        try {
            urlVersion = new URL(JENKINS_JOB_URL + jobName + lastBuild + BUILD_NUMBER);
        } catch (MalformedURLException e) {
            throw new DownloadException("Failed to create jenkins download url", e);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlVersion.openStream(), StandardCharsets.US_ASCII))) {
            buildNumber = Integer.parseInt(reader.readLine());
        } catch (IOException | RuntimeException e) {
            throw new DownloadException("The build number could not be loaded! job=" + jobName + ", URL=" + urlVersion, e);
        }
        return buildNumber;
    }

    @Override
    public JobResult loadJobResultJenkins(String jobName, int buildNumber) throws DownloadException {
        JobResult jobResult = null;
        URL urlResult;
        try {
            urlResult = createUrlJenkins(jobName, buildNumber, API_JSON_RESULT);
        } catch (MalformedURLException e) {
            throw new DownloadException("Failed to create jenkins download url", e);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlResult.openStream(), StandardCharsets.US_ASCII))) {
            final String jsonResult = reader.readLine();
            if (jsonResult != null) {
                for (JobResult result : JobResult.values()) {
                    if (jsonResult.contains(result.name())) {
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
        }
        return jobResult;
    }

    @Override
    public List<String> loadChangeLogJenkins(String jobName, int buildNumber) throws DownloadException {
        List<String> changeLog = null;
        URL urlChangeLog = null;
        try {
            urlChangeLog = createUrlJenkins(jobName, buildNumber, API_XML_CHANGE_LOG);
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            try (InputStream stream = urlChangeLog.openStream()) {
                final Document document = builder.parse(stream);
                final NodeList nodeList = document.getElementsByTagName("msg");
                if (nodeList != null) {
                    changeLog = new ArrayList<String>();
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

    /**
     * Attempts to look up the cause for an Omega job in Jenkins to see if it was triggered directly by an engine job.
     *
     * @param job              The GameJob we're working with, both for the engine job name and the Omega job name
     * @param omegaBuildNumber The instance of the Omega build we care about
     * @return The engine build number as an int or -1 if parsing failed (including the case of no engine-triggered cause)
     * @throws DownloadException
     */
    @Override
    public int loadEngineTriggerJenkins(GameJob job, int omegaBuildNumber) throws DownloadException {
        int engineBuildNumber = -1;
        URL urlResult;
        try {
            urlResult = createUrlJenkins(job.getOmegaJobName(), omegaBuildNumber, API_JSON_CAUSE);
        } catch (MalformedURLException e) {
            throw new DownloadException("Failed to create jenkins download url", e);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlResult.openStream(), StandardCharsets.US_ASCII))) {
            //logger.debug("The Omega URL to check is {}", urlResult);
            final String jsonResult = reader.readLine();
            if (jsonResult != null) {
                //logger.debug("The json result was {}", jsonResult);
                // We're looking for the number in something like [{"upstreamBuild":1401}]
                String pattern = "upstreamBuild\":(\\d+)";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(jsonResult);
                if (m.find()) {
                    //logger.debug("Found regex group believed to be build number: " + m.group(1));
                    engineBuildNumber = Integer.parseInt(m.group(1));
                } else {
                    logger.info("Failed to find a matching regex group for Omega build {}, probably no engine cause", omegaBuildNumber);
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new DownloadException("There was an issue attempting to fetch the Omega url" + urlResult, e);
        }
        return engineBuildNumber;
    }

//    public String loadLauncherChangeLogJenkins(String jobName, Integer buildNumber) throws DownloadException {
//        URL urlChangeLog;
//        try {
//            urlChangeLog = createFileDownloadUrlJenkins(jobName, buildNumber, FILE_TERASOLOGY_LAUNCHER_CHANGE_LOG);
//        } catch (MalformedURLException e) {
//            throw new DownloadException("Failed to create the changelog url", e);
//        }
//        final StringBuilder changeLog = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlChangeLog.openStream(), StandardCharsets.US_ASCII))) {
//            while (true) {
//                String line = reader.readLine();
//                if (line == null) {
//                    break;
//                }
//                if (changeLog.length() > 0) {
//                    changeLog.append("\n");
//                }
//                changeLog.append(line);
//            }
//        } catch (IOException | RuntimeException e) {
//            throw new DownloadException("The launcher change log could not be loaded! job=" + jobName + ", URL=" + urlChangeLog, e);
//        }
//        return changeLog.toString();
//    }

    @Override
    public URL createFileDownloadUrlJenkins(String jobName, int buildNumber, ArtifactType fileName) throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JENKINS_JOB_URL);
        urlBuilder.append(jobName);
        urlBuilder.append("/");
        urlBuilder.append(buildNumber);
        urlBuilder.append(fileName.getValue());

        return new URL(urlBuilder.toString());
    }

    private static URL createUrlJenkins(String jobName, int buildNumber, String subPath) throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JENKINS_JOB_URL);
        urlBuilder.append(jobName);
        urlBuilder.append("/");
        urlBuilder.append(buildNumber);
        urlBuilder.append(subPath);

        return new URL(urlBuilder.toString());
    }

}
