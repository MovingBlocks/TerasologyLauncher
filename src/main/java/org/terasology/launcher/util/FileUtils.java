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

package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.visitor.ArchiveCopyVisitor;
import org.terasology.launcher.util.visitor.DeleteFileVisitor;
import org.terasology.launcher.util.visitor.LocalCopyVisitor;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * Deletes the specified file or directory. If the path is a directory it is removed recursively.
     *
     * @param path Path to delete
     * @throws IOException if something goes wrong
     */
    public static void delete(final Path path) throws IOException {
        Files.walkFileTree(path, new DeleteFileVisitor());
    }

    /**
     * Deletes the specified file. If deletion of the file fails or a exception happens it will be logged.
     * Directories are not handled by this deletion.
     * @param file File to delete
     */
    public static void deleteFileSilently(final Path file) {
        try {
            final boolean deleted = Files.deleteIfExists(file);
            if (!deleted) {
                logger.error("Could not silently delete file '{}'!", file);
            }
        } catch (IOException e) {
            logger.error("Could not silently delete file '{}'!", file, e);
        }
    }

    /**
     * Deletes the content of the directory without removing the directory itself.
     *
     * @param directory Directory which content has to be deleted
     * @throws IOException if something goes wrong
     */
    public static void deleteDirectoryContent(final Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            stream.forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        Files.walkFileTree(directory, new DeleteFileVisitor());
                    } else {
                        Files.delete(path);
                    }
                } catch (IOException e) {
                    logger.error("Failed to delete '{}'", directory, e);
                }
            });
        }
    }

    /**
     * Extracts the specified ZIP file to the specified location.
     *
     * @param archive        the ZIP file to extract
     * @param outputLocation where to extract to
     * @return true if successful
     */
    public static boolean extractZipTo(final Path archive, final Path outputLocation) {
        logger.trace("Extracting '{}' to '{}'", archive, outputLocation);

        try {
            if (Files.notExists(outputLocation)) {
                Files.createDirectories(outputLocation);
            }
            try (FileSystem fileSystem = FileSystems.newFileSystem(archive, null)) {
                for (Path rootDirectory : fileSystem.getRootDirectories()) {
                    Files.walkFileTree(rootDirectory, new ArchiveCopyVisitor(outputLocation));
                }
            }
            return true;
        } catch (IOException e) {
            logger.error("Could not extract zip archive '{}' to '{}'!", archive, outputLocation, e);
            return false;
        }
    }

    /**
     * Copy file from the source path to the destination. If the source file does not exists nothing happens.
     *
     * @param source      File to copy
     * @param destination Destination for the copy
     * @throws IOException If copying the file fails
     */
    private static void copyFile(final Path source, final Path destination) throws IOException {
        if (Files.notExists(source)) {
            logger.error("Source file doesn't exists! '{}'", source);
            return;
        }
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copy the whole folder recursively to the specified destination.
     *
     * @param source      the folder to copy
     * @param destination where to copy to
     * @throws IOException if copying fails
     */
    public static void copyFolder(final Path source, final Path destination) throws IOException {
        if (Files.notExists(source)) {
            logger.error("Source file doesn't exists! '{}'", source);
            return;
        }

        if (Files.isDirectory(source)) {
            Files.walkFileTree(source, new LocalCopyVisitor(source, destination));
        } else {
            copyFile(source, destination);
        }
    }
}
