/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.launcher.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.version.TerasologyGameVersion;
import org.terasology.launcher.version.TerasologyGameVersions;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

final class GameDownloader extends SwingWorker<Void, Void> {

    private static final Logger logger = LoggerFactory.getLogger(GameDownloader.class);

    private final LauncherFrame frame;
    private final TerasologyGameVersions gameVersions;

    private final File downloadZipFile;
    private final URL downloadURL;
    private final File gameDirectory;

    private boolean successfulDownloadAndExtract;
    private boolean successfulLoadVersion;

    public GameDownloader(final JProgressBar progressBar, final LauncherFrame frame, final File tempDirectory, final File gamesDirectory,
                          final TerasologyGameVersion gameVersion, final TerasologyGameVersions gameVersions) throws IOException {
        this.frame = frame;
        this.gameVersions = gameVersions;

        final String jobName = gameVersion.getJob().name();
        final Integer buildNumber = gameVersion.getBuildNumber();

        DirectoryUtils.checkDirectory(tempDirectory);
        downloadZipFile = new File(tempDirectory, jobName + "_" + buildNumber.toString() + ".zip");
        if (downloadZipFile.exists() && (!downloadZipFile.isFile() || !downloadZipFile.delete())) {
            throw new IOException("The ZIP file already exists and can not be deleted! " + downloadZipFile);
        }
        downloadURL = DownloadUtils.createFileDownloadURL(jobName, buildNumber, DownloadUtils.FILE_TERASOLOGY_GAME_ZIP);
        final File gamesSubDirectory = new File(new File(gamesDirectory, gameVersion.getJob().getInstallationDirectory()), jobName);
        DirectoryUtils.checkDirectory(gamesSubDirectory);
        gameDirectory = new File(gamesSubDirectory, buildNumber.toString());

        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setString(null);
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            }
        });
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if ("progressString".equals(evt.getPropertyName())) {
                    progressBar.setString((String) evt.getNewValue());
                }
            }
        });
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString(BundleUtils.getLabel("update_game_startDownload"));
        progressBar.setVisible(true);
        progressBar.setToolTipText(downloadURL.toString());
    }

    @Override
    protected Void doInBackground() {
        try {
            setProgress(0);
            downloadZipFile();
            if (!isCancelled()) {
                setProgress(100);

                firePropertyChange("progressString", null, BundleUtils.getLabel("update_game_extractZip"));
                FileUtils.extractZipTo(downloadZipFile, gameDirectory);
                boolean deleted = downloadZipFile.delete();
                if (!deleted) {
                    logger.warn("Cannot delete downloaded ZIP file! '{}'", downloadZipFile);
                }
                successfulDownloadAndExtract = true;

                firePropertyChange("progressString", null, BundleUtils.getLabel("update_game_gameInfo"));
                successfulLoadVersion = gameVersions.updateGameVersionsAfterInstallation(gameDirectory);
            }
        } catch (Exception e) {
            logger.error("There is an error occurred while downloading the game!", e);
        }
        return null;
    }

    private void downloadZipFile() throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(downloadURL.openStream());
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(downloadZipFile))) {
            final float sizeFactor = 100f / (float) downloadURL.openConnection().getContentLength();

            final byte[] buffer = new byte[2048];

            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);

                int percentage = (int) (sizeFactor * (float) downloadZipFile.length());
                if (percentage < 1) {
                    percentage = 1;
                } else if (percentage >= 100) {
                    percentage = 99;
                }
                setProgress(percentage);

                if (isCancelled()) {
                    break;
                }
            }
        }
    }

    @Override
    protected void done() {
        frame.finishedGameDownload(isCancelled(), successfulDownloadAndExtract, successfulLoadVersion, gameDirectory);
    }
}
