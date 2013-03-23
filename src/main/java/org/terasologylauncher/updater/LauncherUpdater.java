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
import org.terasologylauncher.util.DirectoryUtils;
import org.terasologylauncher.util.DownloadException;
import org.terasologylauncher.util.DownloadUtils;
import org.terasologylauncher.util.FileUtils;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public final class LauncherUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LauncherUpdater.class);

    private final File applicationDir;
    private final String currentVersion;
    private Integer upstreamVersion;

    public LauncherUpdater(final File applicationDir, final String currentVersion) {
        this.applicationDir = applicationDir;
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
        try {
            // TODO Switch to TERASOLOGY_LAUNCHER_STABLE_JOB_NAME
            upstreamVersion = DownloadUtils.loadLatestStableVersion(DownloadUtils.TERASOLOGY_LAUNCHER_NIGHTLY_JOB_NAME);
            logger.debug("Current Version: {}, Upstream Version: {}", currentVersion, upstreamVersion);
            return Integer.parseInt(currentVersion) < upstreamVersion;
        } catch (NumberFormatException e) {
            logger.error("Could not parse current version! " + currentVersion, e);
        } catch (DownloadException e) {
            logger.error("Could not load latest stable version!", e);
        }
        return false;
    }

    public void update() {
        // get temporary update path
        final File temporaryUpdateDir = new File(applicationDir, DirectoryUtils.TMP);
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
        File launcherLocation = new File(LauncherUpdater.class.getProtectionDomain().getCodeSource().getLocation()
            .getPath());
        // TODO: download new files, store to tmp path and run self updater
        try {
            // TODO Switch to TERASOLOGY_LAUNCHER_STABLE_JOB_NAME
            URL updateURL = DownloadUtils.getDownloadURL(DownloadUtils.TERASOLOGY_LAUNCHER_NIGHTLY_JOB_NAME,
                upstreamVersion, "TerasologyLauncher.zip");

            // download the latest zip file to tmp dir

            File downloadedZipFile = new File(temporaryUpdateDir, "TerasologyLauncher.zip");
            DownloadUtils.downloadToFile(updateURL, downloadedZipFile);

            // Extract ZIP file
            FileUtils.extractZip(downloadedZipFile);

            // Start SelfUpdater
            SelfUpdater.runUpdate(temporaryUpdateDir, launcherLocation);
        } catch (MalformedURLException e) {
            logger.error("Launcher update failed!", e);
            // TODO Message and title
            JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
            logger.error("Aborting update process!");
        } catch (IOException e) {
            logger.error("Launcher update failed!", e);
            // TODO Message and title
            JOptionPane.showMessageDialog(null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
            logger.error("Aborting update process!");
        }
    }
}
