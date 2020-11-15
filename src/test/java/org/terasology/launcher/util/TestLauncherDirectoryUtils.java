// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
