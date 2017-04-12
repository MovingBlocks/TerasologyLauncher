/*
 * Copyright 2016 MovingBlocks
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.file.Files;

public final class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    private static final String COULD_NOT_CREATE_DIRECTORY = "Could not create directory! ";

    private FileUtils() {
    }

    /**
     * Deletes the specified file or directory (directories are removed recursively).
     *
     * @param file - file to delete
     * @throws IOException if something goes wrong
     */
    public static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if ((files != null) && (files.length > 0)) {
                for (File child : files) {
                    FileUtils.delete(child);
                }
            }
        }
        final boolean deleted = file.delete();
        if (!deleted) {
            throw new IOException("Could not delete file! " + file);
        }
    }

    public static void deleteDirectoryContent(File directory) throws IOException {
        if (directory.isDirectory()) {
            final File[] files = directory.listFiles();
            if ((files != null) && (files.length > 0)) {
                for (File child : files) {
                    FileUtils.delete(child);
                }
            }
        }
    }

    /**
     * Computes the total size of the given file or directory.
     * @param file file or directory to compute size of
     * @return if the given filesystem object is a file, the same value of {@link File#delete()};
     *      if it's a directory, the recursively computed size of it's contents.
     */
    public static long computeTotalSize(File file) {
        if (file.isDirectory()) {
            long size = 0;
            final File[] files = file.listFiles();
            if ((files != null) && (files.length > 0)) {
                for (File child : files) {
                    size += FileUtils.computeTotalSize(child);
                }
            }
            return size;
        } else {
            return file.length();
        }
    }

    /**
     * Formats the specified number of bytes in a string using SI units.
     * @param byteCount The number of bytes to format. Must be >= 0.
     * @return The readable string representation of the argument expressed in SI unit, rounded to two decimal places.
     */
    public static String formatByteCount(long byteCount) {
        if (byteCount < 0) {
            throw new IllegalArgumentException("Negative byte sizes are not supported.");
        }
        if (byteCount == 0) {
            return String.format("%.2f B", 0F);
        }
        final String[] units = new String[] {"", "k", "M", "G", "T"};
        int unitIndex = (int) (Math.log(byteCount) / Math.log(1000)); //log base 1000 of byteSize, truncated to int
        return String.format("%.2f %sB", byteCount / Math.pow(1000, unitIndex), units[unitIndex]);
    }

    /**
     * Extracts the given ZIP file to its parent folder.
     *
     * @param archive - the ZIP file to extract
     * @return true if successful
     */
    public static boolean extractZip(File archive) {
        return FileUtils.extractZipTo(archive, archive.getParentFile());
    }

    /**
     * Extracts the specified ZIP file to the specified location.
     *
     * @param archive        - the ZIP file to extract
     * @param outputLocation - where to extract to
     * @return true if successful
     */
    public static boolean extractZipTo(File archive, File outputLocation) {

        logger.trace("Extracting '{}' to '{}'.", archive, outputLocation);

        try {
            if (!outputLocation.exists()) {
                boolean created = outputLocation.mkdir();
                if (!created) {
                    throw new IOException(COULD_NOT_CREATE_DIRECTORY + outputLocation);
                }
            }
            final byte[] buffer = new byte[4096];
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archive))) {
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    File extractedFile = new File(outputLocation, com.google.common.io.Files.getNameWithoutExtension(ze.getName()));
                    File extractedDir = extractedFile.getParentFile();
                    if (!extractedDir.exists()) {
                        boolean created = extractedDir.mkdirs();
                        if (!created) {
                            throw new IOException(COULD_NOT_CREATE_DIRECTORY + extractedDir);
                        }
                    }
                    if (!ze.isDirectory()) {
                        try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                            int c;
                            while ((c = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, c);
                            }
                            fos.flush();
                        }
                    }
                }
            }
            return true;
        } catch (IOException e) {
            logger.error("Could not extract zip archive '{}' to '{}'!", archive, outputLocation, e);
        }
        return false;
    }

    private static void copyFile(File source, File destination) throws IOException {
        if (!source.exists()) {
            logger.error("Source file doesn't exists! '{}'", source);
            return;
        }

        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copy the whole folder recursively to the specified destination.
     *
     * @param source      - the folder to copy
     * @param destination - where to copy to
     * @throws IOException if something goes wrong
     */
    public static void copyFolder(File source, File destination) throws IOException {
        if (!source.exists()) {
            logger.error("Source file doesn't exists! '{}'", source);
            return;
        }

        if (source.isDirectory()) {
            if (!destination.exists()) {
                final boolean created = destination.mkdirs();
                if (!created) {
                    throw new IOException(COULD_NOT_CREATE_DIRECTORY + destination);
                }
            }
            final String[] files = source.list();
            if ((files != null) && (files.length > 0)) {
                for (String file : files) {
                    final File srcFile = new File(source, file);
                    final File destFile = new File(destination, file);
                    FileUtils.copyFolder(srcFile, destFile);
                }
            }
        } else {
            FileUtils.copyFile(source, destination);
        }
    }
}
