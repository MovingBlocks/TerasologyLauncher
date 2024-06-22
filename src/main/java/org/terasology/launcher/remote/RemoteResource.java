// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.remote;

import java.net.URL;

public interface RemoteResource<T> {

    URL getUrl();

    String getFilename();

    T getInfo();

    //TODO: String getChecksum();
}
