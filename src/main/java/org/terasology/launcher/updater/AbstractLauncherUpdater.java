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

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class AbstractLauncherUpdater {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractLauncherUpdater.class);

    private final TerasologyLauncherVersionInfo currentVersionInfo;
    private final String currentVersion;
    private final String jobName;

    private Integer upstreamVersion;
    private TerasologyLauncherVersionInfo versionInfo;
    private String changeLog;

    private File launcherInstallationDirectory;


    // constructor and getters are for use by subclasses (default access level -> access allowed only in the updater package)

    AbstractLauncherUpdater(File launcherInstallationDirectory, TerasologyLauncherVersionInfo currentVersionInfo) {
        this.launcherInstallationDirectory = launcherInstallationDirectory;
        this.currentVersionInfo = currentVersionInfo;

        if (currentVersionInfo.getBuildNumber() == null || currentVersionInfo.getBuildNumber().trim().length() == 0) {
            this.currentVersion = "0";
        } else {
            this.currentVersion = currentVersionInfo.getBuildNumber();
        }
        if (currentVersionInfo.getJobName() == null || currentVersionInfo.getJobName().trim().length() == 0) {
            this.jobName = DownloadUtils.TERASOLOGY_LAUNCHER_DEVELOP_JOB_NAME;
        } else {
            this.jobName = currentVersionInfo.getJobName();
        }
    }

    String getJobName() {
        return jobName;
    }

    Integer getUpstreamVersion() {
        return upstreamVersion;
    }

    File getLauncherInstallationDirectory() {
        return launcherInstallationDirectory;
    }

    /**
     * This method indicates if a new launcher version is available.
     * <br>
     * Compares the current launcher version number to the upstream version number if an internet connection is available.
     *
     * @return whether an update is available
     */
    public boolean updateAvailable() {
        if (this.currentVersionInfo.isEmpty()) {
            logger.trace("Skipping update check - no version info file found (assuming development environment)");
            return false;
        }

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
            this.setNewVersionInfo();
            this.setNewChangeLog();
            logger.info("An update is available to the TerasologyLauncher. '{}' '{}'", upstreamVersion, versionInfo);
        }
        return updateAvailable;
    }

    private void setNewVersionInfo() {
        URL urlVersionInfo = null;
        try {
            urlVersionInfo = DownloadUtils.createFileDownloadUrlJenkins(jobName, upstreamVersion, DownloadUtils.FILE_TERASOLOGY_LAUNCHER_VERSION_INFO);
            versionInfo = TerasologyLauncherVersionInfo.loadFromInputStream(urlVersionInfo.openStream());
        } catch (IOException e) {
            logger.warn("The launcher version info could not be loaded! '{}' '{}'", upstreamVersion, urlVersionInfo, e);
        }
    }

    private void setNewChangeLog() {
        try {
            changeLog = DownloadUtils.loadLauncherChangeLogJenkins(jobName, upstreamVersion);
        } catch (DownloadException e) {
            logger.warn("The launcher change log could not be loaded! '{}'", upstreamVersion, e);
        }
    }

    public boolean showUpdateDialog(Stage parentStage) {
        final String infoText = getUpdateInfo();

        FutureTask<Boolean> dialog = new FutureTask<Boolean>(() -> {
            Parent root = BundleUtils.getFXMLLoader("update_dialog").load();
            ((TextArea) root.lookup("#infoTextArea")).setText(infoText);
            ((TextArea) root.lookup("#changelogTextArea")).setText(changeLog);

            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(BundleUtils.getLabel("message_update_launcher_title"));
            alert.setHeaderText(BundleUtils.getLabel("message_update_launcher"));
            alert.getDialogPane().setContent(root);
            alert.initOwner(parentStage);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setResizable(true);

            return alert.showAndWait()
                    .filter(response -> response == ButtonType.YES)
                    .isPresent();
        });

        Platform.runLater(dialog);
        boolean result = false;
        try {
            result = dialog.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Uh oh, something went wrong with the update dialog!", e);
        }
        return result;
    }

    /**
     * Assemble an information message about currently installed launcher version and possible update.
     *
     * @return a multi-line information message
     */
    private String getUpdateInfo() {
        final StringBuilder builder = new StringBuilder();
        builder.append("  ")
               .append(BundleUtils.getLabel("message_update_current"))
               .append("  ")
               .append(currentVersionInfo.getDisplayVersion())
               .append("  \n")
               .append("  ")
               .append(BundleUtils.getLabel("message_update_latest"))
               .append("  ");
        if (versionInfo != null) {
            builder.append(versionInfo.getDisplayVersion());
        } else if (upstreamVersion != null) {
            builder.append(upstreamVersion);
        }
        builder.append("  \n")
               .append("  ")
               .append(BundleUtils.getLabel("message_update_installationDirectory"))
               .append("  ")
               .append(launcherInstallationDirectory.getPath())
               .append("  ");
        return builder.toString();
    }

    public abstract boolean update(File downloadDirectory, File tempDirectory);

}
