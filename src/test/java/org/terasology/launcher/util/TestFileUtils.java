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
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    public void testDeleteFile() throws IOException {
        File directory = tempFolder.newFolder();
        File file = new File(directory, FILE_NAME);
        assertTrue(file.createNewFile());
        FileUtils.delete(directory);
        assertFalse(file.exists());
        assertFalse(directory.exists());
    }

    @Test
    public void testDeleteDirectoryContent() throws IOException {
        File directory = tempFolder.newFolder();
        File file = new File(directory, FILE_NAME);
        assertTrue(file.createNewFile());
        FileUtils.deleteDirectoryContent(directory);
        assertFalse(file.exists());
        assertTrue(directory.exists());
    }

    @Test
    public void testComputeTotalSize() throws IOException {
        File directory = tempFolder.newFolder();
        RandomAccessFile f = new RandomAccessFile(new File(directory, "1"), "rw");
        f.setLength(10);
        f.close();
        File subDirectory = new File(directory, "subDir");
        subDirectory.mkdir();
        f = new RandomAccessFile(new File(subDirectory, "2"), "rw");
        f.setLength(20);
        f.close();
        f = new RandomAccessFile(new File(subDirectory, "3"), "rw");
        f.setLength(12);
        f.close();
        assertEquals(42, FileUtils.computeTotalSize(directory));
    }

    @Test
    public void testFormatByteCount() {
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("en"));
        assertEquals("0.00 B", FileUtils.formatByteCount(0L));
        assertEquals("5.00 B", FileUtils.formatByteCount(5L));
        assertEquals("1.00 kB", FileUtils.formatByteCount(1000L));
        assertEquals("1.50 MB", FileUtils.formatByteCount(1500003L));
        assertEquals("2.04 GB", FileUtils.formatByteCount(2040000200L));
        assertEquals("3.00 TB", FileUtils.formatByteCount(3000000000200L));
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void testCopyFolder() throws IOException {
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
    public void testReadSingleLine() throws IOException {
        File textFile = tempFolder.newFile();
        textFile.createNewFile();
        FileWriter writer = new FileWriter(textFile);
        writer.write("test");
        writer.close();
        assertEquals("test", FileUtils.readSingleLine(textFile));
    }

    @Test(expected = IOException.class)
    public void testReadSingleLineException() throws IOException {
        File textFile = tempFolder.newFile();
        textFile.createNewFile();
        FileWriter writer = new FileWriter(textFile);
        writer.write("test1\ntest2");
        writer.close();
        FileUtils.readSingleLine(textFile);
    }

}
