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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Repository adapter for the "new" Jenkins on <a href="http://jenkins.terasology.io">jenkins.terasology.io</a>.
 */
class NanowareJenkinsRepositoryAdapter implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(NanowareJenkinsRepositoryAdapter.class);

    private static final String BASE_URL = "http://jenkins.terasology.io/teraorg/job/Nanoware/";

    private static final String API_FILTER = "api/json?tree="
            + "builds["
            + "number,"
            + "result,"
            + "artifacts[fileName,relativePath],"
            + "url,"
            + "changeSet[items[msg]]]";
    private static final String TERASOLOGY_ZIP_PATTERN = "Terasology.*zip";
    private static final String ARTIFACT = "artifact/";

    private final Gson gson = new Gson();

    private final Build buildProfile;
    private final Profile profile;

    NanowareJenkinsRepositoryAdapter(Build buildProfile, Profile profile) {
        this.buildProfile = buildProfile;
        this.profile = profile;
    }

    private boolean isSuccess(Jenkins.Build build) {
        return build.result == Jenkins.Build.Result.SUCCESS || build.result == Jenkins.Build.Result.UNSTABLE;
    }

    private String job(String job) {
        return "job/" + job;
    }

    private String profileToJobName() {
        switch (profile) {
            case OMEGA:
                return "Omega/";
            case ENGINE:
                return "Terasology/";
            default:
                throw new IllegalStateException("Unexpected value: " + profile);
        }
    }

    private String buildProfileToJobName() {
        switch (buildProfile) {
            case STABLE:
                return "master/";
            case NIGHTLY:
                return "develop/";
            default:
                throw new IllegalStateException("Unexpected value: " + buildProfile);
        }
    }


    public List<GameRelease> fetchReleases() {
        final List<GameRelease> pkgList = new LinkedList<>();

        final String apiUrl = BASE_URL + job(profileToJobName()) + job(buildProfileToJobName()) + API_FILTER;

        logger.debug("fetching releases from '{}'", apiUrl);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new URL(apiUrl).openStream())
        )) {
            final Jenkins.ApiResult result = gson.fromJson(reader, Jenkins.ApiResult.class);
            for (Jenkins.Build build : result.builds) {
                if (isSuccess(build)) {
                    final String url = getArtefactUrl(build, TERASOLOGY_ZIP_PATTERN);
                    if (url != null) {

                        Semver semver = deriveSemver(result, build);
                        final GameIdentifier id = new GameIdentifier(build.number, buildProfile, profile, semver);

                        final GameRelease release = new GameRelease(id, new URL(url), Collections.emptyList(), null);
                        pkgList.add(release);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to fetch packages from: {}", apiUrl, e);
        }
        return pkgList;
    }

    private Semver deriveSemver(Jenkins.ApiResult result, Jenkins.Build build) {
        final String versionPropertiesUrl = getArtefactUrl(build, "versionInfo.properties");
        if (versionPropertiesUrl != null) {
            try (InputStream inputStream = new URL(versionPropertiesUrl).openStream()) {
                final Properties versionProperties = new Properties();
                versionProperties.load(inputStream);
                final Semver engineVersion = new Semver(versionProperties.getProperty("engineVersion"));

                Semver version = engineVersion.withClearedSuffixAndBuild().withBuild(build.number);
                if (buildProfile == Build.NIGHTLY) {
                    version = version.withSuffix("SNAPSHOT");
                }
                return version;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    private String getArtefactUrl(Jenkins.Build build, String regex) {
        return Arrays.stream(build.artifacts)
                .filter(artifact -> artifact.fileName.matches(regex))
                .findFirst()
                .map(artifact -> build.url + ARTIFACT + artifact.relativePath)
                .orElse(null);
    }
}
