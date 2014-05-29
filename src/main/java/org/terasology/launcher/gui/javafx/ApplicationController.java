/*
 * Copyright 2014 MovingBlocks
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
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
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
    private ChoiceBox<JobItem> jobBox;
    @FXML
    private ChoiceBox<VersionItem> buildVersionBox;
    @FXML
    private Node launcherFrame;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button downloadButton;
    @FXML
    private Button cancelDownloadButton;
    @FXML
    private Button startButton;
    @FXML
    private Button deleteButton;
    @FXML
    private WebView changelogView;

    @FXML
    protected void handleExitButtonAction() {
        close();
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
    protected void handleSocialButtonMousePressed(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = createScaleTransition(0.8, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMouseReleased(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void openSettingsAction() {
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
            updateBuildVersionBox();
            updateGui();
        }
    }

    @FXML
    protected void startGameAction() {
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
                if (gameDownloadWorker == null) {
                    logger.info("Close launcher after game start.");
                    close();
                } else {
                    logger.info("The launcher can not be closed after game start, because a download is running.");
                }
            }
        }
    }

    @FXML
    protected void downloadAction() {
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
                gameDownloadWorker = new GameDownloadWorker(this, gameDownloader);
            } catch (IOException e) {
                logger.error("Could not start game download!", e);
                finishedGameDownload(false, false, false, null);
                return;
            }
            progressBar.progressProperty().bind(gameDownloadWorker.progressProperty());
            progressBar.setVisible(true);
            new Thread(gameDownloadWorker).start();
        }
        updateGui();
    }

    @FXML
    protected void cancelDownloadAction() {
        // Cancel download
        logger.info("Cancel game download!");
        gameDownloadWorker.cancel(false);

        updateGui();
    }

    @FXML
    protected void deleteAction() {
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

    @FXML
    protected void openFacebook() {
        openUri(BundleUtils.getURI("terasology_facebook"));
    }

    @FXML
    protected void openGithub() {
        openUri(BundleUtils.getURI("terasology_github"));
    }

    @FXML
    protected void openGPlus() {
        openUri(BundleUtils.getURI("terasology_gplus"));
    }

    @FXML
    protected void openReddit() {
        openUri(BundleUtils.getURI("terasology_reddit"));
    }

    @FXML
    protected void openTwitter() {
        openUri(BundleUtils.getURI("terasology_twitter"));
    }

    @FXML
    protected void openYoutube() {
        openUri(BundleUtils.getURI("terasology_youtube"));
    }

    public void initialize(final File newLauncherDirectory, final File newDownloadDirectory, final File newTempDirectory, final LauncherSettings newLauncherSettings,
                           final TerasologyGameVersions newGameVersions) {
        this.launcherDirectory = newLauncherDirectory;
        this.downloadDirectory = newDownloadDirectory;
        this.tempDirectory = newTempDirectory;
        this.launcherSettings = newLauncherSettings;
        this.gameVersions = newGameVersions;

        gameStarter = new GameStarter();

        populateJob();

        // add change listeners
        jobBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<JobItem>() {
            @Override
            public void changed(final ObservableValue<? extends JobItem> observableValue, final JobItem oldItem, final JobItem newItem) {
                updateBuildVersionBox();
                newLauncherSettings.setJob(newItem.getJob());
                logger.debug("Selected gamejob: {} -- {}", newLauncherSettings.getJob(), newLauncherSettings.getBuildVersion(newLauncherSettings.getJob()));
                updateGui();
            }
        });

        buildVersionBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<VersionItem>() {
            @Override
            public void changed(final ObservableValue<? extends VersionItem> observableValue, final VersionItem oldVersionItem, final VersionItem newVersionItem) {
                if (newVersionItem != null) {
                    final Integer version = newVersionItem.getVersion();
                    newLauncherSettings.setBuildVersion(version, newLauncherSettings.getJob());
                    logger.debug("Selected gamejob: {} -- {}", newLauncherSettings.getJob(), newLauncherSettings.getBuildVersion(newLauncherSettings.getJob()));
                    updateGui();
                }
            }
        });

        downloadButton.managedProperty().bind(downloadButton.visibleProperty());
        cancelDownloadButton.managedProperty().bind(cancelDownloadButton.visibleProperty());
        updateGui();
    }

    /**
     * Closes the launcher frame this Controller handles. The launcher frame Stage is determined by the enclosing anchor pane.
     */
    private void close() {
        logger.debug("Dispose launcher frame...");
        if (gameDownloadWorker != null) {
            gameDownloadWorker.cancel(false);
        }
        gameStarter.dispose();

        logger.debug("Closing the launcher ...");
        final Stage stage = (Stage) launcherFrame.getScene().getWindow();
        stage.close();
        //System.exit(0);
    }

    private void openUri(URI uri) {
        if ((uri != null) && Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (IOException | RuntimeException e) {
                    logger.error("Could not browse URI '{}' with desktop!", uri, e);
                }
            }
        }
    }

    private void updateGui() {
        updateButtons();
        updateChangeLog();
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
            // TODO turn download button into cancel button and vice versa!
            // TODO or make them invisible in turn
            //downloadButton.setEnabled(true);
            downloadButton.setVisible(false);
            cancelDownloadButton.setVisible(true);
        } else {
            downloadButton.setVisible(true);
            cancelDownloadButton.setVisible(false);
        }
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

    private void updateChangeLog() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        final String gameInfoTextHTML;
        if ((gameVersion == null) || (gameVersion.getJob() == null) || (gameVersion.getBuildNumber() == null)) {
            gameInfoTextHTML = "";
        } else {
            gameInfoTextHTML = getGameInfoText(gameVersion);
        }
        changelogView.getEngine().loadContent(
            "<html><body style='background-color:#0a0a0a; color:#dfdfdf;'>" + gameInfoTextHTML + "</body></html>"
        );
        changelogView.setBlendMode(BlendMode.LIGHTEN);
    }

    private String getGameInfoText(TerasologyGameVersion gameVersion) {
        logger.debug("Display game version: {} {}", gameVersion, gameVersion.getGameVersionInfo());

        final Object[] arguments = new Object[9];
        arguments[0] = gameVersion.getJob().name();
        if (gameVersion.getJob().isStable()) {
            arguments[1] = 1;
        } else {
            arguments[1] = 0;
        }
        arguments[2] = gameVersion.getJob().getGitBranch();
        arguments[3] = gameVersion.getBuildNumber();
        if (gameVersion.isLatest()) {
            arguments[4] = 1;
        } else {
            arguments[4] = 0;
        }
        if (gameVersion.isInstalled()) {
            arguments[5] = 1;
        } else {
            arguments[5] = 0;
        }
        if (gameVersion.getSuccessful() != null) {
            if (!gameVersion.getSuccessful()) {
                // faulty
                arguments[6] = 0;
            } else {
                arguments[6] = 1;
            }
        } else {
            // unknown
            arguments[6] = 2;
        }
        if ((gameVersion.getGameVersionInfo() != null)
            && (gameVersion.getGameVersionInfo().getDisplayVersion() != null)) {
            arguments[7] = gameVersion.getGameVersionInfo().getDisplayVersion();
        } else {
            arguments[7] = "";
        }
        if ((gameVersion.getGameVersionInfo() != null)
            && (gameVersion.getGameVersionInfo().getDateTime() != null)) {
            arguments[8] = gameVersion.getGameVersionInfo().getDateTime();
        } else {
            arguments[8] = "";
        }

        final String infoHeader1 = BundleUtils.getMessage(gameVersion.getJob().getInfoMessageKey(), arguments);
        final String infoHeader2 = BundleUtils.getMessage("infoHeader2", arguments);

        final StringBuilder b = new StringBuilder();
        if ((infoHeader1 != null) && (infoHeader1.trim().length() > 0)) {
            b.append("<h1>");
            b.append(escapeHtml(infoHeader1));
            b.append("</h1>\n");
        }
        if ((infoHeader2 != null) && (infoHeader2.trim().length() > 0)) {
            b.append("<h2>");
            b.append(escapeHtml(infoHeader2));
            b.append("</h2>\n");
        }
        b.append("<strong>\n");
        b.append(BundleUtils.getLabel("infoHeader3"));
        b.append("</strong>\n");

        if ((gameVersion.getChangeLog() != null) && !gameVersion.getChangeLog().isEmpty()) {
            b.append("<p>\n");
            b.append(BundleUtils.getLabel("infoHeader4"));
            b.append("<ul>\n");
            for (String msg : gameVersion.getChangeLog()) {
                b.append("<li>");
                b.append(escapeHtml(msg));
                b.append("</li>\n");
            }
            b.append("</ul>\n");
            b.append("</p>\n");
        }
        return b.toString();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;").replace("/", "&#x2F;");
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
        updateGui();
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
}
