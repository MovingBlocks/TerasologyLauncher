/*
 * Copyright 2017 MovingBlocks
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

import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinDef;
import org.terasology.launcher.util.*;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;

final class WinInstallerLauncherUpdater extends AbstractLauncherUpdater {

    WinInstallerLauncherUpdater(File launcherInstallationDirectory, TerasologyLauncherVersionInfo currentVersionInfo) {
        super(launcherInstallationDirectory, currentVersionInfo);
    }

    @Override
    public boolean update(File downloadDirectory, File tempDirectory) {
        try {
            logger.trace("Downloading launcher installer...");

            // Download launcher ZIP file
            final URL updateURL = DownloadUtils.createFileDownloadUrlJenkins(getJobName(), getUpstreamVersion(), DownloadUtils.FILE_TERASOLOGY_LAUNCHER_WIN_INSTALLER);
            logger.trace("Update URL: {}", updateURL);

            final File downloadedInstaller = new File(downloadDirectory, getJobName() + "_" + getUpstreamVersion() + "_" + System.currentTimeMillis() + ".exe");
            logger.trace("Download new installer file: {}", downloadedInstaller);

            DownloadUtils.downloadToFile(updateURL, downloadedInstaller, new DummyProgressListener());

            // Start the installer
            // Note about last parameter (nShowCmd): 1 is SW_SHOWNORMAL (see https://msdn.microsoft.com/en-us/library/windows/desktop/bb762153(v=vs.85).aspx)
            WinDef.INT_PTR result = Shell32.INSTANCE.ShellExecute(null, "runas", downloadedInstaller.getAbsolutePath(), null, null, 1);
            if (result.intValue() > 32) { //ShellExecute success
                logger.info("Installer started");
                return true;
            } else {
                logger.warn("Failed to start installer, ShellExecute returned " + result.toString());
                return false;
            }
        } catch (DownloadException | IOException | RuntimeException e) {
            logger.error("Launcher update failed! Aborting update process!", e);
            GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("update_launcher_updateFailed"));
            return false;
        }
    }

}
