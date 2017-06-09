/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.launcher.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/***
 * Contains version data for a single instance of a Terasology engine build, parsed out of the main jar.
 */
public final class TerasologyGameVersionInfo implements Serializable {

    private static final long serialVersionUID = 4L;

    private static final Logger logger = LoggerFactory.getLogger(TerasologyGameVersionInfo.class);

    private static final String VERSION_INFO_FILE = "org/terasology/version/versionInfo.properties";

    private static final String BUILD_NUMBER = "buildNumber";
    private static final String BUILD_ID = "buildId";
    private static final String BUILD_TAG = "buildTag";
    private static final String BUILD_URL = "buildUrl";
    private static final String JOB_NAME = "jobName";
    private static final String GIT_BRANCH = "gitBranch";
    private static final String GIT_COMMIT = "gitCommit";
    private static final String DATE_TIME = "dateTime";
    private static final String DISPLAY_VERSION = "displayVersion";
    private static final String ENGINE_VERSION = "engineVersion";

    private static final String DEFAULT_VALUE = "";

    private final String buildNumber;
    private final String buildId;
    private final String buildTag;
    private final String buildUrl;
    private final String jobName;
    private final String gitBranch;
    private final String gitCommit;
    private final String dateTime;
    private final String displayVersion;
    private final String engineVersion;
    private final String stringRepresentation;

    private TerasologyGameVersionInfo(Properties properties) {
        buildNumber = properties.getProperty(BUILD_NUMBER, DEFAULT_VALUE);
        buildId = properties.getProperty(BUILD_ID, DEFAULT_VALUE);
        buildTag = properties.getProperty(BUILD_TAG, DEFAULT_VALUE);
        buildUrl = properties.getProperty(BUILD_URL, DEFAULT_VALUE);
        jobName = properties.getProperty(JOB_NAME, DEFAULT_VALUE);
        gitBranch = properties.getProperty(GIT_BRANCH, DEFAULT_VALUE);
        gitCommit = properties.getProperty(GIT_COMMIT, DEFAULT_VALUE);
        dateTime = properties.getProperty(DATE_TIME, DEFAULT_VALUE);
        displayVersion = properties.getProperty(DISPLAY_VERSION, DEFAULT_VALUE);
        engineVersion = properties.getProperty(ENGINE_VERSION, DEFAULT_VALUE);

        final StringBuilder stringRepresentationBuilder = new StringBuilder();
        stringRepresentationBuilder
                .append("[")
                .append(BUILD_NUMBER)
                .append("=")
                .append(buildNumber)
                .append(", ")
                .append(BUILD_ID)
                .append("=")
                .append(buildId)
                .append(", ")
                .append(BUILD_TAG)
                .append("=")
                .append(buildTag)
                .append(", ")
                .append(BUILD_URL)
                .append("=")
                .append(buildUrl)
                .append(", ")
                .append(JOB_NAME)
                .append("=")
                .append(jobName)
                .append(", ")
                .append(GIT_BRANCH)
                .append("=")
                .append(gitBranch)
                .append(", ")
                .append(GIT_COMMIT)
                .append("=")
                .append(gitCommit)
                .append(", ")
                .append(DATE_TIME)
                .append("=")
                .append(dateTime)
                .append(", ")
                .append(DISPLAY_VERSION)
                .append("=")
                .append(displayVersion)
                .append(", ")
                .append(ENGINE_VERSION)
                .append("=")
                .append(engineVersion)
                .append("]");
        stringRepresentation = stringRepresentationBuilder.toString();
    }

    public static TerasologyGameVersionInfo getEmptyGameVersionInfo() {
        return new TerasologyGameVersionInfo(new Properties());
    }

    public static TerasologyGameVersionInfo loadFromInputStream(InputStream inStream) {
        return new TerasologyGameVersionInfo(loadPropertiesFromInputStream(inStream));
    }

    private static Properties loadPropertiesFromInputStream(InputStream inStream) {
        final Properties properties = new Properties();
        if (inStream != null) {
            try {
                properties.load(inStream);
            } catch (IOException e) {
                logger.error("Loading game version info failed!", e);
            } finally {
                try {
                    inStream.close();
                } catch (IOException e) {
                    logger.warn("Closing InputStream failed!", e);
                }
            }
        }

        return properties;
    }

    public static TerasologyGameVersionInfo loadFromJar(Path terasologyGameJar) {
        final Properties properties = new Properties();
        try {
            if (Files.exists(terasologyGameJar) && Files.isRegularFile(terasologyGameJar) && Files.isReadable(terasologyGameJar)) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(terasologyGameJar, null)) {
                    Path versionInfo = fileSystem.getPath(VERSION_INFO_FILE);
                    try (InputStream versionInput = Files.newInputStream(versionInfo)) {
                        properties.load(versionInput);
                    }
                }
            }

            if (properties.isEmpty()) {
                logger.warn("Could not load TerasologyGameVersionInfo from file '{}'!", terasologyGameJar);
            }
        } catch (IOException e) {
            logger.error("Could not load TerasologyGameVersionInfo from file '{}'!", terasologyGameJar, e);
        }

        return new TerasologyGameVersionInfo(properties);
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getBuildId() {
        return buildId;
    }

    public String getBuildTag() {
        return buildTag;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public String getJobName() {
        return jobName;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public String getGitCommit() {
        return gitCommit;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getDisplayVersion() {
        return displayVersion;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }
}
