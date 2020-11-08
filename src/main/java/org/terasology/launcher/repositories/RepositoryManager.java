// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepositoryManager {
    private static final String JENKINS_BASE_URL = "http://jenkins.terasology.org";

    private final ReleaseRepository terasologyNightly;
    private final ReleaseRepository terasologyStable;
    private final ReleaseRepository omegaNightly;
    private final ReleaseRepository omegaStable;

    private final Set<GameRelease> releases;

    public RepositoryManager() {
        terasologyNightly = new JenkinsRepositoryAdapter(JENKINS_BASE_URL, "Terasology", Build.NIGHTLY, Profile.MINIMAL);
        terasologyStable = new JenkinsRepositoryAdapter(JENKINS_BASE_URL, "TerasologyStable", Build.STABLE, Profile.MINIMAL);
        omegaNightly = new JenkinsRepositoryAdapter(JENKINS_BASE_URL, "DistroOmega", Build.NIGHTLY, Profile.OMEGA);
        omegaStable = new JenkinsRepositoryAdapter(JENKINS_BASE_URL, "DistroOmegaRelease", Build.STABLE, Profile.OMEGA);

        releases = fetchReleases();
    }

    private Stream<ReleaseRepository> allRepositories() {
        return Stream.of(terasologyNightly, terasologyStable, omegaNightly, omegaStable);
    }

    private Set<GameRelease> fetchReleases() {
        return allRepositories()
                .map(ReleaseRepository::fetchReleases)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    public Set<GameRelease> getReleases() {
        return releases;
    }

}
