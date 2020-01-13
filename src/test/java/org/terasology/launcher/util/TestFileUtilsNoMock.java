package org.terasology.launcher.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Power mock trying make public all methods from jdk.nio.zipfs. that restricted and not needed (deleteFile)
 */
public class TestFileUtilsNoMock {

    private static final String FILE_NAME = "File";
    private static final String SAMPLE_TEXT = "Lorem Ipsum";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testExtract() throws IOException {
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
        Path zipFile = tempFolder.newFile(FILE_NAME + ".zip").toPath();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zipOutputStream.putNextEntry(new ZipEntry(fileInRoot));
            zipOutputStream.write(file1Contents.getBytes());
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry(fileInFolder));
            zipOutputStream.write(file2Contents.getBytes());
            zipOutputStream.closeEntry();
        }

        Path outputDir = tempFolder.newFolder().toPath();
        FileUtils.extractZipTo(zipFile, outputDir);
        Path extractedFileInRoot = outputDir.resolve(fileInRoot);
        Path extractedFileInFolder = outputDir.resolve(fileInFolder);
        assertTrue(Files.exists(extractedFileInRoot));
        assertTrue(Files.exists(extractedFileInFolder));
        assertEquals(file1Contents, Files.readAllLines(extractedFileInRoot).get(0));
        assertEquals(file2Contents, Files.readAllLines(extractedFileInFolder).get(0));
    }
}
