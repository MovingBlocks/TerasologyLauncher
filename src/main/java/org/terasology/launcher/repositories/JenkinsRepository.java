// Copyright 2021 The Terasology Foundation
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository adapter for http://jenkins.terasology.io.
 * <p>
 * On the new Jenkins we can make use of the {@code versionInfo.properties} file to get the display name for the release
 * along other metadata (for instance, the corresponding engine version).
 * <p>
 * However, this means that we are doing {@code n + 1} API calls for fetching {@code n} release packages on each
 * launcher start.
 */
class JenkinsRepository implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsRepository.class);

    private static final String BASE_URL = "http://jenkins.terasology.io/teraorg/job/Terasology/";

    private static final String API_FILTER = "api/json?tree="
            + "builds["
            + "number,"
            + "timestamp,"
            + "result,"
            + "artifacts[fileName,relativePath],"
            + "url]";

    private static final String TERASOLOGY_ZIP_PATTERN = "Terasology.*zip";

    private final JenkinsClient client;

    private final Build buildProfile;
    private final Profile profile;

    private final URL apiUrl;

    JenkinsRepository(Profile profile, Build buildProfile, JenkinsClient client) {
        this.client = client;
        this.buildProfile = buildProfile;
        this.profile = profile;
        this.apiUrl = unsafeToUrl(BASE_URL + job(profileToJobName(profile)) + job(buildProfileToJobName(buildProfile)) + API_FILTER);
    }

    public List<GameRelease> fetchReleases() {
        final List<GameRelease> pkgList = new LinkedList<>();

        logger.debug("fetching releases from '{}'", apiUrl);

        final Jenkins.ApiResult result;
        try {
            result = client.request(apiUrl);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while fetching packages from: {}", apiUrl, e);
            return Collections.emptyList();
        }
        if (result != null && result.builds != null) {
            for (Jenkins.Build build : result.builds) {
                computeReleaseFrom(build).ifPresent(pkgList::add);
            }
        } else {
            logger.warn("Failed to fetch packages from: {}", apiUrl);
        }
        return pkgList;
    }

    private Optional<GameRelease> computeReleaseFrom(Jenkins.Build jenkinsBuildInfo) {
        if (hasAcceptableResult(jenkinsBuildInfo)) {
            final URL url = client.getArtifactUrl(jenkinsBuildInfo, TERASOLOGY_ZIP_PATTERN);

            final ReleaseMetadata metadata = computeReleaseMetadataFrom(jenkinsBuildInfo);
            final Optional<GameIdentifier> id = computeIdentifierFrom(jenkinsBuildInfo);

            //TODO: check whether the game release is supported (minimal Java version)
            //      we probably need to encode the engine version explicitly in the GameIdentifier (instead of just the display version)

            if (url != null && id.isPresent()) {
                return Optional.of(new GameRelease(id.get(), url, metadata));
            } else {
                logger.debug("Skipping build without game artifact or version identifier: '{}'", jenkinsBuildInfo.url);
            }
        } else {
            logger.debug("Skipping unsuccessful build '{}'", jenkinsBuildInfo.url);
        }
        return Optional.empty();
    }

    private Optional<GameIdentifier> computeIdentifierFrom(Jenkins.Build jenkinsBuildInfo) {
        return Optional.ofNullable(client.getArtifactUrl(jenkinsBuildInfo, "versionInfo.properties"))
                .map(client::requestProperties)
                .map(versionInfo -> versionInfo.getProperty("displayVersion"))
                .map(displayVersion -> {
                    // versionInfo.properties is created during the Engine build.
                    // jenkinsBuildInfo is the build of a Distribution.
                    //
                    // There may be multiple Distribution builds that come from the same Engine build.
                    //
                    // We can use the Engine's displayVersion, but we use the Distribution build number
                    // to ensure uniqueness.
                    String versionString = displayVersion + "+" + jenkinsBuildInfo.number;
                    return new GameIdentifier(versionString, buildProfile, profile);
                });
    }

    private ReleaseMetadata computeReleaseMetadataFrom(Jenkins.Build jenkinsBuildInfo) {
        String changelog = computeChangelogFrom(jenkinsBuildInfo.changeSet);
        final Date timestamp = new Date(jenkinsBuildInfo.timestamp);
        // all builds from this Jenkins are using LWJGL v3
        return new ReleaseMetadata(changelog, timestamp);
    }

    private String computeChangelogFrom(Jenkins.ChangeSet changeSet) {
        return Optional.ofNullable(changeSet)
                .map(changes ->
                        Arrays.stream(changes.items)
                                .map(change -> "- " + change.msg)
                                .collect(Collectors.joining("\n"))
                ).orElse("");
    }

    // utility IO

    private static URL unsafeToUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) { //NOPMD
            //TODO: at least log something here?
        }
        return null;
    }

    // utility specific to this Jenkins adapter

    private static String profileToJobName(Profile profile) {
        switch (profile) {
            case OMEGA:
                return "Omega/";
            case ENGINE:
                return "Terasology/";
            default:
                throw new IllegalStateException("Unexpected value: " + profile);
        }
    }

    private static String buildProfileToJobName(Build buildProfile) {
        switch (buildProfile) {
            case STABLE:
                return "master/";
            case NIGHTLY:
                return "develop/";
            default:
                throw new IllegalStateException("Unexpected value: " + buildProfile);
        }
    }

    private static String job(String job) {
        return "job/" + job;
    }

    // generic Jenkins.Build utility

    private static boolean hasAcceptableResult(Jenkins.Build build) {
        return build.result == Jenkins.Build.Result.SUCCESS || build.result == Jenkins.Build.Result.UNSTABLE;
    }
}
