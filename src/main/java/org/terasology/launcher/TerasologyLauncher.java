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

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.launcher.gui.javafx.MainController;
import org.terasology.launcher.log.TempLogFilePropertyDefiner;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

public final class TerasologyLauncher extends Application {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyLauncher.class);

    private final LauncherInitTask initTask = new LauncherInitTask();
    private LauncherConfiguration initialConfig;
    private MainController mainController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        initTask.setOnSucceeded(event -> {
            if (mainController != null) {
                mainController.update(initTask.getValue());
            } else {
                initialConfig = initTask.getValue();
            }
        });
        initTask.setOnFailed(event -> openCrashReporterAndExit((Exception) initTask.getException()));
        new Thread(initTask).start();
    }

    @Override
    public void start(Stage mainStage) throws IOException {
        logger.info("TerasologyLauncher is starting");

        logSystemInformation();
        initProxy();
        initLanguage();

        FXMLLoader fxmlLoader = BundleUtils.getFXMLLoader("main_view");
        Parent root = fxmlLoader.load();
        mainController = fxmlLoader.getController();
        mainController.setStage(mainStage);
        if (initialConfig != null) {
            mainController.update(initialConfig);
        }

        decorateStage(mainStage);
        mainStage.setScene(new Scene(root));
        mainStage.setMinWidth(800.0);
        mainStage.setMinHeight(450.0);
        mainStage.setResizable(true);

        mainStage.setOnCloseRequest(event -> {
            if (!initTask.isDone()) {
                initTask.cancel();
            }
            Platform.exit();
        });
        mainStage.show();

        logger.info("TerasologyLauncher has started successfully.");
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
        CrashReporter.report(e, logFile);
        System.exit(1);
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
