// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.google.common.base.Preconditions;
import okhttp3.OkHttpClient;

import java.net.URL;
import java.util.Properties;
import java.util.function.Function;

class StubJenkinsClient extends JenkinsClient {
    final Function<URL, Jenkins.ApiResult> request;
    final Function<URL, Properties> requestProperties;

    StubJenkinsClient(Function<URL, Jenkins.ApiResult> request, Function<URL, Properties> requestProperties) {
        super(new OkHttpClient(), null);
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
