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

package org.terasology.launcher.updater;

import org.terasology.launcher.util.*;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;

final class SelfLauncherUpdater extends AbstractLauncherUpdater {

    SelfLauncherUpdater(File launcherInstallationDirectory, TerasologyLauncherVersionInfo currentVersionInfo) {
        super(launcherInstallationDirectory, currentVersionInfo);
    }

    @Override
    public boolean update(File downloadDirectory, File tempDirectory) {
        try {
            logger.trace("Downloading launcher...");
            //TODO: splash.getInfoLabel().setText(BundleUtils.getLabel("splash_updatingLauncher_download"));

            // Download launcher ZIP file
            final URL updateURL = DownloadUtils.createFileDownloadUrlJenkins(getJobName(), getUpstreamVersion(), DownloadUtils.FILE_TERASOLOGY_LAUNCHER_ZIP);
            logger.trace("Update URL: {}", updateURL);

            final File downloadedZipFile = new File(downloadDirectory, getJobName() + "_" + getUpstreamVersion() + "_" + System.currentTimeMillis() + ".zip");
            logger.trace("Download ZIP file: {}", downloadedZipFile);

            DownloadUtils.downloadToFile(updateURL, downloadedZipFile, new DummyProgressListener());

            //TODO: splash.getInfoLabel().setText(BundleUtils.getLabel("splash_updatingLauncher_updating"));

            // Extract launcher ZIP file
            final boolean extracted = FileUtils.extractZipTo(downloadedZipFile, tempDirectory);
            if (!extracted) {
                throw new IOException("Could not extract ZIP file! " + downloadedZipFile);
            }
            logger.trace("ZIP file extracted");

            final File tempLauncherDirectory = new File(tempDirectory, "TerasologyLauncher");
            DirectoryUtils.checkDirectory(tempLauncherDirectory);

            logger.info("Current launcher path: {}", getLauncherInstallationDirectory().getPath());
            logger.info("New files temporarily located in: {}", tempLauncherDirectory.getPath());

            // Start SelfUpdater
            SelfUpdater.runUpdate(tempLauncherDirectory, getLauncherInstallationDirectory());
        } catch (DownloadException | IOException | RuntimeException e) {
            logger.error("Launcher update failed! Aborting update process!", e);
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("update_launcher_updateFailed"));
            return false;
        }
        return true;
    }
}
