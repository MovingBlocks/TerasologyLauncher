/*
 * Copyright 2013 MovingBlocks
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
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * Deletes the specified file or directory (directories are removed recursively).
     *
     * @param file - file to delete
     */
    public static void delete(final File file) throws IOException {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if ((files != null) && (files.length > 0)) {
                for (File child : files) {
                    FileUtils.delete(child);
                }
            }
        }
        boolean deleted = file.delete();
        if (!deleted) {
            throw new IOException("Could not delete file/directory! " + file);
        }
    }

    public static void deleteDirectoryContent(final File directory) throws IOException {
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
     * Extracts the given ZIP file to its parent folder.
     *
     * @param archive - the ZIP file to extract
     */
    public static boolean extractZip(final File archive) {
        return FileUtils.extractZipTo(archive, archive.getParentFile());
    }

    /**
     * Extracts the specified ZIP file to the specified location.
     *
     * @param archive        - the ZIP file to extract
     * @param outputLocation - where to extract to
     */
    public static boolean extractZipTo(final File archive, final File outputLocation) {
        logger.trace("Extracting '{}' to '{}'.", archive, outputLocation);

        byte[] buffer = new byte[4096];
        ZipInputStream zis = null;
        ZipEntry ze;

        try {
            if (!outputLocation.exists()) {
                boolean created = outputLocation.mkdir();
                if (!created) {
                    throw new IOException("Could not create outputLocation! " + outputLocation);
                }
            }
            zis = new ZipInputStream(new FileInputStream(archive));
            while ((ze = zis.getNextEntry()) != null) {
                File extractedFile = new File(outputLocation, ze.getName());
                File extractedDir = extractedFile.getParentFile();
                if (!extractedDir.exists()) {
                    boolean created = extractedDir.mkdirs();
                    if (!created) {
                        throw new IOException("Could not create directory! " + extractedDir);
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
            return true;
        } catch (IOException e) {
            logger.error("Could not extract zip archive '{}' to '{}'!", archive, outputLocation, e);
        } finally {
            if (zis != null) {
                try {
                    zis.closeEntry();
                    zis.close();
                } catch (IOException e) {
                    logger.warn("The zip input stream could not be closed! '{}'!", archive, e);
                }
            }
        }
        return false;
    }

    private static void copyFile(final File source, final File destination) throws IOException {
        if (!source.exists()) {
            logger.error("Source file doesn't exists! '{}'", source);
            return;
        }

        if (!destination.exists()) {
            boolean created = destination.createNewFile();
            if (!created) {
                throw new IOException("Could not create destination file! " + destination);
            }
        }

        FileChannel sourceStream = null;
        FileChannel destinationStream = null;
        try {
            sourceStream = new FileInputStream(source).getChannel();
            destinationStream = new FileOutputStream(destination).getChannel();
            destinationStream.transferFrom(sourceStream, 0, sourceStream.size());
        } finally {
            if (sourceStream != null) {
                sourceStream.close();
            }
            if (destinationStream != null) {
                destinationStream.close();
            }
        }
    }

    /**
     * Copy the whole folder recursively to the specified destination.
     *
     * @param source      - the folder to copy
     * @param destination - where to copy to
     * @throws IOException
     */
    public static void copyFolder(final File source, final File destination) throws IOException {
        if (!source.exists()) {
            logger.error("Source file doesn't exists! '{}'", source);
            return;
        }

        if (source.isDirectory()) {
            if (!destination.exists()) {
                boolean created = destination.mkdirs();
                if (!created) {
                    throw new IOException("Could not create destination directory! " + destination);
                }
            }
            String[] files = source.list();
            if ((files != null) && (files.length > 0)) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);
                    FileUtils.copyFolder(srcFile, destFile);
                }
            }
        } else {
            FileUtils.copyFile(source, destination);
        }
    }
}
