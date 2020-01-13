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
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class TestFileUtils {

    private static final String FILE_NAME = "File";
    private static final String DIRECTORY_NAME = "lorem";
    private static final String SAMPLE_TEXT = "Lorem Ipsum";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test(expected = IOException.class)
    public void testCannotCreateDirectory() throws IOException {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.exists(directory)).thenReturn(false);
        when(Files.createDirectories(directory)).thenThrow(new IOException("Failed to create directories"));

        FileUtils.ensureWritableDir(directory);
    }

    @Test(expected = IOException.class)
    public void testNotDirectory() throws IOException {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.exists(directory)).thenReturn(true);
        when(Files.isDirectory(directory)).thenReturn(false);

        FileUtils.ensureWritableDir(directory);
    }

    @Test(expected = IOException.class)
    public void testNoPerms() throws IOException {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.isReadable(directory)).thenReturn(false);
        when(Files.isWritable(directory)).thenReturn(false);

        FileUtils.ensureWritableDir(directory);
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
    public void testDeleteDirectoryContent() throws IOException {
        Path directory = tempFolder.newFolder().toPath();
        Path file = directory.resolve(FILE_NAME);
        file = Files.createFile(file);
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
        Path context = tempFolder.newFolder().toPath();
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
        Path context = tempFolder.newFolder().toPath();
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
    public void testDeleteFileSilently() throws IOException {
        Path tempFile = tempFolder.newFile(FILE_NAME).toPath();
        assertTrue(Files.exists(tempFile));

        FileUtils.deleteFileSilently(tempFile);
        assertTrue(Files.notExists(tempFile));
    }

    @Test
    public void testDeleteFileSilentlyWithEmptyDirectory() throws IOException {
        Path tempDirectory = this.tempFolder.newFolder().toPath();
        assertTrue(Files.exists(tempDirectory));

        FileUtils.deleteFileSilently(tempDirectory);
        assertTrue(Files.notExists(tempDirectory));
    }

    @Test
    public void testDeleteFileSilentlyWithNonEmptyDirectory() throws IOException {
        Path tempDirectory = tempFolder.newFolder().toPath();
        Path tempFile = tempDirectory.resolve(FILE_NAME);
        Files.createFile(tempFile);
        assertTrue(Files.exists(tempFile));

        // DirectoryNotEmptyException will be logged but not thrown
        FileUtils.deleteFileSilently(tempDirectory);
        assertTrue(Files.exists(tempDirectory));
    }

}
