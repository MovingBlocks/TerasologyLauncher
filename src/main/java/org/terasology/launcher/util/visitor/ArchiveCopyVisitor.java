/*
 * Copyright 2017 MovingBlocks
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
 * It works for coping between different {@link FileSystemProvider}.
 */
public class ArchiveCopyVisitor extends SimpleFileVisitor<Path> {

    /**
     * Directory to which the files are copied
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
