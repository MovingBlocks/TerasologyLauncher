// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

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

        try (var mockedClientClass = mockStatic(JenkinsClient.class)) {
            mockedClientClass.when(() -> JenkinsClient.openStream(any())).thenThrow(IOException.class);

            final JenkinsClient client = new JenkinsClient(gson);
            assertNull(client.request(url));
        }
    }

    @Test
    @DisplayName("can handle invalid JSON payload")
    void canHandleInvalidJsonPayload() throws InterruptedException {
        final Gson gson = new Gson();
        final JenkinsClient client = new JenkinsClient(gson);

        InputStream invalidPayload = new ByteArrayInputStream("{ this is ] no json |[!".getBytes());

        URL urlToInvalidPayload = mock(URL.class);

        try (MockedStatic<JenkinsClient> utilities = Mockito.mockStatic(JenkinsClient.class)) {
            utilities.when(() -> JenkinsClient.openStream(urlToInvalidPayload)).thenReturn(invalidPayload);
            assertNull(client.request(urlToInvalidPayload));
        }
    }
}
