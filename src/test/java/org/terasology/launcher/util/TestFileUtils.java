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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

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

//    Currently fails due to inconsistent File.getName() behaviour
//
    @Test
    public void testExtract() throws IOException {
        File textFile = tempFolder.newFile();
        textFile.createNewFile();
        List<String> text = Arrays.asList(SAMPLE_TEXT);
        Files.write(textFile.toPath(), text, Charset.forName("UTF-8"));

        FileInputStream textFileIS = new FileInputStream(textFile);
        File zipFile = tempFolder.newFile(FILE_NAME + ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        out.putNextEntry(new ZipEntry(textFile.getPath()));
        byte[] b = new byte[1024];
        int count;
        while ((count = textFileIS.read(b)) > 0) {
            out.write(b, 0, count);
        }
        out.close();
        textFileIS.close();

        File outputDir = tempFolder.newFolder();
        FileUtils.extractZipTo(zipFile, outputDir);
        File extractedTextFile = new File(outputDir, textFile.getName());
        assertArrayEquals(Files.readAllLines(textFile.toPath()).toArray(), Files.readAllLines(extractedTextFile.toPath()).toArray());
    }

}
