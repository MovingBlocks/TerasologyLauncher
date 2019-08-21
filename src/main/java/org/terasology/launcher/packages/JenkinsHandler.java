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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles fetching of packages from a Jenkins-based repository.
 */
class JenkinsHandler implements RepositoryHandler {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsHandler.class);

    private static final String JOB = "/job/";
    private static final String API_FILTER = "/api/json?tree=builds[number,artifacts[fileName,relativePath],url]";
    private static final String TERASOLOGY_ZIP_PATTERN = "Terasology.*zip";
    private static final String ARTIFACT = "artifact/";

    private final Gson gson = new Gson();

    @Override
    public List<Package> getPackages(PackageDatabase.Repository source) {
        final List<Package> packages = new ArrayList<>();
        for (String packageName : source.getTrackedPackages()) {
            final String apiUrl = source.getUrl() + JOB + packageName + API_FILTER;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL(apiUrl).openStream())
            )) {
                final ApiResult result = gson.fromJson(reader, ApiResult.class);

                packages.addAll(Arrays.stream(result.builds)
                                      .map(build -> packageOf(packageName, build))
                                      .collect(Collectors.toList()));
            } catch (IOException e) {
                logger.warn("Failed to fetch packages from: {}", apiUrl);
            }
        }

        return packages;
    }

    private Package packageOf(String packageName, Build build) {
        return Arrays.stream(build.artifacts)
                .filter(artifact -> artifact.fileName.matches(TERASOLOGY_ZIP_PATTERN))
                .findFirst()
                .map(artifact -> new Package(
                        packageName,
                        build.number,
                        build.url + ARTIFACT + artifact.relativePath
                )).orElse(null);
    }

    private static class ApiResult {
        private Build[] builds;
    }

    private static class Build {
        private String number;
        private Artifact[] artifacts;
        private String url;
    }

    private static class Artifact {
        private String fileName;
        private String relativePath;
    }
}
