// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mock.MockInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.joda.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Note: We are testing with the default Gson parser here. This can go out of sync with the instantiation of
 * JenkinsClient in RepositoryManager. Using common dependency injection for productive code and tests would ensure we
 * are using the "real thing" under test here.
 */
@DisplayName("JenkinsClient")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.CloseResource", "PMD.AvoidDuplicateLiterals"})
class JenkinsClientTest {

    @Test
    @DisplayName("should handle IOException on request(url) gracefully")
    void nullOnIoException() throws IOException, InterruptedException {
        final Gson gson = new Gson();
        URL url = new URL("https://jenkins.example");

        final var mockHttpCall = mock(Call.class);
        final var mockHttpClient = mock(OkHttpClient.class);
        final var mockHttpClientBuilder = mock(OkHttpClient.Builder.class);

        when(mockHttpClient.newBuilder()).thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.addNetworkInterceptor(any())).thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);
        when(mockHttpClient.newCall(any())).thenReturn(mockHttpCall);
        when(mockHttpCall.execute()).thenThrow(IOException.class);

        final JenkinsClient client = new JenkinsClient(mockHttpClient, gson);
        assertNull(client.request(url));
    }

    @Test
    @DisplayName("can handle invalid JSON payload")
    void canHandleInvalidJsonPayload() throws InterruptedException, MalformedURLException {
        URL urlToInvalidPayload = new URL("https://jenkins.example");

        final var interceptor = new MockInterceptor();
        interceptor.addRule()
                .get("https://jenkins.example/")
                .respond("{ this is ] no json |[!");

        final var httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        final JenkinsClient client = new JenkinsClient(httpClient, new Gson());

        assertNull(client.request(urlToInvalidPayload));
    }

    @Test
    @DisplayName("should tweak requests with 'PropertiesRequest' tag to remove 'Expires' and set 'Cache-control' header")
    void httpClientInterceptsHeadersForPropertiesRequests() throws IOException {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().addHeader("Expires", new Instant(0L)));
        server.start();

        // create simple JenkinsClient for testing
        final Gson gson = new Gson();
        final OkHttpClient httpClient = new OkHttpClient();
        final JenkinsClient jenkinsClient = new JenkinsClient(httpClient, gson);

        // the unit under test
        OkHttpClient client = jenkinsClient.client;

        var request = new Request.Builder()
                .url(server.url("/build/1337/versionInfo.properties"))
                .tag(JenkinsClient.PropertiesRequest.class, new JenkinsClient.PropertiesRequest())
                .build();

        Response response = client.newCall(request).execute();

        assertNull(response.header("Expires"));
        assertEquals("max-age=2592000", response.header("Cache-control"));
        assertEquals(2, response.headers().size(),
                "should only contain 'Content-length' (default) and 'Cache-control' headers, but was: " + response.headers().toString());

        // Shut down the server. Instances cannot be reused.
        server.shutdown();
    }

    @Test
    @DisplayName("should not tweak requests without 'PropertiesRequest' tag")
    void httpClientDoesNotInterceptHeadersForNonPropertiesRequests() throws IOException {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().addHeader("Expires", new Instant(0L)));
        server.start();

        // create simple JenkinsClient for testing
        final Gson gson = new Gson();
        final OkHttpClient httpClient = new OkHttpClient();
        final JenkinsClient jenkinsClient = new JenkinsClient(httpClient, gson);

        // the unit under test
        OkHttpClient client = jenkinsClient.client;

        var request = new Request.Builder()
                .url(server.url("/build/1337/info"))
                .build();

        Response response = client.newCall(request).execute();

        assertEquals("1970-01-01T00:00:00.000Z", response.header("Expires"));
        assertEquals(2, response.headers().size(),
                "should only contain 'Content-length' (default) and 'Expires' headers, but was: " + response.headers().toString());

        // Shut down the server. Instances cannot be reused.
        server.shutdown();
    }
}
