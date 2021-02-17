// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@DisplayName("JenkinsClient")
class JenkinsClientTest {

    @Test
    @DisplayName("should handle IOException on request(url) gracefully")
    void nullOnIoException() throws IOException {
        final Gson gson = new Gson();
        final JenkinsClient client = new JenkinsClient(gson);

        URL urlThrowingException = mock(URL.class);
        doThrow(IOException.class).when(urlThrowingException).openStream();

        assertNull(client.request(urlThrowingException));
    }
}