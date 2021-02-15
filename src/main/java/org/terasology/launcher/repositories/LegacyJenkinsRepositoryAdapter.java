// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class LegacyJenkinsRepositoryAdapter implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(LegacyJenkinsRepositoryAdapter.class);

    private static final String API_FILTER = "api/json?tree="
            + "builds["
            + "actions[causes[upstreamBuild]]{0},"
            + "number,"
            + "timestamp,"
            + "result,"
            + "artifacts[fileName,relativePath],"
            + "url,"
            + "changeSet[items[msg]]],"
            + "upstreamProjects[name]";

    private static final String TERASOLOGY_ZIP_PATTERN = "Terasology.*zip";

    private final String baseUrl;
    private final String jobName;
    private final Build buildProfile;
    private final Profile profile;

    private final JenkinsClient client;

    LegacyJenkinsRepositoryAdapter(String baseUrl, String jobName, Build buildProfile, Profile profile, JenkinsClient client) {
        this.baseUrl = baseUrl;
        this.jobName = jobName;
        this.buildProfile = buildProfile;
        this.profile = profile;
        this.client = client;
    }

    private boolean hasAcceptableResult(Jenkins.Build build) {
        return build.result == Jenkins.Build.Result.SUCCESS || build.result == Jenkins.Build.Result.UNSTABLE;
    }

    private List<String> computeChangelogFrom(Jenkins.ChangeSet changeSet) {
        return Optional.ofNullable(changeSet)
                .map(changes ->
                        Arrays.stream(changes.items)
                                .map(change -> change.msg)
                                .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }

    public List<GameRelease> fetchReleases() {
        final List<GameRelease> pkgList = new LinkedList<>();
        final String apiUrl = baseUrl + "job/" + jobName + "/" + API_FILTER;

        logger.debug("fetching releases from '{}'", apiUrl);

        try {
            final Jenkins.ApiResult result = client.request(new URL(apiUrl));
            if (result != null && result.builds != null) {
                for (Jenkins.Build build : result.builds) {
                    if (hasAcceptableResult(build)) {
                        final List<String> changelog = computeChangelogFrom(build.changeSet);
                        final URL url = client.getArtifactUrl(build, TERASOLOGY_ZIP_PATTERN);
                        if (url != null) {
                            final GameIdentifier id = new GameIdentifier(build.number, buildProfile, profile);
                            final Date timestamp = new Date(build.timestamp);
                            final GameRelease release = new GameRelease(id, url, changelog, timestamp);
                            pkgList.add(release);
                        } else {
                            logger.debug("Skipping build without game artifact: '{}'", build.url);
                        }
                    }
                }
            } else {
                logger.warn("Failed to fetch packages from: {}", apiUrl);
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid URL: {}", apiUrl, e);
        }
        return pkgList;
    }
}
