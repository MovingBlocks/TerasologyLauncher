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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameStarter;
import org.terasology.launcher.packages.Package;
import org.terasology.launcher.packages.PackageManager;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.tasks.DeleteTask;
import org.terasology.launcher.tasks.DownloadTask;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.HostServices;
import org.terasology.launcher.util.Languages;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private static final long MB = 1024L * 1024;
    private static final long MINIMUM_FREE_SPACE = 200 * MB;

    private Path launcherDirectory;
    private BaseLauncherSettings launcherSettings;
    private PackageManager packageManager;
    private GameStarter gameStarter;
    private Stage stage;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DownloadTask downloadTask;

    private VersionItem selectedVersion;
    private Package selectedPackage;
    private ObservableList<PackageItem> packageItems;

    @FXML
    private ComboBox<PackageItem> jobBox;
    @FXML
    private ComboBox<VersionItem> buildVersionBox;
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
    private LogViewController logViewController;
    @FXML
    private ChangelogViewController changelogViewController;
    @FXML
    private FooterController footerController;

    /**
     * Indicate whether the user's hard drive is running out of space for game downloads.
     */
    private final Property<Optional<Warning>> warning;

    public ApplicationController() {
        warning = new SimpleObjectProperty<>(Optional.empty());
    }

    @FXML
    public void initialize() {
        footerController.bind(warning);
        initComboBoxes();
        initButtons();
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
            settingsController.initialize(launcherDirectory, launcherSettings, packageManager, settingsStage, this);

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
            } else {
                launcherSettings.setLastPlayedGameJob(selectedPackage.getId());
                launcherSettings.setLastPlayedGameVersion(selectedPackage.getVersion());

                if (launcherSettings.isCloseLauncherAfterGameStart()) {
                    if (downloadTask == null) {
                        logger.info("Close launcher after game start.");
                        close();
                    } else {
                        logger.info("The launcher can not be closed after game start, because a download is running.");
                    }
                }
            }
        }
    }

    private void downloadAction() {
        downloadTask = new DownloadTask(packageManager, selectedVersion);

        jobBox.disableProperty().bind(downloadTask.runningProperty());
        buildVersionBox.disableProperty().bind(downloadTask.runningProperty());
        progressBar.visibleProperty().bind(downloadTask.runningProperty());
        cancelDownloadButton.visibleProperty().bind(downloadTask.runningProperty());
        startAndDownloadButton.visibleProperty().bind(downloadTask.runningProperty().not());

        progressBar.progressProperty().bind(downloadTask.progressProperty());

        downloadTask.setOnSucceeded(workerStateEvent -> {
            packageManager.syncDatabase();
            startAndDownloadButton.setGraphic(playImage);
            deleteButton.setDisable(false);
            launcherSettings.setLastInstalledGameJob(selectedPackage.getId());
            launcherSettings.setLastInstalledGameVersion(selectedPackage.getVersion());

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
                    logger.info("Removing game: {}-{}", selectedPackage.getId(), selectedPackage.getVersion());
                    // triggering a game deletion implies the player doesn't want to play this game anymore
                    // hence, we unset `lastPlayedGameJob` and `lastPlayedGameVersion` settings independent of deletion success
                    launcherSettings.setLastPlayedGameJob("");
                    launcherSettings.setLastPlayedGameVersion("");

                    deleteButton.setDisable(true);
                    final DeleteTask deleteTask = new DeleteTask(packageManager, selectedVersion);
                    deleteTask.onDone(() -> {
                        packageManager.syncDatabase();
                        if (!selectedPackage.isInstalled()) {
                            startAndDownloadButton.setGraphic(downloadImage);
                        } else {
                            deleteButton.setDisable(false);
                        }
                    });

                    executor.submit(deleteTask);
                });
    }

    public void update(final Path newLauncherDirectory, final Path newDownloadDirectory, final BaseLauncherSettings newLauncherSettings,
                       final PackageManager newPackageManager, final Stage newStage, final HostServices hostServices) {
        this.launcherDirectory = newLauncherDirectory;
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

        //TODO: This only updates when the launcher is initialized (which should happen exactly once o.O)
        //      We should update this value at least every time the download directory changes (user setting).
        //      Ideally, we would check periodically for disk space.
        if (newDownloadDirectory.toFile().getUsableSpace() <= MINIMUM_FREE_SPACE) {
            warning.setValue(Optional.of(Warning.LOW_ON_SPACE));
        } else {
            warning.setValue(Optional.empty());
        }

        footerController.setHostServices(hostServices);

        initializeComboBoxSelection();
    }

    // To be called after database sync is done
    private void onSync() {
        packageItems.clear();
        packageManager.getPackages()
                .stream()
                .collect(Collectors.groupingBy(Package::getName, //TODO this should be grouped by `id`
                        Collectors.mapping(VersionItem::new, Collectors.toList())))
                .forEach((name, versions) ->
                        packageItems.add(new PackageItem(name, versions)));

        jobBox.setItems(packageItems);
        jobBox.getSelectionModel().select(0);
    }

    private void initComboBoxes() {
        jobBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            buildVersionBox.setItems(newVal.getVersionItems());

            String lastPlayedGameJob = launcherSettings.getLastPlayedGameJob();
            String selectedJobId = newVal.getVersionItems().get(0).getLinkedPackage().getId();
            if (lastPlayedGameJob.isEmpty() || !lastPlayedGameJob.equals(selectedJobId)) {
                // select last installed package for the selected job or the latest one if none installed
                String lastInstalledVersion = packageManager.getLatestInstalledPackageForId(selectedJobId)
                        .map(Package::getVersion).orElse("");
                selectItem(buildVersionBox, item ->
                        item.getVersion().equals(lastInstalledVersion));
            } else {
                // select the package last played
                selectItem(buildVersionBox, item ->
                        item.getVersion().equals(launcherSettings.getLastPlayedGameVersion()));
            }
        });

        buildVersionBox.setCellFactory(list -> new VersionListCell());
        buildVersionBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                return;
            }
            selectedVersion = newVal;
            selectedPackage = newVal.getLinkedPackage();

            if (selectedPackage != null && selectedPackage.isInstalled()) {
                startAndDownloadButton.setGraphic(playImage);
                deleteButton.setDisable(false);
            } else {
                startAndDownloadButton.setGraphic(downloadImage);
                deleteButton.setDisable(true);
            }

            changelogViewController.update(selectedPackage.getChangelog());
        });
    }

    /**
     * Select the first item matching given predicate, select the first item otherwise.
     *
     * @param comboBox  the combo box to change the selection for
     * @param predicate first item matching this predicate will be selected
     */
    private <T> void selectItem(final ComboBox<T> comboBox, Predicate<T> predicate) {
        final T item = comboBox.getItems().stream()
                .filter(predicate)
                .findFirst()
                .orElse(comboBox.getItems().get(0));

        comboBox.getSelectionModel().select(item);
    }

    /**
     * Select the package item with given {@code jobId} or the first item of {@code jobBox}.
     *
     * @param jobId the job id of the package to be selected
     */
    private void selectItemForJob(final String jobId) {
        selectItem(jobBox, jobItem ->
                jobItem.getVersionItems().stream()
                        .anyMatch(vItem -> vItem.getLinkedPackage().getId().equals(jobId)));
    }

    /**
     * Initialize selected game job and version based on last played and last installed games.
     * <p>
     * The selection is derived from the following precedence rules:
     * <ol>
     *     <li>Select the <b>last played game</b></li>
     *     <li>Select the <b>last installed game</b></li>
     *     <li>Select <b>latest version of default job</b> otherwise</li>
     * </ol>
     */
    //TODO: Reduce boilerplate code after switching to >= Java 9
    //      Use 'Optional::or' to chain logic together
    private void initializeComboBoxSelection() {
        String lastPlayedGameJob = launcherSettings.getLastPlayedGameJob();
        if (!lastPlayedGameJob.isEmpty()) {
            // select the package last played
            selectItemForJob(launcherSettings.getLastPlayedGameJob());
            selectItem(buildVersionBox, item ->
                    item.getVersion().equals(launcherSettings.getLastPlayedGameVersion()));
        } else {
            String lastInstalledGameJob = launcherSettings.getLastInstalledGameJob();
            if (!lastInstalledGameJob.isEmpty()) {
                // select last installed package job and version
                selectItemForJob(lastInstalledGameJob);
                selectItem(buildVersionBox, item ->
                        item.getVersion().equals(launcherSettings.getLastInstalledGameVersion()));
            } else {
                // select last installed package for the default job or the latest one if none installed
                String defaultGameJob = launcherSettings.getDefaultGameJob();
                selectItemForJob(defaultGameJob);
                String lastInstalledVersion = packageManager.getLatestInstalledPackageForId(defaultGameJob)
                        .map(Package::getVersion).orElse("");
                selectItem(buildVersionBox, item ->
                        item.getVersion().equals(lastInstalledVersion));
            }
        }
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
        gameStarter.dispose();

        // TODO: Improve close request handling
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
        executor.shutdownNow();

        logger.debug("Closing the launcher ...");
        stage.close();
    }

    /**
     * Custom ListCell used to display package versions
     * along with their installation status.
     */
    private static final class VersionListCell extends ListCell<VersionItem> {
        private static final Image ICON_CHECK = BundleUtils.getFxImage("icon_check");
        private static final Insets MARGIN = new Insets(0, 8, 0, 0);
        private final HBox root;
        private final Label labelVersion;
        private final ImageView iconStatus;

        VersionListCell() {
            root = new HBox();
            labelVersion = new Label();
            iconStatus = new ImageView(ICON_CHECK);

            final Pane separator = new Pane();
            HBox.setHgrow(separator, Priority.ALWAYS);
            HBox.setMargin(iconStatus, MARGIN);
            root.getChildren().addAll(labelVersion, separator, iconStatus);
        }

        @Override
        protected void updateItem(VersionItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                labelVersion.textProperty().bind(item.versionProperty());
                iconStatus.visibleProperty().bind(item.installedProperty());
                setGraphic(root);
            }
        }
    }

}
