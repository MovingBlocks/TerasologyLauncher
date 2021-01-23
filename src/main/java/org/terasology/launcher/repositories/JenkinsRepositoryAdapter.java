// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.gson.Gson;
import com.vdurmont.semver4j.Semver;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Repository adapter for http://jenkins.terasology.io, replacing the {@link LegacyJenkinsRepositoryAdapter}.
 * <p>
 * On the new Jenkins we can make use of the {@code versionInfo.properties} file to get the display name for the release
 * along other metadata (for instance, the corresponding engine version).
 * <p>
 * However, this means that we are doing {@code n + 1} API calls for fetching {@code n} release packages on each
 * launcher start.
 */
class JenkinsRepositoryAdapter implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsRepositoryAdapter.class);

    private static final String BASE_URL = "http://jenkins.terasology.io/teraorg/job/Terasology/";

    private static final String API_FILTER = "api/json?tree="
            + "builds["
            + "number,"
            + "timestamp,"
            + "result,"
            + "artifacts[fileName,relativePath],"
            + "url]";

    private static final String TERASOLOGY_ZIP_PATTERN = "Terasology.*zip";
    private static final String ARTIFACT = "artifact/";

    private final Gson gson = new Gson();

    private final Build buildProfile;
    private final Profile profile;

    private final String jobSelector;

    JenkinsRepositoryAdapter(Profile profile, Build buildProfile) {
        this.buildProfile = buildProfile;
        this.profile = profile;
        this.jobSelector = job(profileToJobName(profile)) + job(buildProfileToJobName(buildProfile));
    }

    private boolean isSuccess(Jenkins.Build build) {
        return build.result == Jenkins.Build.Result.SUCCESS || build.result == Jenkins.Build.Result.UNSTABLE;
    }

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

    private String job(String job) {
        return "job/" + job;
    }

    public List<GameRelease> fetchReleases() {
        final List<GameRelease> pkgList = new LinkedList<>();

        final String apiUrl = BASE_URL + jobSelector + API_FILTER;

        logger.debug("fetching releases from '{}'", apiUrl);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new URL(apiUrl).openStream())
        )) {
            final Jenkins.ApiResult result = gson.fromJson(reader, Jenkins.ApiResult.class);
            for (Jenkins.Build build : result.builds) {
                if (isSuccess(build)) {
                    final String url = getArtifactUrl(build, TERASOLOGY_ZIP_PATTERN);
                    if (url != null) {

                        Properties versionInfo = fetchProperties(getArtifactUrl(build, "versionInfo.properties"));

                        final Date timestamp = new Date(build.timestamp);

                        String displayName = versionInfo.getProperty("displayVersion");

                        final GameIdentifier id = new GameIdentifier(displayName, buildProfile, profile);

                        Semver semver = deriveSemver(versionInfo);
                        logger.debug("Derived SemVer for {}: \t{}", id, semver);

                        List<String> changelog = Optional.ofNullable(build.changeSet)
                                .map(changeSet ->
                                        Arrays.stream(changeSet.items)
                                                .map(change -> change.msg)
                                                .collect(Collectors.toList())).orElse(new ArrayList<>());


                        final GameRelease release = new GameRelease(id, new URL(url), changelog, timestamp);
                        pkgList.add(release);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to fetch packages from: {}", apiUrl, e);
        }
        return pkgList;
    }

    private Properties fetchProperties(final String artifactUrl) {
        if (artifactUrl != null) {
            try (InputStream inputStream = new URL(artifactUrl).openStream()) {
                final Properties properties = new Properties();
                properties.load(inputStream);
                return properties;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Semver deriveSemver(final Properties versionInfo) {
        if (versionInfo != null) {
            final Semver engineVersion = new Semver(versionInfo.getProperty("engineVersion"));
            return engineVersion.withBuild(versionInfo.getProperty("buildId"));
        }
        return null;
    }

    @Nullable
    private String getArtifactUrl(Jenkins.Build build, String regex) {
        return Arrays.stream(build.artifacts)
                .filter(artifact -> artifact.fileName.matches(regex))
                .findFirst()
                .map(artifact -> build.url + ARTIFACT + artifact.relativePath)
                .orElse(null);
    }
}
