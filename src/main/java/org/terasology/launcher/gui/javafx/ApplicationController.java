// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.gui.javafx;

import com.google.common.collect.Lists;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameManager;
import org.terasology.launcher.game.GameService;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;
import org.terasology.launcher.repositories.RepositoryManager;
import org.terasology.launcher.settings.LauncherSettings;
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.tasks.DeleteTask;
import org.terasology.launcher.tasks.DownloadTask;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.HostServices;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private static final long MB = 1024L * 1024;
    private static final long MINIMUM_FREE_SPACE = 200 * MB;

    private Path launcherDirectory;
    private LauncherSettings launcherSettings;

    private GameManager gameManager;
    private RepositoryManager repositoryManager;
    private final GameService gameService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DownloadTask downloadTask;

    private Stage stage;

    private Property<GameRelease> selectedRelease;
    private Property<GameAction> gameAction;
    private BooleanProperty downloading;

    private ObservableList<GameRelease> availableGameReleases;
    private ObservableSet<GameIdentifier> installedGames;

    /**
     * Indicate whether the user's hard drive is running out of space for game downloads.
     */
    private final Property<Optional<Warning>> warning;

    @FXML
    private ComboBox<Profile> profileComboBox;
    @FXML
    private ComboBox<GameRelease> gameReleaseComboBox;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button startButton;
    @FXML
    private Button downloadButton;
    @FXML
    private Button cancelDownloadButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button exitButton;

    @FXML
    private LogViewController logViewController;
    @FXML
    private ChangelogViewController changelogViewController;
    @FXML
    private FooterController footerController;

    public ApplicationController() {
        warning = new SimpleObjectProperty<>(Optional.empty());
        gameService = new GameService();
        gameService.setOnFailed(this::handleRunFailed);
        gameService.valueProperty().addListener(this::handleRunStarted);

        downloading = new SimpleBooleanProperty(false);

        selectedRelease = new SimpleObjectProperty<>();

        availableGameReleases = FXCollections.observableArrayList();
        installedGames = FXCollections.observableSet();

        gameAction = new SimpleObjectProperty<>(GameAction.DOWNLOAD);
        gameAction.bind(Bindings.createObjectBinding(() -> {
            final GameRelease release = selectedRelease.getValue();
            final boolean isInstalled = release != null && installedGames.contains(release.getId());
            if (downloading.get()) {
                return GameAction.CANCEL;
            } else if (isInstalled) {
                return GameAction.PLAY;
            } else {
                return GameAction.DOWNLOAD;
            }
        }, selectedRelease, installedGames, downloading));
    }

    @FXML
    public void initialize() {
        footerController.bind(warning);
        initComboBoxes();
        initButtons();
    }

    private void initComboBoxes() {
        profileComboBox.setCellFactory(list -> new GameProfileCell());
        profileComboBox.setButtonCell(new GameProfileCell());
        profileComboBox.setItems(FXCollections.observableList(Arrays.asList(Profile.values().clone())));
        profileComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            List<GameRelease> releasesForBuildProfile =
                    repositoryManager.getReleases().stream()
                            .filter(release -> release.getId().getProfile() == newVal)
                            .collect(Collectors.toList());
            availableGameReleases.setAll(releasesForBuildProfile.stream().sorted((o1, o2) -> {
                int compareProfile = o1.getId().getProfile().compareTo(o2.getId().getProfile());
                if (compareProfile != 0) {
                    return compareProfile;
                }
                return o2.getTimestamp().compareTo(o1.getTimestamp());
            }).collect(Collectors.toList()));

            //TODO: select last played game
        });

        gameReleaseComboBox.setItems(availableGameReleases);
        gameReleaseComboBox.buttonCellProperty().bind(Bindings.createObjectBinding(() -> new GameReleaseCell(installedGames, true), installedGames));
        gameReleaseComboBox.cellFactoryProperty().bind(Bindings.createObjectBinding(() -> list -> new GameReleaseCell(installedGames), installedGames));

        selectedRelease.bind(gameReleaseComboBox.getSelectionModel().selectedItemProperty());
        selectedRelease.addListener(
                (observable, oldValue, newValue) -> changelogViewController.update(newValue != null ? newValue.getChangelog() : Collections.emptyList()));
    }

    private void initButtons() {
        cancelDownloadButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_cancelDownload")));
        cancelDownloadButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> gameAction.getValue() == GameAction.CANCEL, gameAction));
        cancelDownloadButton.managedProperty().bind(cancelDownloadButton.visibleProperty());

        startButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_start")));
        startButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> gameAction.getValue() == GameAction.PLAY, gameAction));
        startButton.managedProperty().bind(startButton.visibleProperty());

        downloadButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_download")));
        downloadButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> gameAction.getValue() == GameAction.DOWNLOAD, gameAction));
        downloadButton.managedProperty().bind(downloadButton.visibleProperty());

        deleteButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_delete")));
        deleteButton.disableProperty().bind(startButton.visibleProperty().not());

        settingsButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_settings")));
        exitButton.setTooltip(new Tooltip(BundleUtils.getLabel("launcher_exit")));
    }

    @SuppressWarnings("checkstyle:HiddenField")
    public void update(final Path launcherDirectory, final Path downloadDirectory, final LauncherSettings launcherSettings,
                       final RepositoryManager repositoryManager, final GameManager gameManager,
                       final Stage stage, final HostServices hostServices) {
        this.launcherDirectory = launcherDirectory;
        this.launcherSettings = launcherSettings;

        this.repositoryManager = repositoryManager;
        this.gameManager = gameManager;

        this.stage = stage;

        installedGames = gameManager.getInstalledGames();

        // add Logback view appender view to both the root logger and the tab
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (rootLogger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) rootLogger;

            logViewController.setContext(logbackLogger.getLoggerContext());
            logViewController.start(); // CHECK: do I really need to start it manually here?
            logbackLogger.addAppender(logViewController);
        }

        //TODO: This only updates when the launcher is initialized (which should happen exactly once o.O)
        //      We should update this value at least every time the download directory changes (user setting).
        //      Ideally, we would check periodically for disk space.
        if (downloadDirectory.toFile().getUsableSpace() <= MINIMUM_FREE_SPACE) {
            warning.setValue(Optional.of(Warning.LOW_ON_SPACE));
        } else {
            warning.setValue(Optional.empty());
        }

        initializeComboBoxSelection();

        footerController.setHostServices(hostServices);
    }

    @FXML
    protected void handleExitButtonAction() {
        close();
    }

    @FXML
    protected void handleControlButtonMouseEntered(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = Effects.createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void handleControlButtonMouseExited(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = Effects.createScaleTransition(1, source);
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
            settingsController.initialize(launcherDirectory, launcherSettings, settingsStage, this);

            Scene scene = new Scene(root);
            settingsStage.setScene(scene);
            settingsStage.showAndWait();
        } catch (IOException e) {
            logger.warn("Exception in openSettingsAction: ", e);
        }
    }

    /**
     * The PR upgrading the engine to LWJGL v3 (https://github.com/MovingBlocks/Terasology/pull/3969) was merged on
     * Oct 24, 2020, 20:12 UTC as commit 'c83655fb94c02d68fb8ba31fdb1954e81dde12d6'.
     * <p>
     * This method does some reverse engineering of which build on which Jenkins job contains this change. This should
     * probably be baked into the {@link Package} while fetching the info from the remote source. With upcoming
     * refactoring I'd like to keep this separate here for now...
     *
     * @param release
     * @return
     */
    private boolean isLwjgl3(final GameIdentifier release) {
        if (release.getProfile().equals(Profile.OMEGA) && release.getBuild().equals(Build.STABLE)) {
            return Integer.parseInt(release.getVersion()) > 37;
        }
        if (release.getProfile().equals(Profile.OMEGA) && release.getBuild().equals(Build.NIGHTLY)) {
            return Integer.parseInt(release.getVersion()) > 1103;
        }
        if (release.getProfile().equals(Profile.ENGINE) && release.getBuild().equals(Build.STABLE)) {
            return Integer.parseInt(release.getVersion()) > 82;
        }
        if (release.getProfile().equals(Profile.ENGINE) && release.getBuild().equals(Build.NIGHTLY)) {
            return Integer.parseInt(release.getVersion()) > 2317;
        }
        return false;
    }

    @FXML
    protected void startGameAction() {
        if (gameService.isRunning()) {
            logger.debug("The game can not be started because another game is already running.");
            Dialogs.showInfo(stage, BundleUtils.getLabel("message_information_gameRunning"));
        } else {
            final GameIdentifier release = selectedRelease.getValue().getId();
            final Path gamePath = gameManager.getInstallDirectory(release);
            List<String> additionalJavaParameters = Lists.newArrayList();
            List<String> additionalGameParameters = Lists.newArrayList();
            if (isLwjgl3(release) && Platform.getPlatform().isMac()) {
                additionalJavaParameters.add("-XstartOnFirstThread");
                additionalJavaParameters.add("-Djava.awt.headless=true");

                additionalGameParameters.add("-noSplash");
            }
            gameService.start(gamePath, launcherSettings, additionalJavaParameters, additionalGameParameters);
        }
    }

    private void handleRunStarted(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
        if (newValue == null || !newValue) {
            return;
        }

        logger.debug("Game has started successfully.");

        //TODO: launcherSettings.setLastPlayedGameJob(selectedPackage.getId());
        //TODO: launcherSettings.setLastPlayedGameVersion(selectedPackage.getVersion());

        if (launcherSettings.isCloseLauncherAfterGameStart()) {
            if (downloadTask == null) {
                logger.info("Close launcher after game start.");
                close();
            } else {
                logger.info("The launcher can not be closed after game start, because a download is running.");
            }
        }
    }

    void handleRunFailed(WorkerStateEvent event) {
        TabPane tabPane = (TabPane) stage.getScene().lookup("#contentTabPane");
        if (tabPane != null) {
            var tab = tabPane.lookup("#logTab");
            tabPane.getSelectionModel().select((Tab) tab.getProperties().get(Tab.class));
        } else {
            // We're already in error-handling mode here, so avoid bailing with verifyNotNull
            logger.warn("Failed to locate tab pane.");
        }

        Dialogs.showError(stage, BundleUtils.getLabel("message_error_gameStart"));
    }

    @FXML
    protected void downloadAction() {
        downloadTask = new DownloadTask(gameManager, selectedRelease.getValue());
        downloading.bind(downloadTask.runningProperty());

        profileComboBox.disableProperty().bind(downloadTask.runningProperty());
        gameReleaseComboBox.disableProperty().bind(downloadTask.runningProperty());
        progressBar.visibleProperty().bind(downloadTask.runningProperty());

        progressBar.progressProperty().bind(downloadTask.progressProperty());

        downloadTask.setOnSucceeded(workerStateEvent -> {
            //TODO: launcherSettings.setLastInstalledGameJob(selectedPackage.getId());
            //TODO: launcherSettings.setLastInstalledGameVersion(selectedPackage.getVersion());
            downloadTask = null;
        });

        executor.submit(downloadTask);
    }

    @FXML
    protected void cancelDownloadAction() {
        logger.info("Cancel game download!");
        downloadTask.cancel(false);
    }

    @FXML
    protected void deleteAction() {
        final GameIdentifier id = selectedRelease.getValue().getId();
        final Path gameDir = gameManager.getInstallDirectory(id);

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(BundleUtils.getMessage("confirmDeleteGame_withoutData", gameDir));
        alert.setTitle(BundleUtils.getLabel("message_deleteGame_title"));
        alert.initOwner(stage);

        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    logger.info("Removing game '{}' from path '{}", id, gameDir);
                    // triggering a game deletion implies the player doesn't want to play this game anymore
                    // hence, we unset `lastPlayedGameJob` and `lastPlayedGameVersion` settings independent of deletion success
                    //TODO: launcherSettings.setLastPlayedGameJob("");
                    //TODO: launcherSettings.setLastPlayedGameVersion("");
                    final DeleteTask deleteTask = new DeleteTask(gameManager, id);
                    executor.submit(deleteTask);
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
//    private void selectItemForJob(final String jobId) {
//        selectItem(buildProfileComboBox, profile ->
//                profile.getVersionItems().stream()
//                        .anyMatch(vItem -> vItem.getLinkedPackage().getId().equals(jobId)));
//    }

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
//        String lastPlayedGameJob = launcherSettings.getLastPlayedGameJob();
//        if (!lastPlayedGameJob.isEmpty()) {
//            // select the package last played
//            selectItemForJob(launcherSettings.getLastPlayedGameJob());
//            selectItem(gameReleaseComboBox, item ->
//                    item.getVersion().equals(launcherSettings.getLastPlayedGameVersion()));
//        } else {
//        String lastInstalledGameJob = launcherSettings.getLastInstalledGameJob();
//        if (!lastInstalledGameJob.isEmpty()) {
//            // select last installed package job and version
//            selectItemForJob(lastInstalledGameJob);
//            selectItem(gameReleaseComboBox, item ->
//                    item.getVersion().equals(launcherSettings.getLastInstalledGameVersion()));
//        } else {
        // select last installed package for the default job or the latest one if none installed
//        String defaultGameJob = launcherSettings.getDefaultGameJob();
//        String lastInstalledVersion = packageManager.getLatestInstalledPackageForId(defaultGameJob)
//                .map(Package::getVersion).orElse("");
        selectItem(profileComboBox, profile -> true);
        selectItem(gameReleaseComboBox, release -> true);
//        }
    }

    /**
     * Closes the launcher frame this Controller handles. The launcher frame Stage is determined by the enclosing anchor pane.
     */
    private void close() {
        logger.debug("Dispose launcher frame...");
        final Path settingsFile = launcherDirectory.resolve(Settings.DEFAULT_FILE_NAME);
        try {
            Settings.store(launcherSettings, settingsFile);
        } catch (IOException e) {
            logger.warn("Could not store current launcher settings!");
        }

        // TODO: Improve close request handling
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
        executor.shutdownNow();

        logger.debug("Closing the launcher ...");
        stage.close();
    }

    private enum GameAction {
        PLAY,
        DOWNLOAD,
        CANCEL
    }

    /**
     * Custom {@link ListCell} used to display a {@link GameRelease} along with its installation status.
     */
    private static final class GameReleaseCell extends ListCell<GameRelease> {
        private static final Image ICON_CHECK = BundleUtils.getFxImage("icon_check");
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

        private final ImageView iconStatus;

        private final Set<GameIdentifier> installedGames;
        private final boolean isButtonCell;

        GameReleaseCell(Set<GameIdentifier> installedGames) {
            this(installedGames, false);
        }

        GameReleaseCell(Set<GameIdentifier> installedGames, boolean isButtonCell) {
            this.installedGames = installedGames;
            this.isButtonCell = isButtonCell;
            iconStatus = new ImageView(ICON_CHECK);
        }

        @Override
        protected void updateItem(GameRelease item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                final GameIdentifier id = item.getId();

                String displayVersion;
                if (id.getBuild().equals(Build.NIGHTLY)) {
                    setStyle("-fx-font-weight: normal");
                    displayVersion = "preview " + id.getVersion() + " (" + DATE_FORMAT.format(item.getTimestamp()) + ")";
                } else {
                    setStyle("-fx-font-weight: bold");
                    displayVersion = "release " + id.getVersion();
                }

                setText(displayVersion);
                iconStatus.setVisible(installedGames.contains(id));
                // the graphic is not shown on the button cell, so we only set it for list cells
                if (!isButtonCell) {
                    setGraphic(iconStatus);
                }
            }
        }
    }

    private static final class GameProfileCell extends ListCell<Profile> {
        @Override
        protected void updateItem(Profile profile, boolean empty) {
            super.updateItem(profile, empty);
            if (empty) {
                setText(null);
            } else {
                switch (profile) {
                    case OMEGA:
                        setText("Terasology");
                        break;
                    case ENGINE:
                        setText("Terasology Lite (engine-only)");
                        break;
                }
            }
        }
    }
}
