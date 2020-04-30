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

import org.junit.jupiter.api.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.terasology.launcher.util.LauncherDirectoryUtils.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LauncherDirectoryUtils.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class TestLauncherDirectoryUtils {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

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
}
