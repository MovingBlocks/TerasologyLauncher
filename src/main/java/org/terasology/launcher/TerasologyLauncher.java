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

package org.terasology.launcher;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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
import org.terasology.crashreporter.CrashReporter;
import org.terasology.launcher.gui.javafx.ApplicationController;
import org.terasology.launcher.log.TempLogFilePropertyDefiner;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.HostServicesWrapper;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.IOException;
import java.nio.file.Path;
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
    private HostServicesWrapper hostServices;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialize the host service wrapper by attempting to use the JavaFX {@link HostServices}.
     * @return the configured host service wrapper
     */
    private HostServicesWrapper initHostServices() {
        HostServices hs;
        try {
            // This may throw an exception on a different thread, but we cannot catch it here o.O
            // In addition, `hostServices` will be initialized, but disfunctional.
            // Thus, we have no idea whether we can use the services or not...
             hs = getHostServices();
            // poor man's check: this will throw a NPE if the internal `hostServices.delegate` is not initialized
            hs.getCodeBase();
        } catch (NullPointerException e) {
            logger.warn("Host Services not available - won't be able to open hyperlinks in the system browser.");
            hs = null;
        }
        return new HostServicesWrapper(hs);
    }

    @Override
    public void init() {
        ImageView splash = new ImageView(BundleUtils.getFxImage("splash"));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH);
        progressText = new Label();
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.getStylesheets().add(BundleUtils.getStylesheet("css_splash"));
        splashLayout.setEffect(new DropShadow());
        hostServices = initHostServices();
    }

    @Override
    public void start(final Stage initialStage) {
        logger.info("TerasologyLauncher is starting");
        logSystemInformation();

        initProxy();
        initLanguage();

        final Task<LauncherConfiguration> launcherInitTask = new LauncherInitTask(initialStage);

        showSplashStage(initialStage, launcherInitTask);
        Thread initThread = new Thread(launcherInitTask);
        initThread.setName("Launcher init thread");
        initThread.setUncaughtExceptionHandler((t, e) -> logger.warn("Initialization failed!", e));

        launcherInitTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(final WorkerStateEvent workerStateEvent) {
                try {
                    LauncherConfiguration config = launcherInitTask.getValue();
                    if (config == null) {
                        throw new LauncherStartFailedException("Launcher configuration was `null`.");
                    } else if (config instanceof NullLauncherConfiguration) {
                        logger.info("Closing the launcher ... (empty configuration, probably due to update)");
                        Platform.exit();
                    } else {
                        showMainStage(config);
                    }
                } catch (IOException | LauncherStartFailedException e) {
                    openCrashReporterAndExit(e);
                }
            }
        });

        launcherInitTask.setOnFailed(event -> {
            Throwable throwable = event.getSource().getException();
            Exception exception;
            if (throwable instanceof Exception) {
                exception = (Exception) throwable;
            } else {
                exception = new Exception("Wrapped throwable, see deeper", throwable);
            }
            //TODO: Should we really crash here? The task state is set to "failed" if an exception is thrown, even if
            //      it is caught...
            // openCrashReporterAndExit(exception);
        });

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

    /**
     * Opens the CrashReporter with the given exception and exits the launcher.
     *
     * @param e the exception causing the launcher to fail
     */
    private void openCrashReporterAndExit(Exception e) {
        logger.error("The TerasologyLauncher could not be started!");

        Path logFile = TempLogFilePropertyDefiner.getInstance().getLogFile();
        //TODO: this hangs on Java 11 instead of showing the CrashReporter
        // CrashReporter.report(e, logFile);
        System.exit(1);
    }

    private void showMainStage(final LauncherConfiguration launcherConfiguration) throws IOException {
        mainStage = new Stage(StageStyle.DECORATED);

        // launcher frame
        FXMLLoader fxmlLoader;
        Parent root;
        /* Fall back to default language if loading the FXML file files with the current locale */
        try {
            fxmlLoader = BundleUtils.getFXMLLoader("application");
            root = (Parent) fxmlLoader.load();
        } catch (IOException e) {
            fxmlLoader = BundleUtils.getFXMLLoader("application");
            fxmlLoader.setResources(ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle", Languages.DEFAULT_LOCALE));
            root = (Parent) fxmlLoader.load();
        }
        final ApplicationController controller = fxmlLoader.getController();
        controller.update(launcherConfiguration.getLauncherDirectory(), launcherConfiguration.getDownloadDirectory(), launcherConfiguration.getTempDirectory(),
                launcherConfiguration.getLauncherSettings(), launcherConfiguration.getPackageManager(), mainStage, hostServices);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(BundleUtils.getStylesheet("css_terasology"));

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
            logger.debug("Java: {} {} {}", System.getProperty("java.version"), System.getProperty("java.vendor"), System.getProperty("java.home"));
            logger.debug("Java VM: {} {} {}", System.getProperty("java.vm.name"), System.getProperty("java.vm.vendor"), System.getProperty("java.vm.version"));
            logger.debug("Java classpath: {}", System.getProperty("java.class.path"));

            // OS
            logger.debug("OS: {} {} {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));

            //Memory
            logger.debug("Max. Memory: {} bytes", Runtime.getRuntime().maxMemory());

            // TerasologyLauncherVersionInfo
            logger.debug("Launcher version: {}", TerasologyLauncherVersionInfo.getInstance());
        }
    }

    private void initLanguage() {
        logger.trace("Init Languages...");
        Languages.init();
        logger.debug("Language: {}", Languages.getCurrentLocale());
    }

    /**
     * Adds title and icons to a stage.
     *
     * @param stage the stage to decorate
     */
    private static void decorateStage(Stage stage) {
        stage.setTitle("TerasologyLauncher " + TerasologyLauncherVersionInfo.getInstance().getDisplayVersion());
        List<String> iconIds = Arrays.asList("icon16", "icon32", "icon64", "icon128");

        for (String id : iconIds) {
            try {
                Image image = BundleUtils.getFxImage(id);
                stage.getIcons().add(image);
            } catch (MissingResourceException e) {
                logger.warn("Could not load icon image", e);
            }
        }
    }
}
