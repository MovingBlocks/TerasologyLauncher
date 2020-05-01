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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spf4j.log.Level;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.matchers.LogMatchers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFileUtils {

    private static final String FILE_NAME = "File";
    private static final String DIRECTORY_NAME = "lorem";
    private static final String SAMPLE_TEXT = "Lorem Ipsum";

    @TempDir
    public Path tempFolder;

    @Test
    public void testCannotCreateDirectory() {
        final Path directory = tempFolder.resolve(DIRECTORY_NAME);
        var tempFolderFile = tempFolder.toFile();
        assert tempFolderFile.setWritable(false);

        try {
            assertThrows(IOException.class, () ->
                    FileUtils.ensureWritableDir(directory)
            );
        } finally {
            assert tempFolderFile.setWritable(true); // so @TempDir can tidy up
        }
    }

    @Test
    public void testNotDirectory() throws IOException {
        var notDirectory = tempFolder.resolve("notADirectory");
        Files.createFile(notDirectory);

        var exc = assertThrows(IOException.class, () ->
                FileUtils.ensureWritableDir(notDirectory)
        );
        assertThat(exc.getMessage(), startsWith("Not a directory"));
    }

    @Test
    public void testNoPerms() throws IOException {
        var directory = tempFolder.resolve(DIRECTORY_NAME);
        Files.createDirectory(directory, PosixFilePermissions.asFileAttribute(Collections.emptySet()));

        try {
            var exc = assertThrows(IOException.class, () ->
                    FileUtils.ensureWritableDir(directory)
            );
            assertThat(exc.getMessage(), startsWith("Missing read or write permissions"));
        } finally {
            // oh no, @TempDir doesn't know how to delete this! make it writable again.
            var d = directory.toFile();
            assert d.setReadable(true);
            assert d.setWritable(true);
        }
    }

    @Test
    public void testDeleteFile() throws IOException {
        Path directory = tempFolder;
        Path file = directory.resolve(FILE_NAME);
        Files.createFile(file);
        assertTrue(Files.exists(file));
        FileUtils.delete(directory);
        assertFalse(Files.exists(file));
        assertFalse(Files.exists(directory));
    }

    @Test
    public void testDeleteDirectoryContent() throws IOException {
        Path directory = tempFolder;
        Path file = directory.resolve(FILE_NAME);
        Files.createFile(file);
        assertTrue(Files.exists(file));
        FileUtils.deleteDirectoryContent(directory);
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(directory));
    }

    /**
     * Test that `FileUtils.ensureEmptyDir` creates and empty directory if it does not exist.
     */
    @Test
    public void testEnsureEmptyDirCreation() throws IOException {
        Path context = tempFolder;
        // setup
        Path dirToTest = context.resolve(DIRECTORY_NAME);
        assertFalse(Files.exists(dirToTest));
        // test
        FileUtils.ensureEmptyDir(dirToTest);
        assertTrue(Files.exists(dirToTest));
        assertTrue(Files.isDirectory(dirToTest));
        assertEquals(0, Files.list(dirToTest).count());
    }

    /**
     * Test that `FileUtils.ensureEmptyDir` drains (delete all content) if the directory exists.
     */
    @Test
    public void testEnsureEmptyDirDrain() throws IOException {
        Path context = tempFolder;
        // setup
        Path dirToTest = context.resolve(DIRECTORY_NAME);
        Path file = dirToTest.resolve(FILE_NAME);
        Files.createDirectory(dirToTest);
        Files.createFile(file);
        // assure directory exists and is not empty
        assertTrue(Files.exists(dirToTest));
        assertTrue(Files.exists(file));
        // test
        FileUtils.ensureEmptyDir(dirToTest);
        assertTrue(Files.exists(dirToTest));
        assertTrue(Files.isDirectory(dirToTest));
        assertEquals(0, Files.list(dirToTest).count());
    }

    @Test
    public void testCopyFolder(@TempDir Path source, @TempDir Path destination) throws IOException {
        Path fileInSource = source.resolve(FILE_NAME);
        Files.createFile(fileInSource);
        assertTrue(Files.exists(fileInSource));
        List<String> text = Collections.singletonList(SAMPLE_TEXT);
        Files.write(fileInSource, text, StandardCharsets.UTF_8);

        Path fileInDestination = destination.resolve(FILE_NAME);

        FileUtils.copyFolder(source, destination);

        assertTrue(Files.exists(fileInDestination));
        assertArrayEquals(Files.readAllBytes(fileInSource), Files.readAllBytes(fileInDestination));
    }

    @Test
    public void testDeleteFileSilently() throws IOException {
        Path tempFile = tempFolder.resolve(FILE_NAME);
        Files.createFile(tempFile);
        assertTrue(Files.exists(tempFile));

        FileUtils.deleteFileSilently(tempFile);
        assertTrue(Files.notExists(tempFile));
    }

    @Test
    public void testDeleteFileSilentlyWithEmptyDirectory() {
        assertTrue(Files.exists(tempFolder));

        FileUtils.deleteFileSilently(tempFolder);
        assertTrue(Files.notExists(tempFolder));
    }

    @Test
    public void testDeleteFileSilentlyWithNonEmptyDirectory() throws IOException {
        Path tempFile = tempFolder.resolve(FILE_NAME);
        Files.createFile(tempFile);
        assertTrue(Files.exists(tempFile));

        // DirectoryNotEmptyException will be logged but not thrown
        var loggedException = TestLoggers.sys().expect("", Level.ERROR,
                LogMatchers.hasMatchingExtraThrowable(Matchers.instanceOf(DirectoryNotEmptyException.class))
        );

        FileUtils.deleteFileSilently(tempFolder);

        assertTrue(Files.exists(tempFolder));
        loggedException.assertObservation();
    }

    @Test
    public void testExtract(@TempDir Path zipDir, @TempDir Path outputDir) throws IOException {
        final String fileInRoot = "fileInRoot";
        final String fileInFolder = "folder/fileInFolder";
        final String file1Contents = SAMPLE_TEXT + "1";
        final String file2Contents = SAMPLE_TEXT + "2";
        /* An archive with this structure is created:
         * <zip root>
         * +-- fileInRoot
         * +-- folder
         * |   +-- fileInFolder
         */
        Path zipFile = zipDir.resolve(FILE_NAME + ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zipOutputStream.putNextEntry(new ZipEntry(fileInRoot));
            zipOutputStream.write(file1Contents.getBytes());
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry(fileInFolder));
            zipOutputStream.write(file2Contents.getBytes());
            zipOutputStream.closeEntry();
        }

        FileUtils.extractZipTo(zipFile, outputDir);
        Path extractedFileInRoot = outputDir.resolve(fileInRoot);
        Path extractedFileInFolder = outputDir.resolve(fileInFolder);
        assertTrue(Files.exists(extractedFileInRoot));
        assertTrue(Files.exists(extractedFileInFolder));
        assertEquals(file1Contents, Files.readAllLines(extractedFileInRoot).get(0));
        assertEquals(file2Contents, Files.readAllLines(extractedFileInFolder).get(0));
    }
}
