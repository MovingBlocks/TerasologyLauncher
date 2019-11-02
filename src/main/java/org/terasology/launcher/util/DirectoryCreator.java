/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
