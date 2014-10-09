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

package org.terasology.launcher;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
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
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class TerasologyLauncher extends Application {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyLauncher.class);

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private static final int SPLASH_WIDTH = 800;
    private static final int SPLASH_HEIGHT = 223;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        ImageView splash = new ImageView(BundleUtils.getFxImage("splash"));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH);
        progressText = new Label();
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.getStylesheets().add(BundleUtils.getStylesheet("css_splash"));
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(final Stage initialStage) throws Exception {
        logger.info("TerasologyLauncher is starting");
        logSystemInformation();

        initLanguage();

        final Task<LauncherConfiguration> launcherInitTask = new LauncherInitTask();

        try {
            showSplashStage(initialStage, launcherInitTask);
            new Thread(launcherInitTask).start();
            launcherInitTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(final WorkerStateEvent workerStateEvent) {
                    try {
                        showMainStage(launcherInitTask.valueProperty());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (LauncherStartFailedException e) {
            logger.error("The TerasologyLauncher could not be started!");
            System.exit(1);
        } catch (RuntimeException | Error e) {
            logger.error("The TerasologyLauncher could not be started!", e);

            Path logFile = Paths.get("TerasologyLauncher.log");
            CrashReporter.report(e, logFile);
            System.exit(1);
        }
    }

    private void showMainStage(final ReadOnlyObjectProperty<LauncherConfiguration> launcherConfigurationReadOnlyObjectProperty) throws IOException {
        final LauncherConfiguration launcherConfiguration = launcherConfigurationReadOnlyObjectProperty.getValue();
        if (launcherConfiguration == null) {
            throw new LauncherStartFailedException();
        }

        mainStage = new Stage(StageStyle.DECORATED);

        // launcher frame
        final FXMLLoader fxmlLoader = new FXMLLoader(BundleUtils.getFXMLUrl("application"), ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle",
            Languages.getCurrentLocale()));
        final Parent root = (Parent) fxmlLoader.load();
        final ApplicationController controller = fxmlLoader.getController();
        controller.initialize(launcherConfiguration.getLauncherDirectory(), launcherConfiguration.getDownloadDirectory(), launcherConfiguration.getTempDirectory(),
            launcherConfiguration.getLauncherSettings(), launcherConfiguration.getGameVersions());

        Scene scene = new Scene(root);
        scene.getStylesheets().add(BundleUtils.getStylesheet("css_terasology"));

        mainStage.setTitle("TerasologyLauncher " + TerasologyLauncherVersionInfo.getInstance().getDisplayVersion());
        List<String> iconIds = Arrays.asList("icon16", "icon32", "icon64", "icon128");

        for (String id : iconIds) {
            try {
                Image image = BundleUtils.getFxImage(id);
                mainStage.getIcons().add(image);
            } catch (MissingResourceException e) {
                logger.warn("Could not load icon image", e);
            }
        }
        
        mainStage.setScene(scene);
        mainStage.setResizable(false);
        mainStage.show();

        logger.info("The TerasologyLauncher was successfully started.");
    }

    private void showSplashStage(final Stage initialStage, final Task<LauncherConfiguration> task) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
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
                    fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            initialStage.hide();
                        }
                    });
                    fadeSplash.play();
                } // todo add code to gracefully handle other task states.
            }
        });
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
}
