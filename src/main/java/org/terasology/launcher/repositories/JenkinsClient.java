// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

public class JenkinsClient {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsClient.class);

    private static final String ARTIFACT = "artifact/";
    private final Gson gson;

    public JenkinsClient(Gson gson) {
        this.gson = gson;
    }

    public Jenkins.ApiResult request(URL url) {
        Preconditions.checkNotNull(url);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return gson.fromJson(reader, Jenkins.ApiResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    Properties requestProperties(final URL artifactUrl) {
        Preconditions.checkNotNull(artifactUrl);
        try (InputStream inputStream = artifactUrl.openStream()) {
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
