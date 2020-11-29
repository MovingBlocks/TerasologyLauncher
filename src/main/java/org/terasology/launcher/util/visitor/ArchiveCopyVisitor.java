// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.launcher.util.visitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;

/**
 * Visitor for copying files from an archive to disk and preserving the archive structure.
 * It works for copying between different {@link FileSystemProvider}.
 */
public class ArchiveCopyVisitor extends SimpleFileVisitor<Path> {

    /**
     * Directory to which the files are copied.
     */
    private final Path targetLocation;

    public ArchiveCopyVisitor(final Path targetLocation) {
        this.targetLocation = targetLocation;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        final Path destFile = Paths.get(targetLocation.toString() + file.toString());
        Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        final Path dirToCreate = Paths.get(targetLocation.toString() + dir.toString());
        if (Files.notExists(dirToCreate)) {
            Files.createDirectories(dirToCreate);
        }
        return FileVisitResult.CONTINUE;
    }
}
