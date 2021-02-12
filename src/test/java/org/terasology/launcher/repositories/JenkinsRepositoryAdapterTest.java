// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.function.Function;

public class JenkinsRepositoryAdapterTest {

    @Test
    void fetchReleases_emptyOnNullResult() {
        final JenkinsClient nullClient = new StubJenkinsClient(url -> null, url -> null);
        final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, nullClient);
        Assertions.assertTrue(adapter.fetchReleases().isEmpty());
    }

    @Test
    void fetchReleases_responseWithoutBuilds() {
        final JenkinsClient stubClient = new StubJenkinsClient(url -> new Jenkins.ApiResult(), url -> null);

        final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, stubClient);

        Assertions.assertTrue(adapter.fetchReleases().isEmpty());
    }

    @Test
    void fetchReleases_assumeInvalidRelease() {
        Jenkins.Build buildStub = new Jenkins.Build();
        Jenkins.ApiResult resultStub = new Jenkins.ApiResult();
        resultStub.builds = new Jenkins.Build[]{buildStub};

        final JenkinsClient stubClient = new StubJenkinsClient(url -> resultStub, url -> null);

        final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, stubClient);

        Assertions.assertTrue(adapter.fetchReleases().isEmpty());
    }

    @Test
    void fetchReleases_assumeValidRelease() {
        String expectedVersion = "alpha 42 (preview) - 20210130";

        Properties versionInfo = new Properties();
        versionInfo.setProperty("displayVersion", expectedVersion);

        Jenkins.Artifact versionArtifact = new Jenkins.Artifact();
        versionArtifact.fileName = "versionInfo.properties";
        versionArtifact.relativePath = "path/to/";

        Jenkins.Artifact gameArtifact = new Jenkins.Artifact();
        gameArtifact.fileName = "Terasology.zip";
        gameArtifact.relativePath = "path/to/";

        Jenkins.Build buildStub = new Jenkins.Build();
        buildStub.artifacts = new Jenkins.Artifact[]{gameArtifact, versionArtifact};
        buildStub.url = "http://jenkins.terasology.org/";
        buildStub.result = Jenkins.Build.Result.SUCCESS;
        Jenkins.ApiResult resultStub = new Jenkins.ApiResult();
        resultStub.builds = new Jenkins.Build[]{buildStub};

        final JenkinsClient stubClient = new StubJenkinsClient(url -> resultStub, url -> versionInfo);

        final GameRelease expected = new GameRelease(null, null, null, null);

        final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, stubClient);

        Assertions.assertIterableEquals(Lists.newArrayList(expected), adapter.fetchReleases());
    }

    static class StubJenkinsClient extends JenkinsClient {
        final Function<URL, Jenkins.ApiResult> request;
        final Function<URL, Properties> requestProperties;

        StubJenkinsClient(Function<URL, Jenkins.ApiResult> request, Function<URL, Properties> requestProperties) {
            super(null);
            this.request = request;
            this.requestProperties = requestProperties;
        }

        @Override
        public Jenkins.ApiResult request(URL url) {
            return request.apply(url);
        }

        @Override
        Properties requestProperties(URL artifactUrl) {
            return requestProperties.apply(artifactUrl);
        }
    }
}
