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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.terasology.launcher.util.LauncherDirectoryUtils.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LauncherDirectoryUtils.class)
public class TestLauncherDirectoryUtils {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test(expected = IOException.class)
    public void testCannotCreateDirectory() throws IOException {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.exists(directory)).thenReturn(false);
        when(Files.createDirectories(directory)).thenThrow(new IOException("Failed to create directories"));

        checkDirectory(directory);
    }

    @Test(expected = IOException.class)
    public void testNotDirectory() throws IOException {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.exists(directory)).thenReturn(true);
        when(Files.isDirectory(directory)).thenReturn(false);

        checkDirectory(directory);
    }

    @Test(expected = IOException.class)
    public void testNoPerms() throws IOException {
        PowerMockito.mockStatic(Files.class);

        final Path directory = mock(Path.class);
        when(Files.isReadable(directory)).thenReturn(false);
        when(Files.isWritable(directory)).thenReturn(false);

        checkDirectory(directory);
    }

    @Test
    public void testDirectoryWithFiles() throws IOException {
        Path directory = tempFolder.newFolder().toPath();
        Path file = directory.resolve("File");
        assertNotNull(Files.createFile(file));
        assertTrue(containsFiles(directory));
    }

    @Test
    public void testEmptyDirectory() throws IOException {
        Path directory = tempFolder.newFolder().toPath();
        assertFalse(containsFiles(directory));
    }

    @Test
    public void testGameDirectory() throws IOException {
        Path gameDirectory = tempFolder.newFolder().toPath();
        Path savesDirectory = gameDirectory.resolve(GameDataDirectoryNames.SAVES.getName());
        Path saveFile = savesDirectory.resolve("saveFile");

        Files.createDirectories(savesDirectory);
        Files.createFile(saveFile);
        assertTrue(containsGameData(gameDirectory));
    }

    @Test
    public void testApplicationDirectoryWindows() {
        Path expectedApplicationPath = Paths.get("C:/Users/Test/AppData/Roaming/Unit Test");
        PowerMockito.mockStatic(System.class);

        when(System.getenv("APPDATA")).thenReturn("C:/Users/Test/AppData/Roaming");
        when(System.getProperty("user.home", ".")).thenReturn("C:/Users/Test");

        assertEquals(expectedApplicationPath, getApplicationDirectory(OperatingSystem.WINDOWS_8, "Unit Test"));
    }

    @Test
    public void testApplicationDirectoryWindowsNoAppData() {
        Path expectedApplicationPath = Paths.get("C:/Users/Test/Unit Test");
        PowerMockito.mockStatic(System.class);

        when(System.getenv("APPDATA")).thenReturn(null);
        when(System.getProperty("user.home", ".")).thenReturn("C:/Users/Test");

        assertEquals(expectedApplicationPath, getApplicationDirectory(OperatingSystem.WINDOWS_8, "Unit Test"));
    }

    @Test
    public void testApplicationDirectoryUnix() {
        Path expectedApplicationPath = Paths.get("/home/test/.unit test");
        PowerMockito.mockStatic(System.class);

        when(System.getProperty("user.home", ".")).thenReturn("/home/test");

        assertEquals(expectedApplicationPath, getApplicationDirectory(OperatingSystem.UNIX, "Unit Test"));
    }

    @Test
    public void testApplicationDirectoryMac() {
        Path expectedApplicationPath = Paths.get("/home/test/Library/Application Support/Unit Test");
        PowerMockito.mockStatic(System.class);

        when(System.getProperty("user.home", ".")).thenReturn("/home/test");

        assertEquals(expectedApplicationPath, getApplicationDirectory(OperatingSystem.MAC_OSX, "Unit Test"));
    }

    @Test
    public void testApplicationDirectoryUnknown() {
        Path expectedApplicationPath = Paths.get("/Users/test/Unit Test");
        PowerMockito.mockStatic(System.class);

        when(System.getProperty("user.home", ".")).thenReturn("/Users/test");

        assertEquals(expectedApplicationPath, getApplicationDirectory(OperatingSystem.UNKNOWN, "Unit Test"));
    }
}
