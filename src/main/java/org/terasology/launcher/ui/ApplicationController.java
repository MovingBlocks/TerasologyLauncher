// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import com.google.common.collect.Sets;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.LauncherConfiguration;
import org.terasology.launcher.game.GameManager;
import org.terasology.launcher.game.GameService;
import org.terasology.launcher.game.GameVersionNotSupportedException;
import org.terasology.launcher.game.GameInstallation;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;
import org.terasology.launcher.model.ReleaseMetadata;
import org.terasology.launcher.repositories.ReleaseRepository;
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.tasks.DeleteTask;
import org.terasology.launcher.tasks.DownloadTask;
import org.terasology.launcher.util.HostServices;
import org.terasology.launcher.util.I18N;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private static final long MB = 1024L * 1024;
    private static final long MINIMUM_FREE_SPACE = 200 * MB;

    private Path launcherDirectory;
    private Settings launcherSettings;

    private GameManager gameManager;

    private final GameService gameService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DownloadTask downloadTask;

    private Stage stage;

    private final Property<LauncherConfiguration> config;

    private final Property<GameRelease> selectedRelease;
    private final Property<GameAction> gameAction;
    private final BooleanProperty downloading;
    private final BooleanProperty showPreReleases;

    private final ObservableSet<GameIdentifier> installedGames;

    /**
     * Indicate whether the user's hard drive is running out of space for game downloads.
     */
    private final Property<Optional<Warning>> warning;

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

    @FXML
    private Tab changelogTab;
    @FXML
    private Tab aboutTab;
    @FXML
    private Tab logTab;

    public ApplicationController() {
        warning = new SimpleObjectProperty<>(Optional.empty());
        gameService = new GameService();
        gameService.setOnFailed(this::handleRunFailed);
        gameService.valueProperty().addListener(this::handleRunStarted);

        config = new SimpleObjectProperty<>();

        downloading = new SimpleBooleanProperty(false);
        showPreReleases = new SimpleBooleanProperty(false);

        selectedRelease = new SimpleObjectProperty<>();

        installedGames = FXCollections.observableSet();

        // defines which button is shown as game action (i.e., play, download, cancel download)
        gameAction = new SimpleObjectProperty<>(GameAction.DOWNLOAD);
        // the game action is derived from the combination of the selected release (`selectedRelease`), the currently
        // installed games (`installedGames`), and whether there is currently a download in progress (`downloading`).
        // the game action is updated automatically (by this binding) whenever any of the dependencies above change.
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
        // this happens after the FXML elements have been initialized, but before managers and other dependencies have
        // been "injected" to this controller
        footerController.bind(warning);
        initComboBoxes();
        initButtons();
        setLabelStrings();

        cancelDownloadButton.setTooltip(I18N.createTooltip(I18N.labelBinding("launcher_cancelDownload")));
        startButton.setTooltip(I18N.createTooltip(I18N.labelBinding("launcher_start")));
        downloadButton.setTooltip(I18N.createTooltip(I18N.labelBinding("launcher_download")));
        deleteButton.setTooltip(I18N.createTooltip(I18N.labelBinding("launcher_delete")));
        settingsButton.setTooltip(I18N.createTooltip(I18N.labelBinding("launcher_settings")));
        exitButton.setTooltip(I18N.createTooltip(I18N.labelBinding("launcher_exit")));

        // add Logback appender to both the root logger and the tab
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
        warning.bind(Bindings.createObjectBinding(() -> {
            logger.info("Checking for remaining disk space ...");
            LauncherConfiguration cfg = config.getValue();
            if (cfg != null && cfg.getDownloadDirectory().toFile().getUsableSpace() <= MINIMUM_FREE_SPACE) {
                return Optional.of(Warning.LOW_ON_SPACE);
            } else {
                return Optional.empty();
            }
        }, config, installedGames));

    }

    /**
     * Initialize the combo boxes for version selection by setting up bindings and properties.
     * <p>
     * This happens after the FXML elements have been initialized, but before managers and other dependencies have been
     * "injected" to this controller.
     * <p>
     * The combo boxes are configured with custom {@link javafx.scene.control.ListCell} implementations to display
     * human-readable representations of game profiles and game releases. We also bind which game releases are visible
     * to the selected profile, and derive the currently selected release from the combo box's selection model.
     */
    private void initComboBoxes() {
        final ObjectBinding<ObservableList<GameRelease>> releases = Bindings.createObjectBinding(() -> {
            LauncherConfiguration cfg = config.getValue();
            if (cfg == null || cfg.getReleaseRepository() == null) {
                return FXCollections.emptyObservableList();
            } else {
                ReleaseRepository repository = config.getValue().getReleaseRepository();
                Set<GameRelease> onlineReleases = Sets.newHashSet(repository.fetchReleases());
                // Create dummy game release objects from locally installed games.
                // We need this in case of running the launcher in "offline" mode
                // and the list of game releases fetched via the repository manager
                // is empty, but there are still games installed locally.
                //
                // However, we only want to add these dummy releases if they are 
                // not listed in the online releases. Thus, filtering out everything
                // with a GameIdentifier that is already part of the releases fetched
                // from online sources.
                //
                //TODO: This is a weird place to create these dummy releases.
                //      Move this code somewhere else, and make sure that we 
                //      have all the necessary information stored locally, like
                //      the timestamp or the changelog.
                Set<GameIdentifier> onlineIds = onlineReleases.stream().map(GameRelease::getId).collect(Collectors.toSet());
                Stream<GameRelease> localGames = installedGames.stream()
                    .filter(id -> !onlineIds.contains(id))
                    .map(id -> new GameRelease(id, null, new ReleaseMetadata("", new Date())));

                Stream<GameRelease> allReleases = Stream.concat(onlineReleases.stream(), localGames);

                List<GameRelease> releasesForProfile =
                        allReleases
                                .filter(release -> release.getId().getProfile() == Profile.OMEGA)
                                .filter(release -> showPreReleases.getValue() || release.getId().getBuild().equals(Build.STABLE))
                                .sorted(ApplicationController::compareReleases)
                                .collect(Collectors.toList());
                
                return FXCollections.observableList(releasesForProfile);
            }
        }, config, showPreReleases, installedGames);

        // derive the releases to display from the selected profile (`selectedProfile`). the resulting list is ordered
        // in the way the launcher is supposed to display the versions (currently by release timestamp).
        final ObjectBinding<GameRelease> releaseToSelect = Bindings.createObjectBinding(()-> {
            GameIdentifier lastPlayedGame = Optional.ofNullable(config.getValue())
                .map(cfg -> cfg.getLauncherSettings().lastPlayedGameVersion.get())
                .orElse(null);

            Optional<GameRelease> lastPlayed = releases.get().stream()
                    .filter(release -> release.getId().equals(lastPlayedGame))
                    .filter(release -> installedGames.contains(release.getId()))
                    .findFirst();
            Optional<GameRelease> lastInstalled = releases.get().stream()
                    .filter(release -> installedGames.contains(release.getId()))
                    .findFirst();

            return lastPlayed
                    .or(() -> lastInstalled)
                    .or(() -> releases.get().stream().findFirst())
                    .orElse(null);
        }, releases, config);

        releaseToSelect.addListener((obs, old, now) -> {
            gameReleaseComboBox.getSelectionModel().select(now);
        }); 

        gameReleaseComboBox.itemsProperty().bind(releases);
        gameReleaseComboBox.buttonCellProperty()
                .bind(Bindings.createObjectBinding(() -> new GameReleaseCell(installedGames, true), installedGames));
        gameReleaseComboBox.cellFactoryProperty()
                .bind(Bindings.createObjectBinding(() -> list -> new GameReleaseCell(installedGames), installedGames));

        selectedRelease.bind(gameReleaseComboBox.getSelectionModel().selectedItemProperty());
        //TODO: instead of imperatively updating the changelog view its value should be bound via property, too
        selectedRelease.addListener(
                (observable, oldValue, newValue) -> changelogViewController.update(newValue != null ? newValue.getChangelog() : ""));
    }

    /**
     * Initialize buttons by setting up their bindings to observable values or properties.
     * <p>
     * This happens after the FXML elements have been initialized, but before managers and other dependencies have been
     * "injected" to this controller.
     * <p>
     * The buttons "Play", "Download", and "Cancel Download" share the space in the UI. We make sure that only one of
     * them is shown at the same time by deriving their visibility from the current {@link GameAction}. As JavaFX will
     * still occupy space for non-visible nodes, we also bind the {@code managedProperty} to the visibility (nodes that
     * are not managed "disappear" from the scene.
     */
    private void initButtons() {
        cancelDownloadButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> gameAction.getValue() == GameAction.CANCEL, gameAction));
        cancelDownloadButton.managedProperty().bind(cancelDownloadButton.visibleProperty());

        startButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> gameAction.getValue() == GameAction.PLAY, gameAction));
        startButton.managedProperty().bind(startButton.visibleProperty());

        downloadButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> gameAction.getValue() == GameAction.DOWNLOAD, gameAction));
        downloadButton.managedProperty().bind(downloadButton.visibleProperty());

        deleteButton.disableProperty().bind(startButton.visibleProperty().not());
    }

    /**
     * Used to assign localized label strings via BundleUtils.
     * Allows for fallback strings to be assigned if the localization-specific ones
     * are absent/empty
     */
    private void setLabelStrings() {
        changelogTab.textProperty().bind(I18N.labelBinding("tab_changelog"));
        aboutTab.textProperty().bind(I18N.labelBinding("tab_about"));
        logTab.textProperty().bind(I18N.labelBinding("tab_log"));
    }

    @SuppressWarnings("checkstyle:HiddenField")
    public void update(final LauncherConfiguration configuration, final Stage stage, final HostServices hostServices) {
        this.config.setValue(configuration);

        this.launcherDirectory = configuration.getLauncherDirectory();
        this.launcherSettings = configuration.getLauncherSettings();
        this.showPreReleases.bind(launcherSettings.showPreReleases);

        this.gameManager = configuration.getGameManager();

        this.stage = stage;

        // bind the application controller's view of the installed games to that of the game manager. that way, we also
        // get notified if the installed games are changed from a different thread (DeleteTask or DownloadTask).
        Bindings.bindContent(installedGames, gameManager.getInstalledGames());

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
            logger.info("Current Locale: {}", I18N.getCurrentLocale());
            Stage settingsStage = new Stage(StageStyle.UNDECORATED);
            settingsStage.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader fxmlLoader;
            Parent root;
            /* Fall back to default language if loading the FXML file files with the current locale */
            try {
                fxmlLoader = I18N.getFXMLLoader("settings");
                root = fxmlLoader.load();
            } catch (IOException e) {
                fxmlLoader = I18N.getFXMLLoader("settings");
                //FIXME whut?
                fxmlLoader.setResources(ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle", I18N.getDefaultLocale()));
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

    @FXML
    protected void startGameAction() {
        if (gameService.isRunning()) {
            logger.debug("The game can not be started because another game is already running.");
            Dialogs.showInfo(stage, I18N.getLabel("message_information_gameRunning"));
            return;
        }
        final GameRelease release = selectedRelease.getValue();
        final GameInstallation gameInstallation;
        try {
            gameInstallation = gameManager.getInstallation(release.getId());
        } catch (FileNotFoundException e) {
            // TODO: Refresh the list of installed games or something? This should not be reachable if
            //     the properties are up to date.
            logger.warn("Failed to get an installation for selection {}", release, e);
            Dialogs.showError(stage, I18N.getMessage("message_error_installationNotFound", release));
            return;
        }
        try {
            gameService.start(gameInstallation, launcherSettings);
        } catch (GameVersionNotSupportedException e) {
            Dialogs.showError(stage, e.getMessage());
        }
    }

    private void handleRunStarted(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
        if (newValue == null || !newValue) {
            return;
        }

        logger.debug("Game has started successfully.");

        launcherSettings.lastPlayedGameVersion.set(selectedRelease.getValue().getId());

        if (launcherSettings.closeLauncherAfterGameStart.get()) {
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

        Dialogs.showError(stage, I18N.getLabel("message_error_gameStart"));
    }

    @FXML
    protected void downloadAction() {
        downloadTask = new DownloadTask(gameManager, selectedRelease.getValue());
        downloading.bind(downloadTask.runningProperty());

        gameReleaseComboBox.disableProperty().bind(downloadTask.runningProperty());
        progressBar.visibleProperty().bind(downloadTask.runningProperty());

        progressBar.progressProperty().bind(downloadTask.progressProperty());

        downloadTask.setOnSucceeded(workerStateEvent -> {
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
        alert.setContentText(I18N.getMessage("confirmDeleteGame_withoutData", gameDir));
        alert.setTitle(I18N.getLabel("message_deleteGame_title"));
        alert.initOwner(stage);

        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    logger.info("Removing game '{}' from path '{}", id, gameDir);
                    // triggering a game deletion implies the player doesn't want to play this game anymore. hence, we
                    // unset `lastPlayedGameVersion` setting independent of deletion success
                    launcherSettings.lastPlayedGameVersion.set(null);
                    final DeleteTask deleteTask = new DeleteTask(gameManager, id);
                    executor.submit(deleteTask);
                });
    }

    /**
     * Closes the launcher frame this Controller handles. The launcher frame Stage is determined by the enclosing anchor pane.
     */
    private void close() {
        logger.debug("Dispose launcher frame...");
        try {
            Settings.store(launcherSettings, launcherDirectory);
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

    private static int compareReleases(GameRelease o1, GameRelease o2) {
        int compareProfile = o1.getId().getProfile().compareTo(o2.getId().getProfile());
        if (compareProfile != 0) {
            return compareProfile;
        }
        return o2.getTimestamp().compareTo(o1.getTimestamp());
    }

    private enum GameAction {
        PLAY,
        DOWNLOAD,
        CANCEL
    }
}
