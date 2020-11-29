// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Functional interface to abstract over directory creation.
 */
public interface DirectoryCreator {
    /**
     * Create a directory.
     * <p>
     * This abstraction hides the concrete implementation of how the directory should be created, and if other
     * operations should be applied.
     * This interface can be instantiated using method references, such as {@code FileUtils::ensureEmptyDir} for
     * {@link FileUtils#ensureEmptyDir(Path)}.
     *
     * @param path absolute path to the directory to be created.
     * @throws IOException if anything fails while creating the directory.
     */
    void apply(Path path) throws IOException;
}
