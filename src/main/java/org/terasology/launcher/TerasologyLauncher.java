// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.LauncherVersion;
import org.terasology.launcher.ui.ApplicationController;
import org.terasology.launcher.util.I18N;
import org.terasology.launcher.util.HostServices;
import org.terasology.launcher.util.LauncherStartFailedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class TerasologyLauncher extends Application {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyLauncher.class);

    private static final int SPLASH_WIDTH = 800;
    private static final int SPLASH_HEIGHT = 223;

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private HostServices hostServices;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        ImageView splash = new ImageView(I18N.getFxImage("splash"));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH);
        progressText = new Label();
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.getStylesheets().add(I18N.getStylesheet("css_splash"));
        splashLayout.setEffect(new DropShadow());
        hostServices = new HostServices();
    }

    @Override
    public void start(final Stage initialStage) {
        logger.info("TerasologyLauncher is starting");
        logSystemInformation();

        initProxy();

        final Task<LauncherConfiguration> launcherInitTask = new LauncherInitTask(initialStage, hostServices);

        showSplashStage(initialStage, launcherInitTask);
        Thread initThread = new Thread(launcherInitTask);
        initThread.setName("Launcher init thread");
        initThread.setUncaughtExceptionHandler((t, e) -> logger.warn("Initialization failed!", e));

        launcherInitTask.setOnSucceeded(workerStateEvent -> {
            try {
                LauncherConfiguration config = launcherInitTask.getValue();
                if (config == null) {
                    throw new LauncherStartFailedException("Launcher configuration was `null`.");
                } else {
                    showMainStage(config);
                }
            } catch (IOException | LauncherStartFailedException e) {
                logger.error("The TerasologyLauncher could not be started!", e);
                System.exit(1);
            }
        });

        launcherInitTask.setOnFailed(event -> {
            logger.error("The TerasologyLauncher could not be started!", event.getSource().getException()); //NOPMD
            System.exit(1);
        });

        launcherInitTask.setOnCancelled(event -> Platform.exit());

        initThread.start();
    }

    /**
     * Sets the system property as advised by Oracle.
     *
     * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/net/proxies.html">Java Networking and Proxies</a>
     */
    private static void initProxy() {
        System.setProperty("java.net.useSystemProxies", "true");
    }

    private void showMainStage(final LauncherConfiguration launcherConfiguration) throws IOException {
        mainStage = new Stage(StageStyle.DECORATED);

        // launcher frame
        FXMLLoader fxmlLoader;
        Parent root;
        /* Fall back to default language if loading the FXML file fails with the current locale */
        try {
            fxmlLoader = I18N.getFXMLLoader("application");
            root = (Parent) fxmlLoader.load();
        } catch (IOException e) {
            fxmlLoader = I18N.getFXMLLoader("application");
            fxmlLoader.setResources(ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle", I18N.getDefaultLocale()));
            root = (Parent) fxmlLoader.load();
        }
        final ApplicationController controller = fxmlLoader.getController();
        controller.update(launcherConfiguration, mainStage, hostServices);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(I18N.getStylesheet("css_terasology"));

        decorateStage(mainStage);

        mainStage.setScene(scene);
        mainStage.setResizable(true);
        mainStage.show();

        logger.info("The TerasologyLauncher was successfully started.");
    }

    private void showSplashStage(final Stage initialStage, final Task<LauncherConfiguration> task) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                if (mainStage != null) {
                    mainStage.setIconified(false);
                }
                initialStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initialStage.hide());
                fadeSplash.play();
            } // todo add code to gracefully handle other task states.
        });

        decorateStage(initialStage);

        Scene splashScene = new Scene(splashLayout);
        initialStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initialStage.setScene(splashScene);
        initialStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initialStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initialStage.show();
    }

    private void logSystemInformation() {
        if (logger.isDebugEnabled()) {
            // Java
            logger.debug("Java: {} {} {}",
                    System.getProperty("java.version"), System.getProperty("java.vendor"), System.getProperty("java.home"));
            logger.debug("Java VM: {} {} {}",
                    System.getProperty("java.vm.name"), System.getProperty("java.vm.vendor"), System.getProperty("java.vm.version"));
            logger.debug("Java classpath: {}",
                    System.getProperty("java.class.path"));

            // OS
            logger.debug("OS: {} {} {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));

            //Memory
            logger.debug("Max. Memory: {} bytes", Runtime.getRuntime().maxMemory());

            // TerasologyLauncherVersionInfo
            logger.debug("Launcher version: {}", LauncherVersion.getInstance());
        }
    }

    /**
     * Adds title and icons to a stage.
     *
     * @param stage the stage to decorate
     */
    private static void decorateStage(Stage stage) {
        stage.setTitle("TerasologyLauncher");
        List<String> iconIds = Arrays.asList("icon16", "icon32", "icon64", "icon128");

        for (String id : iconIds) {
            try {
                Image image = I18N.getFxImage(id);
                stage.getIcons().add(image);
            } catch (MissingResourceException e) {
                logger.warn("Could not load icon image", e);
            }
        }
    }
}
