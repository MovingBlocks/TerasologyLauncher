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

package org.terasology.launcher.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public final class LauncherUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LauncherUpdater.class);

    private final OperatingSystem os;
    private final File applicationDir;
    private final String currentVersion;
    private final String jobName;
    private Integer upstreamVersion;
    private TerasologyLauncherVersionInfo versionInfo;

    public LauncherUpdater(final OperatingSystem os, final File applicationDir, final String currentVersion,
                           final String jobName) {
        this.os = os;
        this.applicationDir = applicationDir;
        if ((currentVersion == null) || (currentVersion.trim().length() == 0)) {
            this.currentVersion = "0";
        } else {
            this.currentVersion = currentVersion;
        }
        if ((jobName == null) || (jobName.trim().length() == 0)) {
            this.jobName = DownloadUtils.TERASOLOGY_LAUNCHER_NIGHTLY_JOB_NAME;
        } else {
            this.jobName = jobName;
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
        boolean updateAvailable = false;
        upstreamVersion = null;
        versionInfo = null;
        try {
            upstreamVersion = DownloadUtils.loadLastStableBuildNumber(jobName);
            logger.debug("Current Version: {}, Upstream Version: {}", currentVersion, upstreamVersion);
            if (Integer.parseInt(currentVersion) < upstreamVersion) {
                updateAvailable = true;
                versionInfo = DownloadUtils.loadTerasologyLauncherVersionInfo(jobName, upstreamVersion);
            }
        } catch (NumberFormatException e) {
            logger.error("Could not parse current version '{}'!", currentVersion, e);
        } catch (DownloadException e) {
            logger.error("Could not load latest stable version!", e);
        }
        return updateAvailable;
    }

    public void update() {
        // get temporary update path
        final File temporaryUpdateDir = new File(applicationDir, DirectoryUtils.TMP);
        try {
            DirectoryUtils.checkDirectory(temporaryUpdateDir);
        } catch (IOException e) {
            logger.error("Cannot create or use temporary update directory '{}'!", temporaryUpdateDir, e);
            JOptionPane.showMessageDialog(null,
                BundleUtils.getLabel("update_launcher_tmpDir") + "\n" + temporaryUpdateDir,
                BundleUtils.getLabel("message_error_title"),
                JOptionPane.ERROR_MESSAGE);
            logger.error("Aborting update process!");
            return;
        }

        // Get current launcher location
        File launcherLocation;
        try {
            launcherLocation = new File(LauncherUpdater.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        } catch (URISyntaxException e) {
            logger.error("Launcher update failed! Could not retrieve current launcher directory.", e);
            JOptionPane.showMessageDialog(null,
                BundleUtils.getLabel("update_launcher_updateFailed"),
                BundleUtils.getLabel("message_error_title"),
                JOptionPane.ERROR_MESSAGE);
            logger.error("Aborting update process!");
            return;
        }

        try {
            final URL updateURL = DownloadUtils.createFileDownloadURL(jobName, upstreamVersion,
                DownloadUtils.FILE_TERASOLOGY_LAUNCHER_ZIP);

            // download the latest zip file to tmp dir

            final File downloadedZipFile = new File(temporaryUpdateDir, "TerasologyLauncher.zip");
            DownloadUtils.downloadToFile(updateURL, downloadedZipFile);

            // Extract ZIP file
            FileUtils.extractZip(downloadedZipFile);

            // Start SelfUpdater
            SelfUpdater.runUpdate(os, temporaryUpdateDir, launcherLocation);
        } catch (MalformedURLException e) {
            logger.error("Launcher update failed!", e);
            JOptionPane.showMessageDialog(null,
                BundleUtils.getLabel("update_launcher_updateFailed"),
                BundleUtils.getLabel("message_error_title"),
                JOptionPane.ERROR_MESSAGE);
            logger.error("Aborting update process!");
        } catch (DownloadException e) {
            logger.error("Launcher update failed!", e);
            JOptionPane.showMessageDialog(null,
                BundleUtils.getLabel("update_launcher_updateFailed"),
                BundleUtils.getLabel("message_error_title"),
                JOptionPane.ERROR_MESSAGE);
            logger.error("Aborting update process!");
        }
    }

    public Integer getUpstreamVersion() {
        return upstreamVersion;
    }

    public TerasologyLauncherVersionInfo getVersionInfo() {
        return versionInfo;
    }
}
