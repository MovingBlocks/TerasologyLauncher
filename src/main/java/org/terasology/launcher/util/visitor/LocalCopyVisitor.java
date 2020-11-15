// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.launcher.util.visitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;

/**
 * This visitor will only work when files are copied between the same {@link FileSystemProvider}.
 */
public class LocalCopyVisitor extends SimpleFileVisitor<Path> {

    /**
     * Directory to copy from.
     */
    private final Path sourceDirectory;

    /**
     * Directory to copy to.
     */
    private final Path targetDirectory;

    public LocalCopyVisitor(final Path sourceDirectory, final Path targetDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.targetDirectory = targetDirectory;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Files.copy(file, targetDirectory.resolve(sourceDirectory.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        final Path dirToCreate = targetDirectory.resolve(sourceDirectory.relativize(dir));
        if (Files.notExists(dirToCreate)) {
            Files.createDirectories(dirToCreate);
        }
        return FileVisitResult.CONTINUE;
    }
}
