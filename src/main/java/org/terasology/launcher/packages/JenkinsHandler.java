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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles fetching of packages from a Jenkins-based repository.
 */
class JenkinsHandler implements RepositoryHandler {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsHandler.class);

    private static final String JOB = "/job/";
    private static final String API_FILTER = "/api/json?tree="
            + "builds["
            + "actions[causes[upstreamBuild]]{0},"
            + "number,"
            + "result,"
            + "artifacts[fileName,relativePath],"
            + "url,"
            + "changeSet[items[msg]]],"
            + "upstreamProjects[name]";
    private static final String TERASOLOGY_ZIP_PATTERN = "Terasology.*zip";
    private static final String ARTIFACT = "artifact/";

    private final Gson gson = new Gson();
    private final Map<String, Package> syncedPackages = new HashMap<>();
    private final Map<Package, String> upstreamUrls = new LinkedHashMap<>();

    @Override
    public List<Package> getPackageList(PackageDatabase.Repository source) {
        final List<Package> pkgList = new LinkedList<>();
        for (PackageDatabase.PackageMetadata pkg : source.getTrackedPackages()) {
            String pkgId = pkg.getId();
            final String apiUrl = source.getUrl() + JOB + pkgId + API_FILTER;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL(apiUrl).openStream())
            )) {
                final ApiResult result = gson.fromJson(reader, ApiResult.class);
                for (Build build : result.builds) {
                    if (build.result == Build.Result.ABORTED
                            || build.result == Build.Result.NOT_BUILT) {
                        continue;
                    }

                    // Get upstream URL
                    final boolean hasUpstream = (result.upstreamProjects.length != 0);
                    final String upstreamUrl;
                    if (hasUpstream
                            && build.actions.length != 0
                            && build.actions[0].causes != null
                    ) {
                        final String version = build.actions[0].causes[0].upstreamBuild;
                        upstreamUrl = (version != null)
                                ? source.getUrl()
                                    + JOB
                                    + result.upstreamProjects[0].name
                                    + "/" + version + "/"                 // Automated
                                : null;                                   // Manual
                    } else {
                        upstreamUrl = null;
                    }

                    // Get changelog
                    final List<String> changelog = Arrays.stream(build.changeSet.items)
                            .map(change -> change.msg)
                            .collect(Collectors.toCollection(LinkedList::new));

                    // Check package archive URL
                    final String zipUrl = Arrays.stream(build.artifacts)
                            .filter(art -> art.fileName.matches(TERASOLOGY_ZIP_PATTERN))
                            .findFirst()
                            .map(art -> build.url + ARTIFACT + art.relativePath)
                            .orElse(null);

                    // Create a Package
                    final Package currentPkg = new Package(
                            pkgId,
                            pkg.getName(),
                            build.number,
                            zipUrl,
                            changelog
                    );

                    // Update tracked collections
                    if (zipUrl != null) {
                        pkgList.add(currentPkg);
                    }
                    if (hasUpstream) {
                        upstreamUrls.put(currentPkg, upstreamUrl);
                    }
                    syncedPackages.put(build.url, currentPkg);
                }
            } catch (IOException e) {
                logger.warn("Failed to fetch packages from: {}", apiUrl);
            }
        }

        appendUpstreamChangelog();
        return pkgList;
    }

    private void appendUpstreamChangelog() {
        // Get downstream builds in reverse order
        final List<Package> downstreamPackages = new ArrayList<>(upstreamUrls.keySet());
        Collections.reverse(downstreamPackages);

        // Append changelog of upstream packages
        List<String> lastDownloadedChangelog = Collections.emptyList();
        for (Package pkg : downstreamPackages) {
            final String upstreamUrl = upstreamUrls.get(pkg);
            if (upstreamUrl == null) {
                // Manually started build: Use last downloaded changelog
                pkg.getChangelog().addAll(lastDownloadedChangelog);
            } else {
                // Automated build: Fetch changelog
                final Package upstreamPkg = syncedPackages.get(upstreamUrl);

                if (upstreamPkg != null) {
                    pkg.getChangelog().addAll(upstreamPkg.getChangelog());
                } else {
                    // Upstream package was't synced: Download changelog from Jenkins
                    try {
                        lastDownloadedChangelog = downloadChangelog(upstreamUrl);
                        pkg.getChangelog().addAll(lastDownloadedChangelog);
                    } catch (IOException e) {
                        logger.warn("Failed to fetch upstream changelog for package: {} {}",
                                pkg.getId(), pkg.getVersion());
                    }
                }
            }
        }
    }

    private List<String> downloadChangelog(final String upstreamUrl) throws IOException {
        final String changelogApiFilter = "api/json?tree=changeSet[items[msg]]";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new URL(upstreamUrl + changelogApiFilter).openStream())
        )) {
            final Build upstreamBuild = gson.fromJson(reader, Build.class);
            return Arrays.stream(upstreamBuild.changeSet.items)
                    .map(change -> change.msg)
                    .collect(Collectors.toList());
        }
    }

    private static class ApiResult {
        private Build[] builds;
        private Project[] upstreamProjects;
    }

    private static class Build {
        private Action[] actions;
        private String  number;
        private Result result;
        private Artifact[] artifacts;
        private String url;
        private ChangeSet changeSet;

        private enum Result {
            ABORTED, FAILURE, NOT_BUILT, SUCCESS, UNSTABLE
        }
    }

    private static class Artifact {
        private String fileName;
        private String relativePath;
    }

    private static class ChangeSet {
        private Change[] items;
    }

    private static class Change {
        private String msg;
    }

    private static class Action {
        private Cause[] causes;
    }

    private static class Cause {
        private String upstreamProject;
        private String upstreamBuild;
    }

    private static class Project {
        private String name;
    }
}
