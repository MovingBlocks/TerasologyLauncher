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
import java.net.URL;
import java.net.URLConnection;
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
    private InputStream openStream(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10 * 1000);
        return connection.getInputStream();
    }

    Jenkins.ApiResult request(URL url) {
        Preconditions.checkNotNull(url);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openStream(url)))) {
            return gson.fromJson(reader, Jenkins.ApiResult.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            logger.warn("Failed to read JSON from '{}'", url.toExternalForm(), e);
        } catch (IOException e) {
            logger.warn("Failed to read from URL '{}'\n\t{}", e.getMessage(), url.toExternalForm());
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
        } catch (IOException e) {
            e.printStackTrace();
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
