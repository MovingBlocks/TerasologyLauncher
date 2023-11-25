// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.io;

import java.nio.file.Path;

//TODO: define behavior in error cases, annotate non-null, etc.
public interface Installation<T> {

    /**
     * @return The full path to the location of the installation.
     */
    Path getPath();

    /**
     * @return The information object describing <i>what</i> is installed.
     */
    T getInfo();

}
