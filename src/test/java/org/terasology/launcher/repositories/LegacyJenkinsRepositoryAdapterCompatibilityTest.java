// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.Profile;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("LegacyJenkinsRepositoryAdapter should be backwards compatible.")
class LegacyJenkinsRepositoryAdapterCompatibilityTest {

    static final String BASE_URL = "http://jenkins.terasology.org";
    static final String JOB = "DistroOmega";

    static Gson gson = new Gson();

    /**
     * The PR upgrading the engine to LWJGL v3 (https://github.com/MovingBlocks/Terasology/pull/3969) was merged on
     * Oct 24, 2020, 20:12 UTC as commit 'c83655fb94c02d68fb8ba31fdb1954e81dde12d6'.
     * <p>
     * These are the corresponding build numbers on 'jenkins.terasology.org' for the first build with LWJGL v3.
     */
    static Stream<Arguments> firstBuildsForLwjgl3() {
        return Stream.of(
                Arguments.of(Profile.OMEGA, Build.STABLE, 38),
                Arguments.of(Profile.OMEGA, Build.NIGHTLY, 1104),
                Arguments.of(Profile.ENGINE, Build.STABLE, 83),
                Arguments.of(Profile.ENGINE, Build.NIGHTLY, 2318)
        );
    }

    @ParameterizedTest(name = "release before LWJGL v3 is correctly marked - {arguments}")
    @MethodSource("firstBuildsForLwjgl3")
    void releaseBeforeLwjgl3IsCorrectlyMarked(Profile profile, Build build, int buildNumber) {
        Jenkins.Build before = gson.fromJson(JenkinsPayload.V1.minimalValidBuildPayload(), Jenkins.Build.class);
        before.number = Integer.toString(buildNumber - 1);
        Jenkins.ApiResult result = gson.fromJson(JenkinsPayload.V1.minimalValidPayload(), Jenkins.ApiResult.class);
        result.builds = new Jenkins.Build[]{before};

        final JenkinsClient stubClient = new StubJenkinsClient(url -> result, url -> fail());
        final LegacyJenkinsRepositoryAdapter adapter = new LegacyJenkinsRepositoryAdapter(BASE_URL, JOB, build, profile, stubClient);

        assertFalse(adapter.fetchReleases().get(0).isLwjgl3());
    }

    @ParameterizedTest(name = "release after LWJGL v3 is correctly marked - {arguments}")
    @MethodSource("firstBuildsForLwjgl3")
    void releaseAfterLwjgl3IsCorrectlyMarked(Profile profile, Build build, int buildNumber) {
        Jenkins.Build after = gson.fromJson(JenkinsPayload.V1.minimalValidBuildPayload(), Jenkins.Build.class);
        after.number = Integer.toString(buildNumber + 1);

        Jenkins.ApiResult result = gson.fromJson(JenkinsPayload.V1.minimalValidPayload(), Jenkins.ApiResult.class);
        result.builds = new Jenkins.Build[]{after};

        final JenkinsClient stubClient = new StubJenkinsClient(url -> result, url -> fail());
        final LegacyJenkinsRepositoryAdapter adapter = new LegacyJenkinsRepositoryAdapter(BASE_URL, JOB, build, profile, stubClient);

        assertTrue(adapter.fetchReleases().get(0).isLwjgl3());
    }

    @ParameterizedTest(name = "first release with LWJGL v3 is correctly marked - {arguments}")
    @MethodSource("firstBuildsForLwjgl3")
    void firstReleaseWithLwjgl3IsCorrectlyMarked(Profile profile, Build build, int buildNumber) {
        Jenkins.Build first = gson.fromJson(JenkinsPayload.V1.minimalValidBuildPayload(), Jenkins.Build.class);
        first.number = Integer.toString(buildNumber);

        Jenkins.ApiResult result = gson.fromJson(JenkinsPayload.V1.minimalValidPayload(), Jenkins.ApiResult.class);
        result.builds = new Jenkins.Build[]{first};

        final JenkinsClient stubClient = new StubJenkinsClient(url -> result, url -> fail());
        final LegacyJenkinsRepositoryAdapter adapter = new LegacyJenkinsRepositoryAdapter(BASE_URL, JOB, build, profile, stubClient);

        assertTrue(adapter.fetchReleases().get(0).isLwjgl3());
    }
}
