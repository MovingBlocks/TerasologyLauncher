/*
 * Copyright 2019 MovingBlocks
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

import javafx.animation.Transition;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameStarter;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.packages.Package;
import org.terasology.launcher.packages.PackageManager;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.ProgressListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private static final long MB = 1024L * 1024;
    private static final long MINIMUM_FREE_SPACE = 200 * MB;

    private static final class DownloadTask extends Task<Void> implements ProgressListener {
        private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);

        private final PackageManager packageManager;
        private final Package target;
        private Runnable cleanup;

        DownloadTask(PackageManager packageManager, Package target) {
            this.packageManager = packageManager;
            this.target = target;
        }

        @Override
        protected Void call() {
            try {
                packageManager.install(target, this);
            } catch (IOException | DownloadException e) {
                logger.error("Failed to download package: {}-{}", target.getName(), target.getVersion(), e);
            }
            return null;
        }

        @Override
        public void update() {
        }

        @Override
        public void update(int progress) {
            updateProgress(progress, 100);
        }

        @Override
        protected void done() {
            if (cleanup != null) {
                Platform.runLater(cleanup);
            }
        }

        public void onDone(Runnable cleanupCallback) {
            cleanup = cleanupCallback;
        }
    }

    private static final class DeleteTask extends Task<Void> {
        private static final Logger logger = LoggerFactory.getLogger(DeleteTask.class);

        private final PackageManager packageManager;
        private final Package target;
        private Runnable cleanup;

        DeleteTask(PackageManager packageManager, Package target) {
            this.packageManager = packageManager;
            this.target = target;
        }

        @Override
        protected Void call() {
            try {
                packageManager.remove(target);
            } catch (IOException e) {
                logger.error("Failed to remove package: {}-{}", target.getName(), target.getVersion(), e);
            }
            return null;
        }

        @Override
        protected void done() {
            if (cleanup != null) {
                Platform.runLater(cleanup);
            }
        }

        public void onDone(Runnable cleanupCallback) {
            cleanup = cleanupCallback;
        }
    }

    private Path launcherDirectory;
    private Path downloadDirectory;
    private Path tempDirectory;
    private BaseLauncherSettings launcherSettings;
    private PackageManager packageManager;
    private GameStarter gameStarter;
    private GameDownloadWorker gameDownloadWorker;
    private Stage stage;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private DownloadTask downloadTask;

    @FXML
    private ComboBox<PackageItem> jobBox;
    @FXML
    private ComboBox<String> buildVersionBox;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button cancelDownloadButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button startAndDownloadButton;
    @FXML
    private ImageView playImage;
    @FXML
    private ImageView downloadImage;

    @FXML
    private AboutViewController aboutViewController;
    @FXML
    private LogViewController logViewController;
    @FXML
    private ChangelogViewController changelogViewController;
    @FXML
    private FooterController footerController;

    /**
     * Indicate whether the user's hard drive is running out of space for game downloads.
     */
    final private Property<Optional<Warning>> warning;

    public ApplicationController() {
        warning = new SimpleObjectProperty(Optional.empty());
    }

    @FXML
    public void initialize() {
        footerController.bind(warning);
    }

    @FXML
    protected void handleExitButtonAction() {
        close();
    }

    @FXML
    protected void handleControlButtonMouseEntered(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = FXUtils.createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void handleControlButtonMouseExited(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = FXUtils.createScaleTransition(1, source);
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
                root = fxmlLoader.load();
            } catch (IOException e) {
                fxmlLoader = BundleUtils.getFXMLLoader("settings");
                fxmlLoader.setResources(ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle", Languages.DEFAULT_LOCALE));
                root = fxmlLoader.load();
            }

            final SettingsController settingsController = fxmlLoader.getController();
            settingsController.initialize(launcherDirectory, downloadDirectory, launcherSettings, packageManager, settingsStage, this);

            Scene scene = new Scene(root);
            settingsStage.setScene(scene);
            settingsStage.showAndWait();
        } catch (IOException e) {
            logger.warn("Exception in openSettingsAction: ", e);
        }
    }

    private void startGameAction() {
        final Path gamePath = packageManager.resolveInstallDir(selectedPackage);

        if (gameStarter.isRunning()) {
            logger.debug("The game can not be started because another game is already running.");
            GuiUtils.showInfoMessageDialog(stage, BundleUtils.getLabel("message_information_gameRunning"));
        } else {
            final boolean gameStarted = gameStarter.startGame(selectedPackage, gamePath, launcherSettings.getGameDataDirectory(), launcherSettings.getMaxHeapSize(),
                    launcherSettings.getInitialHeapSize(), launcherSettings.getUserJavaParameterList(),
                    launcherSettings.getUserGameParameterList(), launcherSettings.getLogLevel());
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

    private void downloadAction() {
        downloadTask = new DownloadTask(packageManager, selectedPackage);

        jobBox.setDisable(true);
        buildVersionBox.setDisable(true);
        progressBar.progressProperty().bind(downloadTask.progressProperty());
        progressBar.setVisible(true);
        startAndDownloadButton.setVisible(false);
        cancelDownloadButton.setVisible(true);

        downloadTask.onDone(() -> {
            jobBox.setDisable(false);
            buildVersionBox.setDisable(false);
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            startAndDownloadButton.setVisible(true);
            cancelDownloadButton.setVisible(false);

            if (selectedPackage.isInstalled()) {
                startAndDownloadButton.setGraphic(playImage);
                deleteButton.setDisable(false);
            }
            downloadTask = null;
        });

        executor.submit(downloadTask);
    }

    @FXML
    protected void startAndDownloadAction() {
        if (!selectedPackage.isInstalled()) {
            logger.info("Download Game Action!");
            downloadAction();
        } else {
            logger.info("Start Game Action!");
            startGameAction();
        }
    }

    @FXML
    protected void cancelDownloadAction() {
        // Cancel download
        logger.info("Cancel game download!");
        downloadTask.cancel(false);
    }

    @FXML
    protected void deleteAction() {
        final Path gameDir = packageManager.resolveInstallDir(selectedPackage);
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(BundleUtils.getMessage("confirmDeleteGame_withoutData", gameDir));
        alert.setTitle(BundleUtils.getLabel("message_deleteGame_title"));
        alert.initOwner(stage);

        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    logger.info("Removing game: {}-{}", selectedPackage.getName(), selectedPackage.getVersion());

                    deleteButton.setDisable(true);
                    final DeleteTask deleteTask = new DeleteTask(packageManager, selectedPackage);
                    deleteTask.onDone(() -> {
                        if (!selectedPackage.isInstalled()) {
                            startAndDownloadButton.setGraphic(downloadImage);
                        } else {
                            deleteButton.setDisable(false);
                        }
                    });

                    executor.submit(deleteTask);
                });
    }

    public void initialize(final Path newLauncherDirectory, final Path newDownloadDirectory, final Path newTempDirectory, final BaseLauncherSettings newLauncherSettings,
                           final PackageManager newPackageManager, final Stage newStage, final HostServices hostServices) {
        this.launcherDirectory = newLauncherDirectory;
        this.downloadDirectory = newDownloadDirectory;
        this.tempDirectory = newTempDirectory;
        this.launcherSettings = newLauncherSettings;
        this.packageManager = newPackageManager;
        this.stage = newStage;

        // add Logback view appender view to both the root logger and the tab
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (rootLogger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) rootLogger;

            logViewController.setContext(logbackLogger.getLoggerContext());
            logViewController.start(); // CHECK: do I really need to start it manually here?
            logbackLogger.addAppender(logViewController);
        }

        gameStarter = new GameStarter();

        packageItems = FXCollections.observableArrayList();
        onSync();

        initComboBoxes();
        initButtons();

        //TODO: This only updates when the launcher is initialized (which should happen excatly once o.O)
        //      We should update this value at least every time the download directory changes (user setting).
        //      Ideally, we would check periodically for disk space.
        if (downloadDirectory.toFile().getUsableSpace() <= MINIMUM_FREE_SPACE) {
            warning.setValue(Optional.of(Warning.LOW_ON_SPACE));
        } else {
            warning.setValue(Optional.empty());
        }

        footerController.setHostServices(hostServices);
    }

    private Package selectedPackage;
    private List<Package> packages;
    private ObservableList<PackageItem> packageItems;

    static class PackageItem {
        private final String name;
        private final ObservableList<String> versionList;

        PackageItem(String name) {
            this.name = name;
            versionList = FXCollections.observableArrayList();
        }

        @Override
        public String toString() {
            return name;
        }

        ObservableList<String> getVersionList() {
            return versionList;
        }
    }

    // To be called after database sync is done
    private void onSync() {
        packageItems.clear();
        packages = packageManager.getPackages();

        String currentPkgName = null;
        PackageItem currentItem = null;
        for (Package pkg : packages) {
            if (pkg.getName().equals(currentPkgName)) {
                currentItem.versionList.add(pkg.getVersion());
            } else {
                currentPkgName = pkg.getName();
                currentItem = new PackageItem(currentPkgName);
                currentItem.versionList.add(pkg.getVersion());
                packageItems.add(currentItem);
            }
        }
    }

    private void initComboBoxes() {
        jobBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            buildVersionBox.setItems(newVal.versionList);
            buildVersionBox.getSelectionModel().select(0);
        });

        buildVersionBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            final String pkgName = jobBox.getSelectionModel().getSelectedItem().name;
            final String pkgVer = newVal;

            selectedPackage = packages.stream()
                    .filter(pkg -> pkg.getName().equals(pkgName)
                            && pkg.getVersion().equals(pkgVer))
                    .findAny()
                    .orElse(null);

            if (selectedPackage != null && selectedPackage.isInstalled()) {
                startAndDownloadButton.setGraphic(playImage);
                deleteButton.setDisable(false);
            } else {
                startAndDownloadButton.setGraphic(downloadImage);
                deleteButton.setDisable(true);
            }
        });

        jobBox.setItems(packageItems);
        jobBox.getSelectionModel().select(0);
    }

    private void initButtons() {
        cancelDownloadButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_cancelDownload")));
        cancelDownloadButton.managedProperty().bind(cancelDownloadButton.visibleProperty());
        cancelDownloadButton.setVisible(false);

        startAndDownloadButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_download")));
        startAndDownloadButton.managedProperty().bind(startAndDownloadButton.visibleProperty());
        startAndDownloadButton.setGraphic(downloadImage);

        deleteButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_delete")));
        settingsButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_settings")));
        exitButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_exit")));
    }

    private void updateTooltipTexts() {
        cancelDownloadButton.getTooltip().setText(BundleUtils.getLabel("launcher_cancelDownload"));
        deleteButton.getTooltip().setText(BundleUtils.getLabel("launcher_delete"));
        settingsButton.getTooltip().setText(BundleUtils.getLabel("launcher_settings"));
        exitButton.getTooltip().setText(BundleUtils.getLabel("launcher_exit"));

        if (startAndDownloadButton.getGraphic() == downloadImage) {
            startAndDownloadButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_download")));
        } else {
            startAndDownloadButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_start")));
        }
    }

    private void populateJobBox() {
//        jobBox.getItems().clear();
//
//        for (GameJob job : GameJob.values()) {
//            if (job.isOnlyInstalled() && (launcherSettings.getJob() != job)) {
//                boolean foundInstalled = false;
//                final List<TerasologyGameVersion> gameVersionList = packageManager.getGameVersionList(job);
//                for (TerasologyGameVersion gameVersion : gameVersionList) {
//                    if (gameVersion.isInstalled()) {
//                        foundInstalled = true;
//                        break;
//                    }
//                }
//                if (!foundInstalled) {
//                    continue;
//                }
//            }
//
//            final JobItem jobItem = new JobItem(job);
//            jobBox.getItems().add(jobItem);
//            if (launcherSettings.getJob() == job) {
//                jobBox.getSelectionModel().select(jobItem);
//            }
//        }
//
//        updateBuildVersionBox();
//
//        // add change listeners
//        jobBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<JobItem>() {
//            @Override
//            public void changed(final ObservableValue<? extends JobItem> observableValue, final JobItem oldItem, final JobItem newItem) {
//                if (jobBox.getItems().isEmpty()) {
//                    return;
//                }
//                launcherSettings.setJob(newItem.getJob());
//                updateBuildVersionBox();
//                updateGui();
//                logger.debug("Selected gamejob: {} -- {}", launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
//            }
//        });
//
//        buildVersionBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<VersionItem>() {
//            @Override
//            public void changed(final ObservableValue<? extends VersionItem> observableValue, final VersionItem oldVersionItem, final VersionItem newVersionItem) {
//                if (newVersionItem != null) {
//                    final Integer version = newVersionItem.getVersion();
//                    launcherSettings.setBuildVersion(version, launcherSettings.getJob());
//                    logger.debug("Selected gamejob: {} -- {}", launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
//                    updateGui();
//                }
//            }
//        });
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

        // TODO: Improve close request handling
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
        executor.shutdownNow();

        logger.debug("Closing the launcher ...");
        stage.close();
    }

    private void updateGui() {
        updateButtons();
        updateTooltipTexts();
    }

    private void updateButtons() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        if (gameVersion == null) {
            deleteButton.setDisable(true);
            startAndDownloadButton.setGraphic(downloadImage);
            startAndDownloadButton.setDisable(true);
        } else if (gameVersion.isInstalled()) {
            deleteButton.setDisable(false);
            startAndDownloadButton.setGraphic(playImage);
            startAndDownloadButton.setDisable(false);
        } else if ((gameVersion.getSuccessful() != null) && gameVersion.getSuccessful() && (gameVersion.getBuildNumber() != null) && (gameDownloadWorker == null)) {
            deleteButton.setDisable(true);
            startAndDownloadButton.setGraphic(downloadImage);
            startAndDownloadButton.setDisable(false);
        } else {
            deleteButton.setDisable(true);
            startAndDownloadButton.setGraphic(downloadImage);
            startAndDownloadButton.setDisable(true);
        }

        // Cancel download
        if (gameDownloadWorker != null) {
            cancelDownloadButton.setVisible(true);
            startAndDownloadButton.setVisible(false);
        } else {
            cancelDownloadButton.setVisible(false);
            startAndDownloadButton.setVisible(true);
        }
    }

    private void updateJobBox() {
//        jobBox.getItems().clear();
//        for (GameJob job : GameJob.values()) {
//            if (job.isOnlyInstalled() && (launcherSettings.getJob() != job)) {
//                boolean foundInstalled = false;
//                final List<TerasologyGameVersion> gameVersionList = packageManager.getGameVersionList(job);
//                for (TerasologyGameVersion gameVersion : gameVersionList) {
//                    if (gameVersion.isInstalled()) {
//                        foundInstalled = true;
//                        break;
//                    }
//                }
//                if (!foundInstalled) {
//                    continue;
//                }
//            }
//
//            final JobItem jobItem = new JobItem(job);
//            jobBox.getItems().add(jobItem);
//            if (launcherSettings.getJob() == job) {
//                jobBox.getSelectionModel().select(jobItem);
//            }
//        }
//
//        updateBuildVersionBox();
    }

    private void updateBuildVersionBox() {
//        buildVersionBox.getItems().clear();
//
//        final JobItem jobItem = jobBox.getSelectionModel().getSelectedItem();
//        final int buildVersion = launcherSettings.getBuildVersion(jobItem.getJob());
//
//        for (TerasologyGameVersion version : packageManager.getGameVersionList(jobItem.getJob())) {
//            final VersionItem versionItem = new VersionItem(version);
//            buildVersionBox.getItems().add(versionItem);
//            if (versionItem.getVersion() == buildVersion) {
//                buildVersionBox.getSelectionModel().select(versionItem);
//            }
//        }
    }

    void finishedGameDownload(boolean cancelled, boolean successfulDownloadAndExtract, boolean successfulLoadVersion, Path gameDirectory) {
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
        updateBuildVersionBox();
    }

    private TerasologyGameVersion getSelectedGameVersion() {
//        return packageManager.getGameVersionForBuildVersion(launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
        return null;
    }

    ComboBox<PackageItem> getJobBox() {
        return jobBox;
    }

    ComboBox<String> getBuildVersionBox() {
        return buildVersionBox;
    }
}
