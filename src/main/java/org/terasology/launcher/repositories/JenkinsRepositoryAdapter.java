// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.gson.Gson;

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
import java.net.MalformedURLException;
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

    private final Gson gson;

    private final Build buildProfile;
    private final Profile profile;

    private final URL apiUrl;

    JenkinsRepositoryAdapter(Profile profile, Build buildProfile, Gson gson) {
        this.gson = gson;
        this.buildProfile = buildProfile;
        this.profile = profile;
        this.apiUrl = toURL(BASE_URL + job(profileToJobName(profile)) + job(buildProfileToJobName(buildProfile)) + API_FILTER);
    }

    public List<GameRelease> fetchReleases() {
        final List<GameRelease> pkgList = new LinkedList<>();

        logger.debug("fetching releases from '{}'", apiUrl);

        try (BufferedReader reader = openConnection()) {
            final Jenkins.ApiResult result = gson.fromJson(reader, Jenkins.ApiResult.class);
            if (result != null && result.builds != null) {
                for (Jenkins.Build build : result.builds) {
                    computeReleaseFrom(build).ifPresent(pkgList::add);
                }
            } else {
                logger.debug("No build information.");
            }
            
        } catch (IOException e) {
            logger.warn("Failed to fetch packages from: {}", apiUrl, e);
        }
        return pkgList;
    }

    BufferedReader openConnection() throws IOException {
        return new BufferedReader(new InputStreamReader(apiUrl.openStream()));
    }

    Optional<GameRelease> computeReleaseFrom(Jenkins.Build jenkinsBuildInfo) {
        if (isSuccess(jenkinsBuildInfo)) {
            final URL url = getArtifactUrl(jenkinsBuildInfo, TERASOLOGY_ZIP_PATTERN);
            final Date timestamp = new Date(jenkinsBuildInfo.timestamp);     
            final List<String> changelog = computeChangelogFrom(jenkinsBuildInfo);
            final Optional<GameIdentifier> id = computeIdentifierFrom(jenkinsBuildInfo);

            if (url != null && id.isPresent()) {
                return Optional.of(new GameRelease(id.get(), url, changelog, timestamp));
            } else {
                logger.debug("Skipping build without game artifact or version identifier: '{}'", jenkinsBuildInfo.url);
            }
        } else {
            logger.debug("Skipping unsuccessful build '{}'", jenkinsBuildInfo.url);
        }
        return Optional.empty();
    }

    Optional<GameIdentifier> computeIdentifierFrom(Jenkins.Build jenkinsBuildInfo) {
        Properties versionInfo = fetchProperties(getArtifactUrl(jenkinsBuildInfo, "versionInfo.properties"));
        if (versionInfo != null) {
            String displayName = versionInfo.getProperty("displayVersion");
            if (displayName != null)  {
                return Optional.of(new GameIdentifier(displayName, buildProfile, profile));
            }
        }        
        return Optional.empty();
    }

    List<String> computeChangelogFrom(Jenkins.Build jenkinsBuildInfo) {
        return Optional.ofNullable(jenkinsBuildInfo.changeSet)
                    .map(changeSet ->
                        Arrays.stream(changeSet.items)
                            .map(change -> change.msg)
                            .collect(Collectors.toList())
                        ).orElse(new ArrayList<>());
    }

    // utility IO

    private static URL toURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
        }
        return null;
    }

    @Nullable
    private Properties fetchProperties(final URL artifactUrl) {
        if (artifactUrl != null) {
            try (InputStream inputStream = artifactUrl.openStream()) {
                final Properties properties = new Properties();
                properties.load(inputStream);
                return properties;
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private static boolean isSuccess(Jenkins.Build build) {
        return build.result == Jenkins.Build.Result.SUCCESS || build.result == Jenkins.Build.Result.UNSTABLE;
    }

    @Nullable
    private URL getArtifactUrl(Jenkins.Build build, String regex) {
        Optional<String> url = Arrays.stream(build.artifacts)
                .filter(artifact -> artifact.fileName.matches(regex))
                .findFirst()
                .map(artifact -> build.url + ARTIFACT + artifact.relativePath);

        if (url.isPresent()) {
            try {
                return new URL(url.get());
            } catch (MalformedURLException e) {
                logger.debug("Invalid URL: '{}'", url, e);
            }
        } else {
            logger.debug("Cannot find artifact matching '{}' for build '{}'", regex, build.url);
        }
        return null;
    }
}
