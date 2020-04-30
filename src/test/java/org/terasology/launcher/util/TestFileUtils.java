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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnitPlatform.class)
@PrepareForTest(FileUtils.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class TestFileUtils {

    private static final String FILE_NAME = "File";
    private static final String DIRECTORY_NAME = "lorem";
    private static final String SAMPLE_TEXT = "Lorem Ipsum";

    @TempDir
    public Path tempFolder;

    @Test
    public void testCannotCreateDirectory() throws IOException {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.exists(directory)).thenReturn(false);
        when(Files.createDirectories(directory)).thenThrow(new IOException("Failed to create directories"));

        assertThrows(IOException.class, () ->
                FileUtils.ensureWritableDir(directory)
        );
    }

    @Test
    public void testNotDirectory() {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.exists(directory)).thenReturn(true);
        when(Files.isDirectory(directory)).thenReturn(false);

        assertThrows(IOException.class, () ->
                FileUtils.ensureWritableDir(directory)
        );
    }

    @Test
    public void testNoPerms() {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.isReadable(directory)).thenReturn(false);
        when(Files.isWritable(directory)).thenReturn(false);

        assertThrows(IOException.class, () ->
                FileUtils.ensureWritableDir(directory)
        );
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
    public void testDeleteFileSilently() {
        Path tempFile = tempFolder.resolve(FILE_NAME);
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
        FileUtils.deleteFileSilently(tempFolder);
        assertTrue(Files.exists(tempFolder));
    }
}
