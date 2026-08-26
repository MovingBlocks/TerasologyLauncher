// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;
import org.terasology.launcher.model.ReleaseMetadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final String BASE_URL = "https://jenkins.terasology.io/job/Terasology/";

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

    // @Nullable because unsafeToUrl() below can fail to parse it - see its TODO.
    private final @Nullable URL apiUrl;

    JenkinsRepository(Profile profile, Build buildProfile, JenkinsClient client) {
        this.client = client;
        this.buildProfile = buildProfile;
        this.profile = profile;
        this.apiUrl = unsafeToUrl(BASE_URL + job(profileToJobName(profile)) + job(buildProfileToJobName(buildProfile)) + API_FILTER);
    }

    @Override
    public List<GameRelease> fetchReleases() {
        final List<GameRelease> pkgList = new ArrayList<>();

        logger.debug("fetching releases from '{}'", apiUrl);

        if (apiUrl == null) {
            logger.warn("No valid Jenkins API URL for {}/{}, skipping.", profile, buildProfile);
            return pkgList;
        }

        final Jenkins.ApiResult result;
        try {
            result = client.request(apiUrl);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while fetching packages from: {}", apiUrl, e);
            return pkgList;
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

            //TODO: check whether the game release is supported (minimal Java version)
            //      we probably need to encode the engine version explicitly in the GameIdentifier (instead of just the display version)

            if (url != null) {
                final ReleaseMetadata metadata = computeReleaseMetadataFrom(jenkinsBuildInfo);
                final GameIdentifier id = computeIdentifierFrom(jenkinsBuildInfo);
                return Optional.of(new GameRelease(id, url, metadata));
            } else {
                logger.debug("Skipping build without game artifact: '{}'", jenkinsBuildInfo.url);
            }
        } else {
            logger.debug("Skipping unsuccessful build '{}'", jenkinsBuildInfo.url);
        }
        return Optional.empty();
    }

    private GameIdentifier computeIdentifierFrom(Jenkins.Build jenkinsBuildInfo) {
        // versionInfo.properties is created during the Engine build. jenkinsBuildInfo is the build
        // of a Distribution - there may be multiple Distribution builds from the same Engine build,
        // so when it's available we use the Engine's displayVersion plus the Distribution build
        // number to ensure uniqueness. It isn't always archived though (e.g. it's currently missing
        // from the upstream engine job's own artifacts) - fall back to the build number alone rather
        // than silently dropping the release from the list entirely.
        String displayVersion = Optional.ofNullable(client.getArtifactUrl(jenkinsBuildInfo, "versionInfo.properties"))
                .map(client::requestProperties)
                .map(versionInfo -> versionInfo.getProperty("displayVersion"))
                .orElse(null);

        String versionString = displayVersion != null
                ? displayVersion + "+" + jenkinsBuildInfo.number
                : "build-" + jenkinsBuildInfo.number;
        return new GameIdentifier(versionString, buildProfile, profile);
    }

    private ReleaseMetadata computeReleaseMetadataFrom(Jenkins.Build jenkinsBuildInfo) {
        String changelog = computeChangelogFrom(jenkinsBuildInfo.changeSet);
        final Instant timestamp = Instant.ofEpochMilli(jenkinsBuildInfo.timestamp);
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

    private static @Nullable URL unsafeToUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) { //NOPMD
            //TODO: at least log something here?
        }
        return null;
    }

    // utility specific to this Jenkins adapter

    private static String profileToJobName(Profile profile) {
        return switch (profile) {
            case OMEGA -> "Omega/";
            case ENGINE -> "Terasology/";
            default -> throw new IllegalStateException("Unexpected value: " + profile);
        };
    }

    private static String buildProfileToJobName(Build buildProfile) {
        return switch (buildProfile) {
            case STABLE -> "master/";
            case NIGHTLY -> "develop/";
            default -> throw new IllegalStateException("Unexpected value: " + buildProfile);
        };
    }

    private static String job(String job) {
        return "job/" + job;
    }

    // generic Jenkins.Build utility

    private static boolean hasAcceptableResult(Jenkins.Build build) {
        return build.result == Jenkins.Build.Result.SUCCESS || build.result == Jenkins.Build.Result.UNSTABLE;
    }
}
