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

package org.terasology.launcher.gui.javafx;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.game.GameDownloader;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.GameStarter;
import org.terasology.launcher.game.JobItem;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.game.VersionItem;
import org.terasology.launcher.log.LogViewAppender;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    private static final String ABOUT = "about";

    private File launcherDirectory;
    private File downloadDirectory;
    private File tempDirectory;
    private BaseLauncherSettings launcherSettings;
    private TerasologyGameVersions gameVersions;
    private GameStarter gameStarter;
    private GameDownloadWorker gameDownloadWorker;
    private Stage stage;
    private HostServices hostServies;

    @FXML
    private ChoiceBox<JobItem> jobBox;
    @FXML
    private ChoiceBox<VersionItem> buildVersionBox;
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
    private Label versionInfo;
    @FXML
    private Accordion aboutInfoAccordion;
    @FXML
    private TableView<ILoggingEvent> loggingView;
    @FXML
    private Button settingsButton;
    @FXML
    private Button exitButton;

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
            logger.info("Current Locale: {}", Languages.getCurrentLocale());
            Stage settingsStage = new Stage(StageStyle.UNDECORATED);
            settingsStage.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader fxmlLoader;
            Parent root;
            /* Fall back to default language if loading the FXML file files with the current locale */
            try {
                fxmlLoader = BundleUtils.getFXMLLoader("settings");
                root = (Parent) fxmlLoader.load();
            } catch (IOException e) {
                fxmlLoader = BundleUtils.getFXMLLoader("settings");
                fxmlLoader.setResources(ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle", Languages.DEFAULT_LOCALE));
                root = (Parent) fxmlLoader.load();
            }

            final SettingsController settingsController = fxmlLoader.getController();
            settingsController.initialize(launcherDirectory, downloadDirectory, launcherSettings, gameVersions, settingsStage);

            Scene scene = new Scene(root);
            settingsStage.setScene(scene);
            settingsStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            updateJobBox();
            updateGui();
        }
    }

    @FXML
    protected void startGameAction() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        if ((gameVersion == null) || !gameVersion.isInstalled()) {
            logger.warn("The selected game version can not be started! '{}'", gameVersion);
            GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameStart"));
        } else if (gameStarter.isRunning()) {
            logger.debug("The game can not be started because another game is already running.");
            GuiUtils.showInfoMessageDialog(stage, BundleUtils.getLabel("message_information_gameRunning"));
        } else {
            final boolean gameStarted = gameStarter.startGame(gameVersion, launcherSettings.getGameDataDirectory(), launcherSettings.getMaxHeapSize(),
                    launcherSettings.getInitialHeapSize(), launcherSettings.getUserJavaParameterList(), launcherSettings.getUserGameParameterList(), launcherSettings.getLogLevel());
            if (!gameStarted) {
                GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameStart"));
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
                GameDownloader gameDownloader = new GameDownloader(downloadDirectory, tempDirectory, launcherSettings.isKeepDownloadedFiles(),
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
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText(msg);
            alert.setTitle(BundleUtils.getLabel("message_deleteGame_title"));
            alert.initOwner(stage);

            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        logger.info("Delete installed game! '{}' '{}'", gameVersion, gameVersion.getInstallationPath());
                        try {
                            FileUtils.delete(gameVersion.getInstallationPath());
                        } catch (IOException e) {
                            logger.error("Could not delete installed game!", e);
                            // TODO: introduce new message label
                            GuiUtils.showWarningMessageDialog(stage, "Could not delete installed game!");
                            return;
                        }
                        gameVersions.removeInstallationInfo(gameVersion);
                        updateGui();
                    });
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

    public void initialize(final File newLauncherDirectory, final File newDownloadDirectory, final File newTempDirectory, final BaseLauncherSettings newLauncherSettings,
                           final TerasologyGameVersions newGameVersions, final Stage newStage, final HostServices hostServices) {
        this.launcherDirectory = newLauncherDirectory;
        this.downloadDirectory = newDownloadDirectory;
        this.tempDirectory = newTempDirectory;
        this.launcherSettings = newLauncherSettings;
        this.gameVersions = newGameVersions;
        this.stage = newStage;
        this.hostServies = hostServices;

        // add Logback view appender view to both the root logger and the tab
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (rootLogger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) rootLogger;

            LogViewAppender viewLogger = new LogViewAppender(loggingView);
            viewLogger.setContext(logbackLogger.getLoggerContext());
            viewLogger.start(); // CHECK: do I really need to start it manually here?
            logbackLogger.addAppender(viewLogger);
        }

        gameStarter = new GameStarter();

        populateJobBox();

        downloadButton.managedProperty().bind(downloadButton.visibleProperty());
        cancelDownloadButton.managedProperty().bind(cancelDownloadButton.visibleProperty());

        downloadButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_download")));
        cancelDownloadButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_cancelDownload")));
        deleteButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_delete")));
        startButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_start")));
        settingsButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_settings")));
        exitButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_exit")));

        updateGui();
    }

    private void updateTooltipTexts() {
        downloadButton.getTooltip().setText(BundleUtils.getLabel("launcher_download"));
        cancelDownloadButton.getTooltip().setText(BundleUtils.getLabel("launcher_cancelDownload"));
        deleteButton.getTooltip().setText(BundleUtils.getLabel("launcher_delete"));
        startButton.getTooltip().setText(BundleUtils.getLabel("launcher_start"));
        settingsButton.getTooltip().setText(BundleUtils.getLabel("launcher_settings"));
        exitButton.getTooltip().setText(BundleUtils.getLabel("launcher_exit"));
    }

    private void populateJobBox() {
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

        // add change listeners
        jobBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<JobItem>() {
            @Override
            public void changed(final ObservableValue<? extends JobItem> observableValue, final JobItem oldItem, final JobItem newItem) {
                if (jobBox.getItems().isEmpty()) {
                    return;
                }
                launcherSettings.setJob(newItem.getJob());
                updateBuildVersionBox();
                updateGui();
                logger.debug("Selected gamejob: {} -- {}", launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
            }
        });

        buildVersionBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<VersionItem>() {
            @Override
            public void changed(final ObservableValue<? extends VersionItem> observableValue, final VersionItem oldVersionItem, final VersionItem newVersionItem) {
                if (newVersionItem != null) {
                    final Integer version = newVersionItem.getVersion();
                    launcherSettings.setBuildVersion(version, launcherSettings.getJob());
                    logger.debug("Selected gamejob: {} -- {}", launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
                    updateGui();
                }
            }
        });
    }

    /**
     * Closes the launcher frame this Controller handles. The launcher frame Stage is determined by the enclosing anchor pane.
     */
    private void close() {
        logger.debug("Dispose launcher frame...");
        try {
            launcherSettings.store();
        } catch (IOException e) {
            logger.warn("Could not store current launcher settings!");
        }
        if (gameDownloadWorker != null) {
            gameDownloadWorker.cancel(false);
        }
        gameStarter.dispose();

        logger.debug("Closing the launcher ...");
        stage.close();
    }

    private void openUri(URI uri) {
        if (uri != null) {
            hostServies.showDocument(uri.toString());
        }
    }

    private void updateGui() {
        updateButtons();
        updateLabels();
        updateTooltipTexts();
        updateChangeLog();
        updateAboutTab();
    }

    private void updateLabels() {
        // set and display version info
        final String launcherVersion = TerasologyLauncherVersionInfo.getInstance().getDisplayVersion();
        if (launcherVersion.isEmpty()) {
            versionInfo.setText(BundleUtils.getLabel("launcher_versionInfo"));
        } else {
            versionInfo.setText(launcherVersion);
        }
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
            downloadButton.setVisible(false);
            cancelDownloadButton.setVisible(true);
        } else {
            downloadButton.setVisible(true);
            cancelDownloadButton.setVisible(false);
        }
    }

    private void updateJobBox() {
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

    private void updateChangeLog() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        final String gameInfoTextHTML;
        if ((gameVersion == null) || (gameVersion.getJob() == null) || (gameVersion.getBuildNumber() == null)) {
            gameInfoTextHTML = "";
        } else {
            gameInfoTextHTML = getGameInfoText(gameVersion);
        }

        changelogView.getEngine().loadContent(gameInfoTextHTML);
        changelogView.setBlendMode(BlendMode.LIGHTEN);
        changelogView.getEngine().setUserStyleSheetLocation(BundleUtils.getFXMLUrl("css_webview").toExternalForm());
    }

    private void updateAboutTab() {
        aboutInfoAccordion.getPanes().clear();

        Collection<URL> files = new ArrayList<>();
        files.add(BundleUtils.getFXMLUrl(ABOUT, "README.md"));
        files.add(BundleUtils.getFXMLUrl(ABOUT, "CHANGELOG.md"));
        files.add(BundleUtils.getFXMLUrl(ABOUT, "CONTRIBUTING.md"));
        files.add(BundleUtils.getFXMLUrl(ABOUT, "LICENSE"));
        Charset cs = Charset.forName("UTF-8");

        for (URL url : files) {
            try {
                int fnameIdx = url.getFile().lastIndexOf('/');
                int extIdx = url.getFile().lastIndexOf('.');
                String fname = url.getFile().substring(fnameIdx + 1);
                String ext = extIdx < 0 ? "" : url.getFile().substring(extIdx + 1).toLowerCase();

                final WebView view = new WebView();
                StringBuilder sb = new StringBuilder();

                if (ext.equals("md") || ext.equals("markdown")) {
                    try (InputStream input = url.openStream()) {
                        sb.append("<body style='padding-left:24px;'>\n");
                        sb.append(Processor.process(input, Configuration.DEFAULT));
                        sb.append("</body>");
                        view.getEngine().loadContent(sb.toString(), "text/html");
                    }
                } else if (ext.equals("htm") || ext.equals("html")) {
                    view.getEngine().load(url.toExternalForm());
                } else {
                    try (Reader isr = new InputStreamReader(url.openStream(), cs);
                         BufferedReader br = new BufferedReader(isr)) {
                        String line = br.readLine();

                        while (line != null) {
                            sb.append(line);
                            sb.append(System.lineSeparator());
                            line = br.readLine();
                        }
                        view.getEngine().loadContent(sb.toString(), "text/plain");
                    }
                }

                view.getStylesheets().add(BundleUtils.getFXMLUrl("css_webview").toExternalForm());
                view.setContextMenuEnabled(false);

                final AnchorPane pane = new AnchorPane();
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setTopAnchor(view, 0.0);
                pane.getChildren().add(view);

                final TitledPane titledPane = new TitledPane(fname, pane);
                titledPane.setAnimated(false);

                aboutInfoAccordion.getPanes().add(titledPane);
            } catch (MalformedURLException e) {
                logger.warn("Could not load info file -- {}", url);
            } catch (IOException e) {
                logger.warn("Failed to parse markdown file {}", url, e);
            }

            if (!aboutInfoAccordion.getPanes().isEmpty()) {
                aboutInfoAccordion.setExpandedPane(aboutInfoAccordion.getPanes().get(0));
            }
        }
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
            b.append("<h1>")
             .append(escapeHtml(infoHeader1))
             .append("</h1>\n");
        }
        if ((infoHeader2 != null) && (infoHeader2.trim().length() > 0)) {
            b.append("<h2>")
             .append(escapeHtml(infoHeader2))
             .append("</h2>\n");
        }
        b.append("<strong>\n")
         .append(BundleUtils.getLabel("infoHeader3"))
         .append("</strong>\n");

        if ((gameVersion.getChangeLog() != null) && !gameVersion.getChangeLog().isEmpty()) {
            b.append("<p>\n")
             .append(BundleUtils.getLabel("infoHeader4"))
             .append("<ul>\n");
            for (String msg : gameVersion.getChangeLog()) {
                b.append("<li>")
                 .append(escapeHtml(msg))
                 .append("</li>\n");
            }
            b.append("</ul>\n")
             .append("</p>\n");
        }

        /* Append changelogs of previous builds. */
        int previousLogs = gameVersion.getJob().isStable() ? 1 : 10;
        b.append("<hr/>");
        for (String msg : gameVersions.getAggregatedChangeLog(gameVersion, previousLogs)) {
            b.append("<li>")
             .append(escapeHtml(msg))
             .append("</li>\n");
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
                GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameDownload_downloadExtract"));
            } else if (!successfulLoadVersion) {
                if (gameDirectory != null) {
                    GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameDownload_loadVersion") + "\n" + gameDirectory);
                } else {
                    GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameDownload_loadVersion"));
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
}
