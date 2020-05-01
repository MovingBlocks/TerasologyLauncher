/*
 * Copyright 2019 MovingBlocks
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

package org.terasology.launcher.packages;

import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides game packages from our Jenkins server.
 */
public class JenkinsRepository implements Repository {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsRepository.class);

    private static final String JENKINS_JOB_URL = "http://jenkins.terasology.org/job/";
    private static final String API_PATH = "/api/json?tree=builds[number,result]";
    private static final int LIMIT_VERSIONS = 5;
    private static final Duration JENKINS_TIMEOUT = Duration.ofSeconds(3);

    private Gson gson = new Gson();

    @Override
    public List<Integer> getPackageVersions(PackageBuild pkgType) {
        final String jobApiPath = JENKINS_JOB_URL + pkgType.getJobName() + API_PATH;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new URL(jobApiPath).openStream()))) {

            final Job result = gson.fromJson(reader, Job.class);
            return Arrays.stream(result.builds)
                    .map(build -> build.number)
                    .limit(LIMIT_VERSIONS)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.warn("Failed to access URL: {}", jobApiPath);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Package> getPackage(PackageBuild pkgBuild, int version) {
        // TODO: Implement this
        return Optional.empty();
    }

    @Override
    public boolean isAvailable() {
        logger.trace("Checking Jenkins availability...");
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(JENKINS_JOB_URL).openConnection();
            try (AutoCloseable ac = conn::disconnect) {
                conn.setConnectTimeout(Ints.checkedCast(JENKINS_TIMEOUT.toMillis()));
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    logger.trace("Jenkins is available at {}", JENKINS_JOB_URL);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not connect to Jenkins at {} - {}", JENKINS_JOB_URL, e.getMessage());
        }
        return false;
    }

    private static class Job {
        private Build[] builds;
    }

    private static class Build {
        private int number;
        private String result;
    }
}
