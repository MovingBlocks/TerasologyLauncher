// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.mock.MockInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
                //TODO: I'd like to specify the URL here, but then matcher does not match.
                //      Somehow, the matcher surrounds the expected URL with some weird characters.
                //      expected=\Qhttps://jenkins.example\E;actual=https://jenkins.example/; matcher=url(~=\Qhttps://jenkins.example\E)
                .get()
                .respond("{ this is ] no json |[!");

        final var httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        final JenkinsClient client = new JenkinsClient(httpClient, new Gson());

        assertNull(client.request(urlToInvalidPayload));
    }
}
