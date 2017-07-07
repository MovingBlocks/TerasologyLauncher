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

package org.terasology.launcher.game;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.ProgressListener;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GameDownloader {

    private static final Logger logger = LoggerFactory.getLogger(GameDownloader.class);

    private final TerasologyGameVersions gameVersions;

    private final Path downloadZipFile;
    private final URL downloadURL;
    private final Path gameDirectory;
    private final boolean saveDownloadedFiles;

    public GameDownloader(Path downloadDirectory, Path tempDirectory, boolean saveDownloadedFiles, Path gameParentDirectory, TerasologyGameVersion gameVersion,
                          TerasologyGameVersions gameVersions) throws IOException {
        this.gameVersions = gameVersions;
        this.saveDownloadedFiles = saveDownloadedFiles;

        final String jobName = gameVersion.getJob().name();
        final Integer buildNumber = gameVersion.getBuildNumber();

        DirectoryUtils.checkDirectory(tempDirectory);
        if (saveDownloadedFiles) {
            downloadZipFile = downloadDirectory.resolve(jobName + "_" + buildNumber.toString() + "_" + System.currentTimeMillis() + ".zip");
        } else {
            downloadZipFile = tempDirectory.resolve(jobName + "_" + buildNumber.toString() + "_" + System.currentTimeMillis() + ".zip");
        }
        if (Files.exists(downloadZipFile) && (!Files.isRegularFile(downloadZipFile) || !Files.deleteIfExists(downloadZipFile))) {
            throw new IOException("Could not delete file! " + downloadZipFile);
        }

        // If we have a matching Omega distribution for this game version then fetch that zip file instead
        if (gameVersion.getOmegaNumber() != null) {
            logger.info("Omega distribution {} is available for that engine build, downloading it", gameVersion.getOmegaNumber());
            downloadURL = DownloadUtils.createFileDownloadUrlJenkins(gameVersion.getJob().getOmegaJobName(),
                    gameVersion.getOmegaNumber(), DownloadUtils.FILE_TERASOLOGY_OMEGA_ZIP);
        } else {
            logger.warn("Engine build {} has no Omega zip available! Falling back to main zip without extra modules", buildNumber);
            downloadURL = DownloadUtils.createFileDownloadUrlJenkins(jobName, buildNumber, DownloadUtils.FILE_TERASOLOGY_GAME_ZIP);
        }
        logger.info("The download URL is {}", downloadURL);

        final Path gameJobDirectory = gameParentDirectory.resolve(gameVersion.getJob().getInstallationDirectory()).resolve(jobName);
        DirectoryUtils.checkDirectory(gameJobDirectory);
        gameDirectory = gameJobDirectory.resolve(buildNumber.toString());
        DirectoryUtils.checkDirectory(gameDirectory);
        FileUtils.deleteDirectoryContent(gameDirectory);
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    public Path getGameDirectory() {
        return gameDirectory;
    }

    public void download(ProgressListener listener) throws DownloadException {
        long contentLength = DownloadUtils.getContentLength(downloadURL);
        long availableSpace = downloadZipFile.toFile().getParentFile().getUsableSpace();
        if (availableSpace >= contentLength) {
            DownloadUtils.downloadToFile(downloadURL, downloadZipFile, listener);
        } else {
            logger.error("Insufficient space in " + downloadZipFile.getParent());
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("message_error_insufficientSpace"));
        }
    }

    public boolean extractAfterDownload() {
        return FileUtils.extractZipTo(downloadZipFile, gameDirectory);
    }

    public void deleteSilentAfterExtract() {
        if (!saveDownloadedFiles) {
            FileUtils.deleteFileSilently(downloadZipFile);
        }
    }

    public void deleteSilentAfterCancel() {
        FileUtils.deleteFileSilently(downloadZipFile);
    }

    public boolean updateAfterDownload() {
        return gameVersions.updateGameVersionsAfterInstallation(gameDirectory);
    }
}
