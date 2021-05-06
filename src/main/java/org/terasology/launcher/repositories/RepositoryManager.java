// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RepositoryManager {
    private static final String JENKINS_BASE_URL = "http://jenkins.terasology.org/";

    private final Set<GameRelease> releases;

    public RepositoryManager() {
        JenkinsClient client = new JenkinsClient(new Gson());

        ReleaseRepository legacyEngineNightly = new LegacyJenkinsRepositoryAdapter(JENKINS_BASE_URL, "Terasology", Build.NIGHTLY, Profile.ENGINE, client);
        ReleaseRepository legacyEngineStable = new LegacyJenkinsRepositoryAdapter(JENKINS_BASE_URL, "TerasologyStable", Build.STABLE, Profile.ENGINE, client);
        ReleaseRepository legacyOmegaNightly = new LegacyJenkinsRepositoryAdapter(JENKINS_BASE_URL, "DistroOmega", Build.NIGHTLY, Profile.OMEGA, client);
        ReleaseRepository legacyOmegaStable = new LegacyJenkinsRepositoryAdapter(JENKINS_BASE_URL, "DistroOmegaRelease", Build.STABLE, Profile.OMEGA, client);

        ReleaseRepository omegaNightly = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.NIGHTLY, client);
        ReleaseRepository omegaStable = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, client);

        Set<ReleaseRepository> all = Sets.newHashSet(
                legacyEngineNightly, legacyEngineStable,
                legacyOmegaNightly, legacyOmegaStable,
                omegaNightly, omegaStable);

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
