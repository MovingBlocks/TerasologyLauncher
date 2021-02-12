// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JenkinsRepositoryAdapter#fetchReleases() should")
class JenkinsRepositoryAdapterTest {

    static Gson gson;
    static Jenkins.ApiResult validResult;
    static URL expectedArtifactUrl;
    static List<Jenkins.ApiResult> incompleteResults;

    @BeforeAll
    static void setup() throws MalformedURLException {
        gson = new Gson();
        validResult = gson.fromJson(validPayload(), Jenkins.ApiResult.class);
        incompleteResults = incompletePayloads().stream()
                .map(json -> gson.fromJson(json, Jenkins.ApiResult.class))
                .collect(Collectors.toList());
        expectedArtifactUrl = new URL("http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/artifact/distros/omega/build/distributions/TerasologyOmega.zip");
    }

    static String validPayload() {
        return "{\n" +
                "  \"builds\": [\n" +
                "    {\n" +
                "      \"artifacts\": [\n" +
                "        {\n" +
                "          \"fileName\": \"TerasologyOmega.zip\",\n" +
                "          \"relativePath\": \"distros/omega/build/distributions/TerasologyOmega.zip\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"fileName\": \"versionInfo.properties\",\n" +
                "          \"relativePath\": \"distros/omega/versionInfo.properties\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"number\": 1,\n" +
                "      \"result\": \"SUCCESS\",\n" +
                "      \"timestamp\": 1604285977306,\n" +
                "      \"url\": \"http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    static String nullArtifactsPayload() {
        return "{ \n" +
                "  \"builds\": [\n" +
                "    {\n" +
                "      \"number\": 1, \"result\": \"SUCCESS\", \"timestamp\": 1604285977306, \n" +
                "      \"url\": \"http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    static String emptyArtifactsPayload() {
        return "{\n" +
                "  \"builds\": [\n" +
                "    {\n" +
                "      \"artifacts\": [],\n" +
                "      \"number\": 1,\n" +
                "      \"result\": \"SUCCESS\",\n" +
                "      \"timestamp\": 1604285977306,\n" +
                "      \"url\": \"http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    static String incompleteArtifactsPayload() {
        return "{\n" +
                "  \"builds\": [\n" +
                "    {\n" +
                "      \"artifacts\": [\n" +
                "        {\n" +
                "          \"fileName\": \"versionInfo.properties\",\n" +
                "          \"relativePath\": \"distros/omega/versionInfo.properties\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"number\": 1,\n" +
                "      \"result\": \"SUCCESS\",\n" +
                "      \"timestamp\": 1604285977306,\n" +
                "      \"url\": \"http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    static List<String> incompletePayloads() {
        return List.of(
                "{}",
                "{ \"builds\": [] }",
                nullArtifactsPayload(),
                emptyArtifactsPayload(),
                incompleteArtifactsPayload()
        );
    }

    static Stream<Arguments> incompleteResults() {
        return incompleteResults.stream().map(Arguments::of);
    }

    @Test
    @DisplayName("handle null Jenkins response gracefully")
    void shouldHandleNullJenkinsResponseGracefully() {
        final JenkinsClient nullClient = new StubJenkinsClient(url -> null, url -> null);
        final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, nullClient);
        assertTrue(adapter.fetchReleases().isEmpty());
    }

    @Test
    @DisplayName("skip builds without version info")
    void shouldSkipBuildsWithoutVersionInfo() {
        Properties emptyVersionInfo = new Properties();

        final JenkinsClient stubClient = new StubJenkinsClient(url -> validResult, url -> emptyVersionInfo);

        final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, stubClient);

        assertTrue(adapter.fetchReleases().isEmpty());
    }

    @Test
    @DisplayName("process valid response correctly")
    void shouldProcessValidResponseCorrectly() {
        String expectedVersion = "alpha 42 (preview) - 20210130";

        Properties versionInfo = new Properties();
        versionInfo.setProperty("displayVersion", expectedVersion);

        final JenkinsClient stubClient = new StubJenkinsClient(url -> validResult, url -> versionInfo);

        final GameIdentifier id = new GameIdentifier(expectedVersion, Build.STABLE, Profile.OMEGA);
        final GameRelease expected = new GameRelease(id, expectedArtifactUrl, new ArrayList<>(), new Date(1604285977306L));

        final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, stubClient);

        assertAll(
                () -> assertEquals(1, adapter.fetchReleases().size()),
                () -> assertEquals(expected.getId(), adapter.fetchReleases().get(0).getId()),
                () -> assertEquals(expected.getUrl(), adapter.fetchReleases().get(0).getUrl()),
                () -> assertEquals(expected.getTimestamp(), adapter.fetchReleases().get(0).getTimestamp())
        );
    }

    @ParameterizedTest(name = "{displayName} - [{index}] {arguments}")
    @DisplayName("skip incomplete API results")
    @MethodSource("incompleteResults")
    void shouldSkipIncompleteJsonPayloadData(Jenkins.ApiResult incompleteResult) {
        final JenkinsClient stubClient = new StubJenkinsClient(url -> incompleteResult, url -> null);
        final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, stubClient);
        assertTrue(adapter.fetchReleases().isEmpty());
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
            Preconditions.checkNotNull(artifactUrl);
            return requestProperties.apply(artifactUrl);
        }
    }
}
