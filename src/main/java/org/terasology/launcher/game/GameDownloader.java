/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.launcher.game;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class GameDownloader {

    private static final Logger logger = LoggerFactory.getLogger(GameDownloader.class);

    private final TerasologyGameVersions gameVersions;

    private final File downloadZipFile;
    private final URL downloadURL;
    private final File gameDirectory;
    private final boolean saveDownloadedFiles;

    public GameDownloader(File downloadDirectory, File tempDirectory, boolean saveDownloadedFiles, File gameParentDirectory, TerasologyGameVersion gameVersion,
                          TerasologyGameVersions gameVersions) throws IOException {
        this.gameVersions = gameVersions;
        this.saveDownloadedFiles = saveDownloadedFiles;

        final String jobName = gameVersion.getJob().name();
        final Integer buildNumber = gameVersion.getBuildNumber();

        DirectoryUtils.checkDirectory(tempDirectory);
        if (saveDownloadedFiles) {
            downloadZipFile = new File(downloadDirectory, jobName + "_" + buildNumber.toString() + "_" + System.currentTimeMillis() + ".zip");
        } else {
            downloadZipFile = new File(tempDirectory, jobName + "_" + buildNumber.toString() + "_" + System.currentTimeMillis() + ".zip");
        }
        if (downloadZipFile.exists() && (!downloadZipFile.isFile() || !downloadZipFile.delete())) {
            throw new IOException("Could not delete file! " + downloadZipFile);
        }
        downloadURL = DownloadUtils.createFileDownloadUrlJenkins(jobName, buildNumber, DownloadUtils.FILE_TERASOLOGY_GAME_ZIP);
        final File gameJobDirectory = new File(new File(gameParentDirectory, gameVersion.getJob().getInstallationDirectory()), jobName);
        DirectoryUtils.checkDirectory(gameJobDirectory);
        gameDirectory = new File(gameJobDirectory, buildNumber.toString());
        DirectoryUtils.checkDirectory(gameDirectory);
        FileUtils.deleteDirectoryContent(gameDirectory);
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    public File getGameDirectory() {
        return gameDirectory;
    }

    public void download(ProgressListener listener) throws DownloadException {
        DownloadUtils.downloadToFile(downloadURL, downloadZipFile, listener);
    }

    public boolean extractAfterDownload() {
        return FileUtils.extractZipTo(downloadZipFile, gameDirectory);
    }

    public void deleteSilentAfterExtract() {
        if (!saveDownloadedFiles) {
            final boolean deleted = downloadZipFile.delete();
            if (!deleted) {
                logger.error("Could not delete downloaded ZIP file '{}'!", downloadZipFile);
            }
        }
    }

    public void deleteSilentAfterCancel() {
        final boolean deleted = downloadZipFile.delete();
        if (!deleted) {
            logger.error("Could not delete downloaded ZIP file '{}'!", downloadZipFile);
        }
    }

    public boolean updateAfterDownload() {
        return gameVersions.updateGameVersionsAfterInstallation(gameDirectory);
    }
}
