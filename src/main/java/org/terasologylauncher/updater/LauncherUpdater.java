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
import org.terasologylauncher.gui.LauncherFrame;
import org.terasologylauncher.util.DirectoryUtils;
import org.terasologylauncher.util.DownloadUtils;
import org.terasologylauncher.util.FileUtils;
import org.terasologylauncher.util.OperatingSystem;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public final class LauncherUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LauncherUpdater.class);
    private final String currentVersion;
    private String upstreamVersion;

    public LauncherUpdater(String currentVersion) {
        if (currentVersion.equals("")) {
            this.currentVersion = "0";
        } else {
            this.currentVersion = currentVersion;
        }
    }

    /**
     * This method indicates if a new launcher version is available.
     * <p/>
     * Compares the current launcher version number to the upstream version number if an internet connection is
     * available.
     *
     * @return whether an update is available
     */
    public boolean updateAvailable() {
        URL url;
        try {
            url = new URL("http://jenkins.movingblocks.net/job/TerasologyLauncher/lastSuccessfulBuild/buildNumber");
            final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            upstreamVersion = in.readLine();
            try {
                in.close();
            } catch (Exception e) {
                logger.info("Closing reader failed", e);
            }
        } catch (MalformedURLException e) {
            logger.error("Wrong/Malformed URL: {}", e);
        } catch (IOException e) {
            logger.error("IO exception reading upstream launcher version number: {}", e);
        }
        logger.debug("Current Version: {}, Upstream Verion: {}", currentVersion, upstreamVersion);
        try {
            return Integer.parseInt(currentVersion) < Integer.parseInt(upstreamVersion);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void update() {
        // get temporary update path
        final File temporaryUpdateDir = new File(DirectoryUtils.getApplicationDirectory(OperatingSystem.getOS()),
            DirectoryUtils.TMP);
        try {
            DirectoryUtils.checkDirectory(temporaryUpdateDir);
        } catch (IOException e) {
            logger.error("Can not create or use temporary update directory! - {} ", temporaryUpdateDir, e);
            // TODO Message and title
            JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
            logger.error("Aborting update process!");
            return;
        }
        // TODO: handle different executable types?

        // Get current launcher location
        File launcherLocation = new File(LauncherFrame.class.getProtectionDomain().getCodeSource().getLocation()
            .getPath());
        // TODO: download new files, store to tmp path and run self updater
        try {
            // TODO: refactor all download urls to DownloadUtils class
            URL updateURL = new URL("http://jenkins.movingblocks" +
                ".net/job/TerasologyLauncher/lastSuccessfulBuild/artifact/build/distributions/TerasologyLauncher.zip");

            // download the latest zip file to tmp dir

            File downloadedZipFile = new File(temporaryUpdateDir, "TerasologyLauncher.zip");
            DownloadUtils.downloadToFile(updateURL, downloadedZipFile);

            // Extract ZIP file
            FileUtils.extractZip(downloadedZipFile);

            // Start SelfUpdater
            SelfUpdater.runUpdate(temporaryUpdateDir, launcherLocation);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
