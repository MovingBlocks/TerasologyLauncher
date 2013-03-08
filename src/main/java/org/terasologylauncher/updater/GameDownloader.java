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
import org.terasologylauncher.Settings;
import org.terasologylauncher.gui.LauncherFrame;
import org.terasologylauncher.util.Utils;
import org.terasologylauncher.util.ZIPUnpacker;

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

    public static final String ZIP_FILE = "Terasology.zip";

    private static final Logger logger = LoggerFactory.getLogger(GameDownloader.class);

    private final JProgressBar progressBar;
    private final LauncherFrame frame;

    public GameDownloader(final JProgressBar progressBar, final LauncherFrame frame) {
        this.progressBar = progressBar;
        this.frame = frame;
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setString("Starting Download");
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setString(null);
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            }
        });
    }

    @Override
    protected Void doInBackground() {
        // get the selected settings for the download
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(GameData.JENKINS);
        switch (Settings.getBuildType()) {
            case STABLE:
                urlBuilder.append(GameData.STABLE_JOB_NAME);
                break;
            case NIGHTLY:
                urlBuilder.append(GameData.NIGHTLY_JOB_NAME);
                break;
        }
        urlBuilder.append("/");
        if (Settings.getBuildVersion(Settings.getBuildType()).equals("Latest")) {
            urlBuilder.append(GameData.getUpStreamVersion(Settings.getBuildType()));
        } else {
            urlBuilder.append(Settings.getBuildVersion(Settings.getBuildType()));
        }
        urlBuilder.append("/artifact/build/distributions/").append(ZIP_FILE);

        // try to do the download
        URL url = null;
        File file = null;
        try {
            url = new URL(urlBuilder.toString());
            long dataSize = url.openConnection().getContentLength() / 1024 / 1024;

            InputStream in = null;
            OutputStream out = null;

            try {

                file = new File(Utils.getWorkingDirectory(), ZIP_FILE);

                in = url.openConnection().getInputStream();
                out = new FileOutputStream(file);

                byte[] buffer = new byte[2048];

                for (int n; (n = in.read(buffer)) != -1; out.write(buffer, 0, n)) {
                    long fileSizeMB = file.length() / 1024 / 1024;
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
        progressBar.setString("Extracting zip …");
        progressBar.setStringPainted(true);

        File zip = null;
        try {
            zip = new File(Utils.getWorkingDirectory(), ZIP_FILE);
            ZIPUnpacker.extractArchive(zip);

        } catch (IOException e) {
            logger.error("Could not unzip game!", e);
        }

        progressBar.setString("Updating game info …");
        progressBar.setStringPainted(true);

        GameData.forceReReadVersionFile();
        frame.updateStartButton();

        if (zip != null) {
            zip.delete();
        }
        progressBar.setVisible(false);
    }
}
