// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;
import org.terasology.launcher.model.ReleaseMetadata;

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

    /**
     * The PR upgrading the engine to LWJGL v3 (https://github.com/MovingBlocks/Terasology/pull/3969) was merged on
     * Oct 24, 2020, 20:12 UTC as commit 'c83655fb94c02d68fb8ba31fdb1954e81dde12d6'.
     * <p>
     * This method does some reverse engineering of which build on which Jenkins job contains this change. This should
     * probably be baked into the {@link GameIdentifier} while fetching the info from the remote source. With upcoming
     * refactoring I'd like to keep this separate here for now...
     *
     * @param buildNumber the Jenkins build numbers (from jenkins.terasology.org)
     * @return
     */
    private boolean isLwjgl3(int buildNumber) {
        if (profile.equals(Profile.OMEGA) && buildProfile.equals(Build.STABLE)) {
            return buildNumber > 37;
        }
        if (profile.equals(Profile.OMEGA) && buildProfile.equals(Build.NIGHTLY)) {
            return buildNumber > 1103;
        }
        if (profile.equals(Profile.ENGINE) && buildProfile.equals(Build.STABLE)) {
            return buildNumber > 82;
        }
        if (profile.equals(Profile.ENGINE) && buildProfile.equals(Build.NIGHTLY)) {
            return buildNumber > 2317;
        }
        return false;
    }

    private ReleaseMetadata computeReleaseMetadataFrom(Jenkins.Build jenkinsBuildInfo) {
        final List<String> changelog = computeChangelogFrom(jenkinsBuildInfo.changeSet);
        final Date timestamp = new Date(jenkinsBuildInfo.timestamp);
        final boolean isLwjgl3 = isLwjgl3(Integer.parseInt(jenkinsBuildInfo.number));
        // all builds from this Jenkins are using LWJGL v3
        return new ReleaseMetadata(changelog, timestamp, isLwjgl3);
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
                        final URL url = client.getArtifactUrl(build, TERASOLOGY_ZIP_PATTERN);
                        if (url != null) {
                            final GameIdentifier id = new GameIdentifier(build.number, buildProfile, profile);
                            final ReleaseMetadata releaseMetadata = computeReleaseMetadataFrom(build);
                            final GameRelease release = new GameRelease(id, url, releaseMetadata);
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
