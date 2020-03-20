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

import com.vdurmont.semver4j.Semver;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public final class LauncherUpdater {

    //TODO: use semver library to compare version strings

    //TODO: connect to GitHub to check for new version (only consider stable releases for now)

    //TODO: fetch all releases (including pre-releases) based on user settings<

    private static final Logger logger = LoggerFactory.getLogger(LauncherUpdater.class);


    private final Semver currentVersion;

    private GitHub github;

    private GHRelease latestRelease;

    private Path launcherInstallationDirectory;

    public LauncherUpdater(TerasologyLauncherVersionInfo currentVersionInfo) {

        currentVersion = new Semver(currentVersionInfo.getVersion());

        try {
            github = GitHub.connectAnonymously();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method indicates if a new launcher version is available.
     * <br>
     * Compares the current launcher version number to the upstream version number if an internet connection is available.
     *
     * @return whether an update is available
     */
    public boolean updateAvailable() {
        //TODO return Option<GHRelease> instead of side-effect
        if (github != null) {
            try {
                final GHRelease latestRelease = github.getOrganization("MovingBlocks")
                        .getRepository("TerasologyLauncher")
                        .getLatestRelease();

                final Semver latestVersion = new Semver(latestRelease.getTagName().replaceAll("^v(.*)$", "$1"));

                final boolean updateAvailable = latestVersion.isGreaterThan(currentVersion);

                if (updateAvailable) {
                    this.latestRelease = latestRelease;
                }

                return updateAvailable;
            } catch (IOException e) {
                logger.warn("Could not fetch latest release: {}", e.getMessage());
            }
        } else {
            logger.warn("Could not connect to GitHub");
        }
        return false;
    }

    public void detectAndCheckLauncherInstallationDirectory() throws URISyntaxException, IOException {
        final Path launcherLocation = Paths.get(LauncherUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        logger.trace("Launcher location: {}", launcherLocation);
        launcherInstallationDirectory = launcherLocation.getParent().getParent();
        FileUtils.ensureWritableDir(launcherInstallationDirectory);
        logger.trace("Launcher installation directory: {}", launcherInstallationDirectory);
    }

    public boolean showUpdateDialog(Stage parentStage) {
        final String infoText = getUpdateInfo();

        FutureTask<Boolean> dialog = new FutureTask<Boolean>(() -> {
            Parent root = BundleUtils.getFXMLLoader("update_dialog").load();
            ((TextArea) root.lookup("#infoTextArea")).setText(infoText);
            ((TextArea) root.lookup("#changelogTextArea")).setText(latestRelease.getBody());

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
                .append(currentVersion.getValue())
                .append("  \n")
                .append("  ")
                .append(BundleUtils.getLabel("message_update_latest"))
                .append("  ")
                .append(latestRelease.getTagName())
                .append("  \n")
                .append("  ")
                .append(BundleUtils.getLabel("message_update_installationDirectory"))
                .append("  ")
                .append(launcherInstallationDirectory.toString())
                .append("  ");
        return builder.toString();
    }

    public boolean update(Path downloadDirectory, Path tempDirectory) {

        //TODO: splash.getInfoLabel().setText(BundleUtils.getLabel("splash_updatingLauncher_download"));
        try {
            //TODO: resolve correct package base on current installation(?)
            Optional<GHAsset> releaseAsset = latestRelease.getAssets().stream()
                    .filter(asset -> asset.getName().equals("<os>_<arch>"))
                    .findFirst();

            releaseAsset.ifPresent(asset -> {
                //TODO: use label instead, and figure out the filename....
                final Path downloadedZipFile =
                        downloadDirectory.resolve(asset.getName() + "_" + latestRelease.getTagName() + "_" + System.currentTimeMillis() + ".zip");

                //TODO: DownloadUtils.downloadToFile(updateURL, downloadedZipFile, new DummyProgressListener());

                //TODO: splash.getInfoLabel().setText(BundleUtils.getLabel("splash_updatingLauncher_updating"));

                // Extract launcher ZIP file
                final boolean extracted = FileUtils.extractZipTo(downloadedZipFile, tempDirectory);

                logger.trace("ZIP file extracted");

                final Path tempLauncherDirectory = tempDirectory.resolve("TerasologyLauncher");
                try {
                    FileUtils.ensureWritableDir(tempLauncherDirectory);

                    logger.info("Current launcher path: {}", launcherInstallationDirectory.toString());
                    logger.info("New files temporarily located in: {}", tempLauncherDirectory.toAbsolutePath());

                    SelfUpdater.runUpdate(tempLauncherDirectory, launcherInstallationDirectory);
                } catch (IOException e) {
                    logger.error("Launcher update failed! Aborting update process!", e);
                    GuiUtils.showErrorMessageDialog(null, BundleUtils.getLabel("update_launcher_updateFailed"));
                }
            });
        } catch (IOException e) {

        }
        return false;
    }
}
