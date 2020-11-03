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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.terasology.launcher.util.LauncherDirectoryUtils.containsFiles;
import static org.terasology.launcher.util.LauncherDirectoryUtils.containsGameData;

class TestLauncherDirectoryUtils {

    @TempDir
    Path tempFolder;

    @Test
    void testDirectoryWithFiles() throws IOException {
        Path file = tempFolder.resolve("File");
        assertNotNull(Files.createFile(file));
        assertTrue(containsFiles(tempFolder));
    }

    @Test
    void testEmptyDirectory() throws IOException {
        assertFalse(containsFiles(tempFolder));
    }

    @Test
    void testGameDirectory() throws IOException {
        Path savesDirectory = tempFolder.resolve(GameDataDirectoryNames.SAVES.getName());
        Path saveFile = savesDirectory.resolve("saveFile");

        Files.createDirectories(savesDirectory);
        Files.createFile(saveFile);
        assertTrue(containsGameData(tempFolder));
    }
}
