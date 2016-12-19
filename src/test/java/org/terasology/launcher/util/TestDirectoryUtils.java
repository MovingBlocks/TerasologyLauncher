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

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.terasology.launcher.util.DirectoryUtils.checkDirectory;
import static org.terasology.launcher.util.DirectoryUtils.containsFiles;
import static org.terasology.launcher.util.DirectoryUtils.containsGameData;

public class TestDirectoryUtils {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test(expected = IOException.class)
    public void testCannotCreateDirectory() throws IOException {
        File directory = mock(File.class);
        when(directory.exists()).thenReturn(false);
        when(directory.mkdirs()).thenReturn(false);

        checkDirectory(directory);
    }

    @Test(expected = IOException.class)
    public void testNotDirectory() throws IOException {
        File directory = mock(File.class);
        when(directory.exists()).thenReturn(true);
        when(directory.isDirectory()).thenReturn(false);

        checkDirectory(directory);
    }

    @Test(expected = IOException.class)
    public void testNoPerms() throws IOException {
        File directory = mock(File.class);
        when(directory.canRead()).thenReturn(false);
        when(directory.canWrite()).thenReturn(false);

        checkDirectory(directory);
    }

    @Test
    public void testDirectoryWithFiles() throws IOException {
        File directory = tempFolder.newFolder();
        File file = new File(directory, "File");
        assertTrue(file.createNewFile());
        assertTrue(containsFiles(directory));
    }

    @Test
    public void testEmptyDirectory() throws IOException {
        File directory = tempFolder.newFolder();
        assertFalse(containsFiles(directory));
    }

    @Test
    public void testGameDirectory() throws IOException {
        File gameDirectory = tempFolder.newFolder();
        File savesDirectory = new File(gameDirectory, "saves");
        File saveFile = new File(savesDirectory, "saveFile");
        assertTrue(savesDirectory.mkdir());
        assertTrue(saveFile.createNewFile());
        assertTrue(containsGameData(gameDirectory));
    }
}
