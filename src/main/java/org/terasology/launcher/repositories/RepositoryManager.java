// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RepositoryManager {

    private final Set<GameRelease> releases;

    public RepositoryManager(OkHttpClient httpClient) {
        JenkinsClient client = new JenkinsClient(httpClient, new Gson());

        ReleaseRepository omegaNightly = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.NIGHTLY, client);
        ReleaseRepository github = new GithubRepositoryAdapter(httpClient);

        Set<ReleaseRepository> all = Sets.newHashSet(github, omegaNightly);

        releases = fetchReleases(all);
    }

    private Set<GameRelease> fetchReleases(final Set<ReleaseRepository> repositories) {
        return repositories.parallelStream()
                .map(ReleaseRepository::fetchReleases)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    public Set<GameRelease> getReleases() {
        return releases;
    }

}
