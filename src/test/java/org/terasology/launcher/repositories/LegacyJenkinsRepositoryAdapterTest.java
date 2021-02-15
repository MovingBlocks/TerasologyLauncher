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
        fail("not implemented");
    }

    @Test
    @DisplayName("skip incomplete API results")
    void skipIncompatibleApiResults() {
        fail("not implemented");
    }

}
