// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

class JenkinsClient {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsClient.class);

    private static final String ARTIFACT = "artifact/";

    final OkHttpClient client;

    private final Gson gson;

    JenkinsClient(OkHttpClient httpClient, Gson gson) {
        this.gson = gson;

        // We have to work around some inconvenience with our own Jenkins by tweaking the cache control headers a bit.
        // The 'Expires' header would cause the 'versionInfo.properties' file to be fetched each and every time again.
        // To prevent this, we use an OkHttp Interceptor to remove the 'Expires' header and replace it with a custom
        // 'Cache-control' header.
        //
        // See https://square.github.io/okhttp/features/interceptors/
        var interceptor = new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                var request = chain.request();
                var originalResponse = chain.proceed(request);
                if (request.tag(PropertiesRequest.class) != null) {
                    // for requests for properties (e.g., versionInfo.properties) we use a long caching period
                    return originalResponse.newBuilder()
                            .removeHeader("Expires")
                            .header("Cache-control", "max-age=" + Duration.ofDays(30).toSeconds())
                            .build();
                }
                return originalResponse;
            }
        };

        client = httpClient.newBuilder()
                .addNetworkInterceptor(interceptor)
                .build();
    }

    Jenkins.ApiResult request(URL url) throws InterruptedException {
        Preconditions.checkNotNull(url);

        var request = new Request.Builder().url(url).build();
        try (var response = client.newCall(request).execute()) {
            logger.debug("{}{}", response, response.cacheResponse() != null ? " (cached)" : "");
            if (response.isSuccessful()) {
                return gson.fromJson(response.body().string(), Jenkins.ApiResult.class);
            } else {
                logger.warn("Failed to read from URL '{}' with status code {}.", url.toExternalForm(), response.code());
            }
        } catch (JsonSyntaxException | JsonIOException e) {
            logger.warn("Failed to read JSON from '{}'", url.toExternalForm(), e);
        } catch (IOException e) {
            logger.warn("Failed to read from URL '{}'\n\t{}", url.toExternalForm(), e.getMessage());
        }
        return null;
    }

    // a) request for 'versionInfo.properties' have the PropertiesRequest tag
    // b) request for other resources don't have it
    // c) the 'Expires' header is removed from response for requests with PropertiesRequest

    @Nullable
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    Properties requestProperties(final URL artifactUrl) {
        Preconditions.checkNotNull(artifactUrl);

        var request = new Request.Builder()
                .url(artifactUrl)
                .tag(PropertiesRequest.class, new PropertiesRequest())
                .build();

        try (var response = client.newCall(request).execute()) {
            logger.debug("{}{}", response.request().url(), response.cacheResponse() != null ? " (cached)" : "");
            final Properties properties = new Properties();
            properties.load(response.body().charStream());
            return properties;
        } catch (IOException e) {
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

    static final class PropertiesRequest { }
}
