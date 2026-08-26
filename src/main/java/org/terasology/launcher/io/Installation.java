// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.io;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

//TODO: define behavior in error cases, annotate non-null, etc.
public interface Installation<T> {

    /**
     * Returns the full path to the location of the installation.
     */
    Path getPath();

    /**
     * Returns what's installed, or {@code null} if that can't be derived (e.g. bad layout).
     */
    @Nullable T getInfo();

}
