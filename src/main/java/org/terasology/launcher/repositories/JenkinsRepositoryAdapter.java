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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class JenkinsRepositoryAdapter implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsRepositoryAdapter.class);

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

    private final String baseUrl;
    private final String jobName;
    private final Build buildProfile;
    private final Profile profile;

    public JenkinsRepositoryAdapter(String baseUrl, String jobName, Build buildProfile, Profile profile) {
        this.baseUrl = baseUrl;
        this.jobName = jobName;
        this.buildProfile = buildProfile;
        this.profile = profile;
    }

    private boolean isSuccess(Jenkins.Build build) {
        return build.result == Jenkins.Build.Result.SUCCESS || build.result == Jenkins.Build.Result.UNSTABLE;
    }


    public List<GameRelease> fetchReleases() {
        final List<GameRelease> pkgList = new LinkedList<>();

        final String apiUrl = baseUrl + JOB + jobName + API_FILTER;

        logger.debug("fetching releases from '{}'", apiUrl);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new URL(apiUrl).openStream())
        )) {
            final Jenkins.ApiResult result = gson.fromJson(reader, Jenkins.ApiResult.class);
            for (Jenkins.Build build : result.builds) {
                if (isSuccess(build)) {
                    final String changelog = Arrays.stream(build.changeSet.items)
                            .map(change -> change.msg)
                            .collect(Collectors.joining("\n"));
                    final String url = getArtefactUrl(build, TERASOLOGY_ZIP_PATTERN);
                    if (url != null) {
                        final GameIdentifier id = new GameIdentifier(build.number, buildProfile, profile);
                        final GameRelease release = new GameRelease(id, new URL(url), changelog, null);
                        pkgList.add(release);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to fetch packages from: {}", apiUrl, e);
        }
        return pkgList;
    }

    @Nullable
    private String getArtefactUrl(Jenkins.Build build, String regex) {
        return Arrays.stream(build.artifacts)
                .filter(artifact -> artifact.fileName.matches(regex))
                .findFirst()
                .map(artifact -> build.url + ARTIFACT + artifact.relativePath)
                .orElse(null);
    }

    @Nullable
    private String getUpstreamUrl(Jenkins.Project[] projects, Jenkins.Build build) {
        return Arrays.stream(projects).findFirst().map(project -> {
            final String version = build.actions[0].causes[0].upstreamBuild;
            if (version != null) {
                return baseUrl
                        + JOB
                        + projects[0].name
                        + "/" + version + "/";                 // Automated
            }
            return null;
        }).orElse(null);
    }

}
