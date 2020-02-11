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

import java.nio.file.Path;

/**
 * A list of directories managed by the launcher.
 *
 * These directories are local to the launcher installation directory.
 * The managed directories provide means to create them via the {@link DirectoryCreator} functional interface.
 * Each directory further holds information about the error label (i18n) to display to the user in case the setup failed.
 */
public enum LauncherManagedDirectory {
    //TODO add documentation what these folders are exactly used for
    TEMP(FileUtils::ensureEmptyDir, FileUtils::ensureWritableDir),
    CACHE(FileUtils::ensureWritableDir),
    DOWNLOAD(FileUtils::ensureWritableDir);

    private final DirectoryCreator[] creators;
    private final String errorLabel;
    private final String directoryName;

    LauncherManagedDirectory(DirectoryCreator... creators) {
        this.creators = creators;
        this.errorLabel = String.format("message_error_{}Directory", this.name().toLowerCase());
        this.directoryName = this.name().toLowerCase();
    }

    /**
     * The ordered list of {@link DirectoryCreator}s applicable for this directory.
     *
     * @return ordered list of {@link DirectoryCreator}s which can be used to create/prepare the directory.
     */
    public DirectoryCreator[] getCreators() {
        return creators;
    }

    public String getErrorLabel() {
        return errorLabel;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    /**
     * Resolves the absolute directory path relative to the given launcher installation directory.
     *
     * @param launcherInstallationPath absolute path to the launcher installation directory
     */
    public Path getDirectoryPath(Path launcherInstallationPath) {
        return launcherInstallationPath.resolve(getDirectoryName());
    }
}
