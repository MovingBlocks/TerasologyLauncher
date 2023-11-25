// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import java.net.URL;

public interface Downloadable<T> {
    URL getUrl();

    default String getChecksum() {
        return null;
    }
    String getFilename();

    T getInfo();
}
