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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class TestFileUtils {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static final String FILE_NAME = "File";
    private static final String SAMPLE_TEXT = "Lorem Ipsum";

    @Test
    public void testDeleteFile_Deprecated() throws IOException {
        File directory = tempFolder.newFolder();
        File file = new File(directory, FILE_NAME);
        assertTrue(file.createNewFile());
        FileUtils.delete(directory);
        assertFalse(file.exists());
        assertFalse(directory.exists());
    }

    @Test
    public void testDeleteFile() throws IOException {
        Path directory = tempFolder.newFolder().toPath();
        Path file = directory.resolve(FILE_NAME);
        file = Files.createFile(file);
        assertTrue(Files.exists(file));
        FileUtils.delete(directory);
        assertFalse(Files.exists(file));
        assertFalse(Files.exists(directory));
    }

    @Test
    public void testDeleteDirectoryContent_Deprecated() throws IOException {
        File directory = tempFolder.newFolder();
        File file = new File(directory, FILE_NAME);
        assertTrue(file.createNewFile());
        FileUtils.deleteDirectoryContent(directory);
        assertFalse(file.exists());
        assertTrue(directory.exists());
    }

    @Test
    public void testDeleteDirectoryContent() throws IOException {
        Path directory = tempFolder.newFolder().toPath();
        Path file = directory.resolve(FILE_NAME);
        file = Files.createFile(file);
        assertTrue(Files.exists(file));
        FileUtils.deleteDirectoryContent(directory);
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(directory));
    }

    @Test
    public void testCopyFolder_Deprecated() throws IOException {
        File source = tempFolder.newFolder();
        File fileInSource = new File(source, FILE_NAME);
        fileInSource.createNewFile();
        List<String> text = Arrays.asList(SAMPLE_TEXT);
        Files.write(fileInSource.toPath(), text, Charset.forName("UTF-8"));

        File destination = tempFolder.newFolder();
        File fileInDestination = new File(destination, FILE_NAME);
        FileUtils.copyFolder(source, destination);

        assertTrue(fileInDestination.exists());
        assertArrayEquals(Files.readAllLines(fileInSource.toPath()).toArray(), Files.readAllLines(fileInDestination.toPath()).toArray());
    }

    @Test
    public void testCopyFolder() throws IOException {
        Path source = tempFolder.newFolder().toPath();
        Path fileInSource = source.resolve(FILE_NAME);
        fileInSource = Files.createFile(fileInSource);
        assertTrue(Files.exists(fileInSource));
        List<String> text = Collections.singletonList(SAMPLE_TEXT);
        Files.write(fileInSource, text, StandardCharsets.UTF_8);

        Path destination = tempFolder.newFolder().toPath();
        Path fileInDestination = destination.resolve(FILE_NAME);

        FileUtils.copyFolder(source, destination);

        assertTrue(Files.exists(fileInDestination));
        assertArrayEquals(Files.readAllBytes(fileInSource), Files.readAllBytes(fileInDestination));
    }

    @Test
    public void testExtract_Deprecated() throws IOException {
        final String FILE_IN_ROOT = "fileInRoot";
        final String FILE_IN_FOLDER = "folder/fileInFolder";
        final String FILE1_CONTENTS = SAMPLE_TEXT + "1";
        final String FILE2_CONTENTS = SAMPLE_TEXT + "2";
        /* An archive with this structure is created:
         * <zip root>
         * +-- fileInRoot
         * +-- folder
         * |   +-- fileInFolder
         */
        File zipFile = tempFolder.newFile(FILE_NAME + ".zip");
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOutputStream.putNextEntry(new ZipEntry(FILE_IN_ROOT));
        zipOutputStream.write(FILE1_CONTENTS.getBytes());
        zipOutputStream.closeEntry();
        zipOutputStream.putNextEntry(new ZipEntry(FILE_IN_FOLDER));
        zipOutputStream.write(FILE2_CONTENTS.getBytes());
        zipOutputStream.closeEntry();
        zipOutputStream.close();

        File outputDir = tempFolder.newFolder();
        FileUtils.extractZipTo(zipFile, outputDir);
        File extractedFileInRoot = new File(outputDir, FILE_IN_ROOT);
        File extractedFileInFolder = new File(outputDir, FILE_IN_FOLDER);
        assertTrue(extractedFileInRoot.exists());
        assertTrue(extractedFileInFolder.exists());
        assertEquals(FILE1_CONTENTS, Files.readAllLines(extractedFileInRoot.toPath()).get(0));
        assertEquals(FILE2_CONTENTS, Files.readAllLines(extractedFileInFolder.toPath()).get(0));
    }

    @Test
    public void testExtract() throws IOException {
        final String FILE_IN_ROOT = "fileInRoot";
        final String FILE_IN_FOLDER = "folder/fileInFolder";
        final String FILE1_CONTENTS = SAMPLE_TEXT + "1";
        final String FILE2_CONTENTS = SAMPLE_TEXT + "2";
        /* An archive with this structure is created:
         * <zip root>
         * +-- fileInRoot
         * +-- folder
         * |   +-- fileInFolder
         */
        Path zipFile = tempFolder.newFile(FILE_NAME + ".zip").toPath();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zipOutputStream.putNextEntry(new ZipEntry(FILE_IN_ROOT));
            zipOutputStream.write(FILE1_CONTENTS.getBytes());
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry(FILE_IN_FOLDER));
            zipOutputStream.write(FILE2_CONTENTS.getBytes());
            zipOutputStream.closeEntry();
        }

        Path outputDir = tempFolder.newFolder().toPath();
        FileUtils.extractZipTo(zipFile, outputDir);
        Path extractedFileInRoot = outputDir.resolve(FILE_IN_ROOT);
        Path extractedFileInFolder = outputDir.resolve(FILE_IN_FOLDER);
        assertTrue(Files.exists(extractedFileInRoot));
        assertTrue(Files.exists(extractedFileInFolder));
        assertEquals(FILE1_CONTENTS, Files.readAllLines(extractedFileInRoot).get(0));
        assertEquals(FILE2_CONTENTS, Files.readAllLines(extractedFileInFolder).get(0));
    }

}
