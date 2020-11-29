// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
    DOWNLOAD(FileUtils::ensureWritableDir),
    GAMES(FileUtils::ensureWritableDir);

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
