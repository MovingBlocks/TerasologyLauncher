// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("LegacyJenkinsRepositoryAdapter#fetchReleases() should")
class LegacyJenkinsRepositoryAdapterTest {

    static final String BASE_URL = "http://jenkins.terasology.org";
    static final String JOB = "DistroOmega";

    static Gson gson;

    @BeforeAll
    static void setup() {
        gson = new Gson();
    }

    @Test
    @DisplayName("handle null Jenkins response gracefully")
    void handleNullJenkinsResponseGracefully() {
        final JenkinsClient nullClient = new StubJenkinsClient(url -> null, url -> null);
        final LegacyJenkinsRepositoryAdapter adapter =
                new LegacyJenkinsRepositoryAdapter(BASE_URL, JOB, Build.STABLE, Profile.OMEGA, nullClient);
        assertTrue(adapter.fetchReleases().isEmpty());
    }

    @Test
    @DisplayName("process valid response correctly")
    void processValidResponseCorrectly() throws MalformedURLException {
        final Jenkins.ApiResult validResult = gson.fromJson(JenkinsPayload.V1.validPayload(), Jenkins.ApiResult.class);
        final JenkinsClient stubClient = new StubJenkinsClient(url -> validResult, url -> {
            throw new RuntimeException();
        });
        final LegacyJenkinsRepositoryAdapter adapter =
                new LegacyJenkinsRepositoryAdapter(BASE_URL, JOB, Build.STABLE, Profile.OMEGA, stubClient);

        final URL expectedArtifactUrl = new URL("http://jenkins.terasology.org/job/DistroOmega/1123/"
                + "artifact/" + "distros/omega/build/distributions/TerasologyOmega.zip");
        final GameIdentifier id = new GameIdentifier("1123", Build.STABLE, Profile.OMEGA);
        final GameRelease expected = new GameRelease(id, expectedArtifactUrl, new ArrayList<>(), new Date(1609713454443L));

        assertEquals(1, adapter.fetchReleases().size());
        assertAll(
                () -> assertEquals(expected.getId(), adapter.fetchReleases().get(0).getId()),
                () -> assertEquals(expected.getUrl(), adapter.fetchReleases().get(0).getUrl()),
                () -> assertEquals(expected.getTimestamp(), adapter.fetchReleases().get(0).getTimestamp())
        );
    }

    @Test
    @DisplayName("skip incomplete API results")
    void skipIncompatibleApiResults() {
        fail("not implemented");
    }

}
