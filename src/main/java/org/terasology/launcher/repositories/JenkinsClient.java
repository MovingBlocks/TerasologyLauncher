// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class JenkinsClient {

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
}
