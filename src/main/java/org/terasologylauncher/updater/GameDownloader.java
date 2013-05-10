/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasologylauncher.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.BuildType;
import org.terasologylauncher.gui.LauncherFrame;
import org.terasologylauncher.util.BundleUtils;
import org.terasologylauncher.util.DownloadUtils;
import org.terasologylauncher.util.FileUtils;
import org.terasologylauncher.version.TerasologyGameVersion;
import org.terasologylauncher.version.TerasologyGameVersions;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author MrBarsack
 * @author Skaldarnar
 */
public final class GameDownloader extends SwingWorker<Void, Void> {

    private static final Logger logger = LoggerFactory.getLogger(GameDownloader.class);

    private static final String ZIP_FILE = "Terasology.zip";

    private final JProgressBar progressBar;
    private final LauncherFrame frame;
    private final File terasologyDirectory;
    private final TerasologyGameVersions gameVersions;
    private final TerasologyGameVersion gameVersion;

    public GameDownloader(final JProgressBar progressBar, final LauncherFrame frame,
                          final File terasologyDirectory, final TerasologyGameVersion gameVersion,
                          final TerasologyGameVersions gameVersions) {
        this.progressBar = progressBar;
        this.frame = frame;
        this.terasologyDirectory = terasologyDirectory;
        this.gameVersion = gameVersion;
        this.gameVersions = gameVersions;

        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setString(BundleUtils.getLabel("update_game_startDownload"));
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setString(null);
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            }
        });
    }

    @Override
    protected Void doInBackground() {
        final String jobName;
        if (BuildType.STABLE == gameVersion.getBuildType()) {
            jobName = DownloadUtils.TERASOLOGY_STABLE_JOB_NAME;
        } else {
            jobName = DownloadUtils.TERASOLOGY_NIGHTLY_JOB_NAME;
        }
        final Integer buildNumber = gameVersion.getBuildNumber();

        // try to do the download
        URL url;
        File file;
        try {
            url = DownloadUtils.createFileDownloadURL(jobName, buildNumber, DownloadUtils.FILE_TERASOLOGY_GAME_ZIP);
            final long dataSize = url.openConnection().getContentLength() / 1024 / 1024;

            InputStream in = null;
            OutputStream out = null;

            try {

                file = new File(terasologyDirectory, ZIP_FILE);

                in = url.openConnection().getInputStream();
                out = new FileOutputStream(file);

                final byte[] buffer = new byte[2048];

                int n;
                while ((n = in.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                    final long fileSizeMB = file.length() / 1024 / 1024;
                    float percentage = fileSizeMB / (float) dataSize;
                    percentage *= 100;

                    setProgress((int) percentage);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (MalformedURLException e) {
            logger.error("Could not download game!", e);
        } catch (IOException e) {
            logger.error("Could not download game!", e);
        }
        return null;
    }

    @Override
    protected void done() {
        logger.debug("Download is done");
        // unzip downloaded file
        progressBar.setValue(100);
        progressBar.setString(BundleUtils.getLabel("update_game_extractZip"));
        progressBar.setStringPainted(true);

        File zip;
        zip = new File(terasologyDirectory, ZIP_FILE);
        FileUtils.extractZip(zip);

        progressBar.setString(BundleUtils.getLabel("update_game_gameInfo"));
        progressBar.setStringPainted(true);

        gameVersions.updateGameVersionsAfterInstallation(terasologyDirectory);
        frame.updateGui();

        zip.delete();

        progressBar.setVisible(false);
    }
}
