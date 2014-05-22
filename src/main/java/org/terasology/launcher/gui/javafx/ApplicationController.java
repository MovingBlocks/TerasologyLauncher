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

package org.terasology.launcher.gui.javafx;

import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.LauncherSettings;
import org.terasology.launcher.game.GameDownloader;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.GameStarter;
import org.terasology.launcher.game.JobItem;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.game.VersionItem;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private File launcherDirectory;
    private File downloadDirectory;
    private File tempDirectory;
    private LauncherSettings launcherSettings;
    private TerasologyGameVersions gameVersions;
    private GameStarter gameStarter;
    private GameDownloadWorker gameDownloadWorker;

    @FXML
    ChoiceBox<JobItem> jobBox;
    @FXML
    ChoiceBox<VersionItem> buildVersionBox;
    @FXML
    ProgressBar progressBar;
    @FXML
    Button downloadButton, startButton, deleteButton;


    @FXML
    protected void handleExitButtonAction(ActionEvent event) {
        logger.debug("Closing the launcher ...");
        System.exit(0);
    }

    @FXML
    protected void handleControlButtonMouseEntered(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void handleControlButtonMouseExited(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = createScaleTransition(1, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMouseEntered(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMouseExited(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = createScaleTransition(1, source);
        t.playFromStart();
    }

    @FXML
    protected void openSettingsAction(ActionEvent event) {
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(BundleUtils.getFXMLUrl("settings"), ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle"));
            Parent root = (Parent) fxmlLoader.load();
            final SettingsController settingsController = fxmlLoader.getController();
            settingsController.initialize(launcherDirectory, downloadDirectory, launcherSettings, gameVersions);

            Scene scene = new Scene(root);
            Stage settings = new Stage(StageStyle.UNDECORATED);
            settings.initModality(Modality.APPLICATION_MODAL);
            settings.setScene(scene);
            settings.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            updateGui();
        }
    }

    @FXML
    protected void startGameAction(ActionEvent event) {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        if ((gameVersion == null) || !gameVersion.isInstalled()) {
            logger.warn("The selected game version can not be started! '{}'", gameVersion);
            JOptionPane.showMessageDialog(null, BundleUtils.getLabel("message_error_gameStart"),
                BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            // updateGui();
        } else if (gameStarter.isRunning()) {
            logger.debug("The game can not be started because another game is already running.");
            JOptionPane.showMessageDialog(null, BundleUtils.getLabel("message_information_gameRunning"),
                BundleUtils.getLabel("message_information_title"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            final boolean gameStarted = gameStarter.startGame(gameVersion, launcherSettings.getGameDataDirectory(), launcherSettings.getMaxHeapSize(),
                launcherSettings.getInitialHeapSize());
            if (!gameStarted) {
                JOptionPane.showMessageDialog(null, BundleUtils.getLabel("message_error_gameStart"),
                    BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            } else if (launcherSettings.isCloseLauncherAfterGameStart()) {
                /*if (gameDownloadWorker == null) {
                    logger.info("Close launcher after game start.");
                    dispose();
                } else {
                    logger.info("The launcher can not be closed after game start, because a download is running.");
                } */
            }
        }
    }

    @FXML
    protected void downloadAction(ActionEvent event) {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        if (gameDownloadWorker != null) {
            // Cancel download
            logger.info("Cancel game download!");
            gameDownloadWorker.cancel(false);
        } else if ((gameVersion == null) || gameVersion.isInstalled() || (gameVersion.getSuccessful() == null) || !gameVersion.getSuccessful()) {
            logger.warn("The selected game version can not be downloaded! '{}'", gameVersion);
        } else {
            try {
                GameDownloader gameDownloader = new GameDownloader(downloadDirectory, tempDirectory, launcherSettings.isSaveDownloadedFiles(),
                    launcherSettings.getGameDirectory(), gameVersion, gameVersions);
                gameDownloadWorker = new GameDownloadWorker(gameDownloader);
            } catch (IOException e) {
                logger.error("Could not start game download!", e);
                finishedGameDownload(false, false, false, null);
                return;
            }
            progressBar.progressProperty().bind(gameDownloadWorker.progressProperty());
            progressBar.setVisible(true);
            new Thread(gameDownloadWorker).start();
        }
    }

    @FXML
    protected void deleteAction(ActionEvent event) {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        if ((gameVersion != null) && gameVersion.isInstalled()) {
            final boolean containsGameData = DirectoryUtils.containsGameData(gameVersion.getInstallationPath());
            final String msg;
            if (containsGameData) {
                msg = BundleUtils.getMessage("confirmDeleteGame_withData", gameVersion.getInstallationPath());
            } else {
                msg = BundleUtils.getMessage("confirmDeleteGame_withoutData", gameVersion.getInstallationPath());
            }
            final int option = JOptionPane.showConfirmDialog(null, msg, BundleUtils.getLabel("message_deleteGame_title"), JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                logger.info("Delete installed game! '{}' '{}'", gameVersion, gameVersion.getInstallationPath());
                try {
                    FileUtils.delete(gameVersion.getInstallationPath());
                } catch (IOException e) {
                    logger.error("Could not delete installed game!", e);
                    // TODO Show message dialog
                    return;
                }
                gameVersions.removeInstallationInfo(gameVersion);
                updateGui();
            }
        } else {
            logger.warn("The selected game version can not be deleted! '{}'", gameVersion);
        }
    }

    private void updateGui() {
        updateBuildVersionBox();
        updateButtons();
    }

    private void updateButtons() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        if (gameVersion == null) {
            downloadButton.setDisable(true);
            startButton.setDisable(true);
            deleteButton.setDisable(true);
        } else if (gameVersion.isInstalled()) {
            downloadButton.setDisable(true);
            startButton.setDisable(false);
            deleteButton.setDisable(false);
        } else if ((gameVersion.getSuccessful() != null) && gameVersion.getSuccessful() && (gameVersion.getBuildNumber() != null) && (gameDownloadWorker == null)) {
            downloadButton.setDisable(false);
            startButton.setDisable(true);
            deleteButton.setDisable(true);
        } else {
            downloadButton.setDisable(true);
            startButton.setDisable(true);
            deleteButton.setDisable(true);
        }

        // Cancel download
        if (gameDownloadWorker != null) {
            //downloadButton.setEnabled(true);
        }
    }

    void finishedGameDownload(boolean cancelled, boolean successfulDownloadAndExtract, boolean successfulLoadVersion, File gameDirectory) {
        gameDownloadWorker = null;
        progressBar.setVisible(false);
        if (!cancelled) {
            if (!successfulDownloadAndExtract) {
                JOptionPane.showMessageDialog(null, BundleUtils.getLabel("message_error_gameDownload_downloadExtract"),
                    BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            } else if (!successfulLoadVersion) {
                if (gameDirectory != null) {
                    JOptionPane.showMessageDialog(null, BundleUtils.getLabel("message_error_gameDownload_loadVersion") + "\n" + gameDirectory,
                        BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, BundleUtils.getLabel("message_error_gameDownload_loadVersion"),
                        BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private TerasologyGameVersion getSelectedGameVersion() {
        return gameVersions.getGameVersionForBuildVersion(launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
    }

    /**
     * Creates a {@link javafx.animation.ScaleTransition} with the given factor for the specified node element.
     *
     * @param factor the scaling factor
     * @param node   the target node
     * @return a transition object
     */
    private ScaleTransition createScaleTransition(final double factor, final Node node) {
        final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), node);
        scaleTransition.setFromX(node.getScaleX());
        scaleTransition.setFromY(node.getScaleY());
        scaleTransition.setToX(factor);
        scaleTransition.setToY(factor);
        return scaleTransition;
    }

    public void initialize(final File launcherDirectory, final File downloadDirectory, final File tempDirectory, final LauncherSettings launcherSettings, final TerasologyGameVersions gameVersions) {
        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.tempDirectory = tempDirectory;
        this.launcherSettings = launcherSettings;
        this.gameVersions = gameVersions;

        gameStarter = new GameStarter();

        populateJob();

        // add change listeners
        // TODO disable start/delete/download button if necessary
        jobBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<JobItem>() {
            @Override
            public void changed(final ObservableValue<? extends JobItem> observableValue, final JobItem oldItem, final JobItem newItem) {
                updateBuildVersionBox();
                launcherSettings.setJob(newItem.getJob());
                logger.debug("Selected gamejob: {} -- {}", launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
                updateButtons();
            }
        });

        buildVersionBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<VersionItem>() {
            @Override
            public void changed(final ObservableValue<? extends VersionItem> observableValue, final VersionItem oldVersionItem, final VersionItem newVersionItem) {
                if (newVersionItem != null) {
                    final Integer version = newVersionItem.getVersion();
                    final GameJob job = jobBox.getSelectionModel().getSelectedItem().getJob();
                    launcherSettings.setBuildVersion(version, launcherSettings.getJob());
                    logger.debug("Selected gamejob: {} -- {}", launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
                    updateButtons();
                }
            }
        });

        updateGui();
    }

    private void populateJob() {
        jobBox.getItems().clear();

        for (GameJob job : GameJob.values()) {
            if (job.isOnlyInstalled() && (launcherSettings.getJob() != job)) {
                boolean foundInstalled = false;
                final List<TerasologyGameVersion> gameVersionList = gameVersions.getGameVersionList(job);
                for (TerasologyGameVersion gameVersion : gameVersionList) {
                    if (gameVersion.isInstalled()) {
                        foundInstalled = true;
                        break;
                    }
                }
                if (!foundInstalled) {
                    continue;
                }
            }

            final JobItem jobItem = new JobItem(job);
            jobBox.getItems().add(jobItem);
            if (launcherSettings.getJob() == job) {
                jobBox.getSelectionModel().select(jobItem);
            }
        }
        updateBuildVersionBox();
    }

    private void updateBuildVersionBox() {
        buildVersionBox.getItems().clear();

        final JobItem jobItem = jobBox.getSelectionModel().getSelectedItem();
        final int buildVersion = launcherSettings.getBuildVersion(jobItem.getJob());

        for (TerasologyGameVersion version : gameVersions.getGameVersionList(jobItem.getJob())) {
            final VersionItem versionItem = new VersionItem(version);
            buildVersionBox.getItems().add(versionItem);
            if (versionItem.getVersion() == buildVersion) {
                buildVersionBox.getSelectionModel().select(versionItem);
            }
        }
    }
}
