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
import org.terasology.launcher.gui.SplashScreenWindow;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import javax.swing.JOptionPane;
import java.io.File;
import java.net.URL;

public final class LauncherUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LauncherUpdater.class);

    private final OperatingSystem os;
    private final File downloadDirectory;
    private final String currentVersion;
    private final String jobName;

    private Integer upstreamVersion;
    private TerasologyLauncherVersionInfo versionInfo;

    public LauncherUpdater(final OperatingSystem os, final File downloadDirectory,
                           final String currentVersion, final String jobName) {
        this.os = os;
        this.downloadDirectory = downloadDirectory;
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
            logger.trace("Current Version: {}, Upstream Version: {}", currentVersion, upstreamVersion);
            if (Integer.parseInt(currentVersion) < upstreamVersion) {
                updateAvailable = true;
                versionInfo = DownloadUtils.loadTerasologyLauncherVersionInfo(jobName, upstreamVersion);
            }
        } catch (NumberFormatException e) {
            logger.error("The current version '{}' could not be parsed!", currentVersion, e);
        } catch (DownloadException e) {
            logger.warn("The latest stable version of the launcher could not be determined!", e);
        }
        return updateAvailable;
    }

    public void update(final SplashScreenWindow splash) {
        try {
            // Get current launcher location
            final File launcherLocation = new File(LauncherUpdater.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI());
            logger.trace("Launcher location: {}" + launcherLocation);

            // Download launcher ZIP file
            final URL updateURL = DownloadUtils.createFileDownloadURL(jobName, upstreamVersion,
                DownloadUtils.FILE_TERASOLOGY_LAUNCHER_ZIP);
            logger.trace("Update URL: {}" + updateURL);

            final File downloadedZipFile = new File(downloadDirectory, jobName + "_" + upstreamVersion + ".zip");
            logger.trace("Download ZIP file: {}" + downloadedZipFile);

            DownloadUtils.downloadToFile(updateURL, downloadedZipFile);

            // Extract launcher ZIP file
            FileUtils.extractZip(downloadedZipFile);
            logger.trace("ZIP file extracted");

            // Delete launcher ZIP file
            downloadedZipFile.delete();
            logger.trace("ZIP file deleted");

            // Start SelfUpdater
            SelfUpdater.runUpdate(os, downloadDirectory, launcherLocation);
        } catch (Exception e) {
            logger.error("Launcher update failed! Aborting update process!", e);
            JOptionPane.showMessageDialog(splash,
                BundleUtils.getLabel("update_launcher_updateFailed"),
                BundleUtils.getLabel("message_error_title"),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public Integer getUpstreamVersion() {
        return upstreamVersion;
    }

    public TerasologyLauncherVersionInfo getVersionInfo() {
        return versionInfo;
    }
}
