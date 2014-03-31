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

package org.terasology.launcher.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.gui.GuiUtils;
import org.terasology.launcher.gui.SplashProgressIndicator;
import org.terasology.launcher.gui.SplashScreenWindow;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public final class LauncherUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LauncherUpdater.class);

    private final TerasologyLauncherVersionInfo currentVersionInfo;
    private final String currentVersion;
    private final String jobName;

    private Integer upstreamVersion;
    private TerasologyLauncherVersionInfo versionInfo;
    private String changeLog;

    private File launcherInstallationDirectory;

    public LauncherUpdater(TerasologyLauncherVersionInfo currentVersionInfo) {
        this.currentVersionInfo = currentVersionInfo;

        if ((currentVersionInfo.getBuildNumber() == null) || (currentVersionInfo.getBuildNumber().trim().length() == 0)) {
            this.currentVersion = "0";
        } else {
            this.currentVersion = currentVersionInfo.getBuildNumber();
        }
        if ((currentVersionInfo.getJobName() == null) || (currentVersionInfo.getJobName().trim().length() == 0)) {
            this.jobName = DownloadUtils.TERASOLOGY_LAUNCHER_NIGHTLY_JOB_NAME;
        } else {
            this.jobName = currentVersionInfo.getJobName();
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
        changeLog = null;
        try {
            upstreamVersion = DownloadUtils.loadLastStableBuildNumberJenkins(jobName);
            logger.trace("Launcher upstream version: {}", upstreamVersion);
            updateAvailable = Integer.parseInt(currentVersion) < upstreamVersion;
        } catch (DownloadException e) {
            logger.warn("The latest stable version of the launcher could not be determined!", e);
        } catch (NumberFormatException e) {
            logger.error("The current version '{}' could not be parsed!", currentVersion, e);
        }
        if (updateAvailable) {
            URL urlVersionInfo = null;
            try {
                urlVersionInfo = DownloadUtils.createFileDownloadUrlJenkins(jobName, upstreamVersion, DownloadUtils.FILE_TERASOLOGY_LAUNCHER_VERSION_INFO);
                versionInfo = TerasologyLauncherVersionInfo.loadFromInputStream(urlVersionInfo.openStream());
            } catch (IOException e) {
                logger.warn("The launcher version info could not be loaded! '{}' '{}'", upstreamVersion, urlVersionInfo, e);
            }
            try {
                changeLog = DownloadUtils.loadLauncherChangeLogJenkins(jobName, upstreamVersion);
            } catch (DownloadException e) {
                logger.warn("The launcher change log could not be loaded! '{}'", upstreamVersion, e);
            }
            logger.info("An update is available to the TerasologyLauncher. '{}' '{}'", upstreamVersion, versionInfo);
        }
        return updateAvailable;
    }

    public void detectAndCheckLauncherInstallationDirectory() throws URISyntaxException, IOException {
        final File launcherLocation = new File(LauncherUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        logger.trace("Launcher location: {}", launcherLocation);
        launcherInstallationDirectory = launcherLocation.getParentFile().getParentFile();
        DirectoryUtils.checkDirectory(launcherInstallationDirectory);
        logger.trace("Launcher installation directory: {}", launcherInstallationDirectory);
    }

    public boolean showUpdateDialog(Component parentComponent) {
        final JPanel msgPanel = new JPanel(new BorderLayout(0, 10));
        final JTextArea msgLabel = new JTextArea(BundleUtils.getLabel("message_update_launcher"));
        msgLabel.setBackground(msgPanel.getBackground());
        msgLabel.setEditable(false);

        final StringBuilder builder = new StringBuilder();
        builder.append("  ");
        builder.append(BundleUtils.getLabel("message_update_current"));
        builder.append("  ");
        builder.append(currentVersionInfo.getDisplayVersion());
        builder.append("  \n");
        builder.append("  ");
        builder.append(BundleUtils.getLabel("message_update_latest"));
        builder.append("  ");
        if (versionInfo != null) {
            builder.append(versionInfo.getDisplayVersion());
        } else if (upstreamVersion != null) {
            builder.append(upstreamVersion);
        }
        builder.append("  \n");
        builder.append("  ");
        builder.append(BundleUtils.getLabel("message_update_installationDirectory"));
        builder.append("  ");
        builder.append(launcherInstallationDirectory.getPath());
        builder.append("  ");

        final JTextArea msgArea = new JTextArea();
        msgArea.setText(builder.toString());
        msgArea.setEditable(false);
        msgArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        final JTextArea changeLogArea = new JTextArea();
        changeLogArea.setText(changeLog);
        changeLogArea.setEditable(false);
        changeLogArea.setRows(15);
        changeLogArea.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 7));
        final JScrollPane changeLogPane = new JScrollPane(changeLogArea);
        changeLogPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        msgPanel.add(msgLabel, BorderLayout.NORTH);
        msgPanel.add(msgArea, BorderLayout.CENTER);
        msgPanel.add(changeLogPane, BorderLayout.SOUTH);

        final Object[] options = {BundleUtils.getLabel("main_yes"), BundleUtils.getLabel("main_no")};

        final int option = JOptionPane.showOptionDialog(parentComponent,
            msgPanel,
            BundleUtils.getLabel("message_update_launcher_title"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        return (option == 0);
    }

    public boolean update(File downloadDirectory, File tempDirectory, SplashScreenWindow splash) {
        try {
            logger.trace("Downloading launcher...");
            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_updatingLauncher_download"));

            // Download launcher ZIP file
            final URL updateURL = DownloadUtils.createFileDownloadUrlJenkins(jobName, upstreamVersion, DownloadUtils.FILE_TERASOLOGY_LAUNCHER_ZIP);
            logger.trace("Update URL: {}", updateURL);

            final File downloadedZipFile = new File(downloadDirectory, jobName + "_" + upstreamVersion + "_" + System.currentTimeMillis() + ".zip");
            logger.trace("Download ZIP file: {}", downloadedZipFile);

            DownloadUtils.downloadToFile(updateURL, downloadedZipFile, new SplashProgressIndicator(splash, "splash_updatingLauncher_download"));

            splash.getInfoLabel().setText(BundleUtils.getLabel("splash_updatingLauncher_updating"));

            // Extract launcher ZIP file
            final boolean extracted = FileUtils.extractZipTo(downloadedZipFile, tempDirectory);
            if (!extracted) {
                throw new IOException("Could not extract ZIP file! " + downloadedZipFile);
            }
            logger.trace("ZIP file extracted");

            final File tempLauncherDirectory = new File(tempDirectory, "TerasologyLauncher");
            DirectoryUtils.checkDirectory(tempLauncherDirectory);

            logger.info("Current launcher path: {}", launcherInstallationDirectory.getPath());
            logger.info("New files temporarily located in: {}", tempLauncherDirectory.getPath());

            // Start SelfUpdater
            SelfUpdater.runUpdate(tempLauncherDirectory, launcherInstallationDirectory);
        } catch (DownloadException | IOException | RuntimeException e) {
            logger.error("Launcher update failed! Aborting update process!", e);
            GuiUtils.showErrorMessageDialog(false, splash, BundleUtils.getLabel("update_launcher_updateFailed"));
            return false;
        }
        return true;
    }
}
