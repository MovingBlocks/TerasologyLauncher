/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.FileNotFoundException;
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
     * @return whether deletion was successful
     */
    public static boolean delete(final File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        return file.delete();
    }

    public static void deleteDirectoryContent(final File directory) {
        if (directory.isDirectory()) {
            for (File child : directory.listFiles()) {
                delete(child);
            }
        }
    }

    /**
     * Extracts the given ZIP file to its parent folder.
     *
     * @param archive - the ZIP file to extract
     */
    public static void extractZip(final File archive) {
        extractZipTo(archive, archive.getParentFile());
    }

    /**
     * Extracts the specified ZIP file to the specified location.
     *
     * @param archive        - the ZIP file to extract
     * @param outputLocation - where to extract to
     */
    public static void extractZipTo(final File archive, final File outputLocation) {
        logger.info("Extracting {}.", archive);

        byte[] buffer = new byte[4096];
        ZipInputStream zis = null;
        ZipEntry ze;

        try {
            if (!outputLocation.exists()) {
                outputLocation.mkdir();
            }
            zis = new ZipInputStream(new FileInputStream(archive));
            while ((ze = zis.getNextEntry()) != null) {
                File extractedFile = new File(outputLocation, ze.getName());
                extractedFile.getParentFile().mkdirs();
                if (!ze.isDirectory()) {
                    FileOutputStream fos = new FileOutputStream(extractedFile);
                    int c;
                    while ((c = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, c);
                    }
                    fos.flush();
                    fos.close();
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not find zip archive.", e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (zis != null) {
                try {
                    zis.closeEntry();
                    zis.close();
                } catch (IOException ignored) {
                    logger.warn("Could not close zip input stream.", ignored);
                }
            }
        }
    }

    /**
     * Copies the single source file to the specified destination.
     *
     * @param source      - the file to copy
     * @param destination - where to copy to
     * @throws IOException
     */
    public static void copyFile(final File source, final File destination) throws IOException {
        if (!source.exists()) {
            return;
        }

        if (!destination.exists()) {
            destination.createNewFile();
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
            return;
        }

        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            String[] files = source.list();
            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);
                copyFolder(srcFile, destFile);
            }
        } else {
            copyFile(source, destination);
        }
    }
}
