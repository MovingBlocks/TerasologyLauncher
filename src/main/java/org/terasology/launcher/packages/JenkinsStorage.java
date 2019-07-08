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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.JobResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides game packages from our Jenkins server.
 */
public class JenkinsStorage implements Storage {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsStorage.class);

    private static final String JENKINS_JOB_URL = "http://jenkins.terasology.org/";
    private static final String API_PATH = "/api/json?tree=builds[number,result]";
    private static final int LIMIT_VERSIONS = 20;

    private Gson gson = new Gson();

    @Override
    public List<Integer> getPackageVersions(GamePackageType pkgType) {
        final String jobApiPath = JENKINS_JOB_URL + pkgType.getJobName() + API_PATH;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new URL(jobApiPath).openStream()))) {

            final Job result = gson.fromJson(reader, Job.class);
            return Arrays.stream(result.builds)
                    .filter(build -> build.result.equals(JobResult.SUCCESS.name()))
                    .map(build -> build.number)
                    .limit(LIMIT_VERSIONS)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.warn("Failed to access URL: {}", jobApiPath);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<GamePackage> getPackage(GamePackageType pkgType, int version) {
        // TODO: Implement this
        return Optional.empty();
    }

    private static class Job {
        private Build[] builds;
    }

    private static class Build {
        private int number;
        private String result;
    }
}
