// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

class JenkinsClient {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsClient.class);

    private static final String ARTIFACT = "artifact/";
    private final Gson gson;

    JenkinsClient(Gson gson) {
        this.gson = gson;
    }

    /**
     * Open an input stream from the given URL with a default timeout configured.
     *
     * The shorthand {@link URL#openStream()} does not allow for setting a connection timeout. Therefore, we interleave
     * the original implementation {@code openConnection().getInputStream()} with a step to configure a default timeout.
     *
     * The default timeout is 10 seconds.
     *
     * @param url the URL to open the stream for
     * @return an input stream similar to {@code url.openStream()} with a connection timeout of 10 seconds
     * @throws IOException if opening the URL connection or retrieving the input stream fails
     */
    static InputStream openStream(URL url) throws IOException, URISyntaxException, InterruptedException {
        // this is a static member to indicate that it is independent of the client itself, and to cleanly stub it for
        // testing purposes without the need to mock the class-under-test itself.
        var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        var request = HttpRequest.newBuilder(url.toURI()).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        logger.debug("{}", response);
        return response.body();
    }

    Jenkins.ApiResult request(URL url) throws InterruptedException {
        Preconditions.checkNotNull(url);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openStream(url)))) {
            return gson.fromJson(reader, Jenkins.ApiResult.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            logger.warn("Failed to read JSON from '{}'", url.toExternalForm(), e);
        } catch (URISyntaxException | IOException e) {
            logger.warn("Failed to read from URL '{}'\n\t{}", url.toExternalForm(), e.getMessage());
        }
        return null;
    }

    @Nullable
    Properties requestProperties(final URL artifactUrl) {
        Preconditions.checkNotNull(artifactUrl);
        try (InputStream inputStream = openStream(artifactUrl)) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.warn("Error while fetching {}", artifactUrl, e);
        }
        return null;
    }

    @Nullable
    URL getArtifactUrl(Jenkins.Build build, String regex) {
        if (build.artifacts == null || build.url == null) {
            return null;
        }
        Optional<String> url = Arrays.stream(build.artifacts)
                .filter(artifact -> artifact.fileName.matches(regex))
                .findFirst()
                .map(artifact -> build.url + ARTIFACT + artifact.relativePath);

        if (url.isPresent()) {
            try {
                return new URL(url.get());
            } catch (MalformedURLException e) {
                logger.debug("Invalid URL: '{}'", url, e);
            }
        }
        return null;
    }
}
