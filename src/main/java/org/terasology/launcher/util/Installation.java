// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import java.nio.file.Path;

public interface Installation<T> {
    Path getPath();

    T getInfo();
}
