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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.LauncherConfiguration;
import org.terasology.launcher.game.*;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.util.*;

import java.io.IOException;
import java.nio.file.Path;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @FXML
    private ComboBox<JobItem> jobBox;
    @FXML
    private ComboBox<VersionItem> buildVersionBox;
    @FXML
    private ComboBox<JavaHeapSize> maxHeapSizeBox;
    @FXML
    private ComboBox<JavaHeapSize> initialHeapSizeBox;
    @FXML
    private ComboBox<String> languageBox;
    @FXML
    private CheckBox saveDownloadedFilesBox;
    @FXML
    private CheckBox searchForUpdatesBox;
    @FXML
    private CheckBox closeAfterStartBox;
    @FXML
    private TextField userJavaParametersField;
    @FXML
    private TextField userGameParametersField;
    @FXML
    private ComboBox<LogLevel> logLevelBox;
    @FXML
    private TextField gameDirectoryPath;
    @FXML
    private TextField gameDataDirectoryPath;
    @FXML
    private TextField launcherDirectoryPath;
    @FXML
    private TextField downloadDirectoryPath;

    private Stage stage;
    private Path launcherDirectory;
    private Path downloadDirectory;
    private BaseLauncherSettings launcherSettings;
    private TerasologyGameVersions gameVersions;
    private Path gameDirectory;
    private Path gameDataDirectory;

    @FXML
    protected void cancelSettingsAction(ActionEvent event) {
        // TODO: Cancel all changes and return to previous state (use MVVM)
    }

    @FXML
    protected void saveSettingsAction(ActionEvent event) {
        // save job
        final JobItem jobItem = jobBox.getSelectionModel().getSelectedItem();
        launcherSettings.setJob(jobItem.getJob());

        // save build version
        final VersionItem versionItem = buildVersionBox.getSelectionModel().getSelectedItem();
        launcherSettings.setBuildVersion(versionItem.getVersion(), jobItem.getJob());

        // save gameDirectory
        launcherSettings.setGameDirectory(gameDirectory);

        // save gameDataDirectory
        launcherSettings.setGameDataDirectory(gameDataDirectory);

        // save heap size settings
        launcherSettings.setMaxHeapSize(maxHeapSizeBox.getSelectionModel().getSelectedItem());
        launcherSettings.setInitialHeapSize(initialHeapSizeBox.getSelectionModel().getSelectedItem());

        // save log level settings
        launcherSettings.setLogLevel(logLevelBox.getSelectionModel().getSelectedItem());

        // save languageBox settings
        Languages.update(Languages.SUPPORTED_LOCALES.get(languageBox.getSelectionModel().getSelectedIndex()));
        launcherSettings.setLocale(Languages.getCurrentLocale());

        // save searchForLauncherUpdates
        launcherSettings.setSearchForLauncherUpdates(searchForUpdatesBox.isSelected());

        // save closeLauncherAfterGameStart
        launcherSettings.setCloseLauncherAfterGameStart(closeAfterStartBox.isSelected());

        // save saveDownloadedFiles
        launcherSettings.setKeepDownloadedFiles(saveDownloadedFilesBox.isSelected());

        //save userParameters (java & game), if textfield is empty then set to defaults
        if (userJavaParametersField.getText().isEmpty()) {
            launcherSettings.setUserJavaParameters(BaseLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        } else {
            logger.debug("User defined Java parameters: {}", userJavaParametersField.getText());
            launcherSettings.setUserJavaParameters(userJavaParametersField.getText());
        }
        if (userGameParametersField.getText().isEmpty()) {
            launcherSettings.setUserGameParameters(BaseLauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        } else {
            logger.debug("User defined game parameters: {}", userGameParametersField.getText());
            launcherSettings.setUserGameParameters(userGameParametersField.getText());
        }

        // store changed settings
        try {
            launcherSettings.store();
            logger.info("Successfully saved current launcher settings");
            // TODO: Save current state and disable Save button (use MVVM)
        } catch (IOException e) {
            logger.error("The launcher settings can not be stored! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
            GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_storeSettings"));
        }
    }

    @FXML
    protected void editGameDirectoryAction() {
        final Path selectedFile = GuiUtils.chooseDirectoryDialog(stage, gameDirectory, BundleUtils.getLabel("settings_game_gameDirectory_edit_title"));
        if (selectedFile != null) {
            try {
                DirectoryUtils.checkDirectory(selectedFile);
                gameDirectory = selectedFile;
                updateDirectoryPathLabels();
            } catch (IOException e) {
                logger.error("The game directory can not be created or used! '{}'", gameDirectory, e);
                GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory);
            }
        }
    }

    @FXML
    protected void editGameDataDirectoryAction() {
        final Path selectedFile = GuiUtils.chooseDirectoryDialog(stage, gameDataDirectory, BundleUtils.getLabel("settings_game_gameDataDirectory_edit_title"));
        if (selectedFile != null) {
            try {
                DirectoryUtils.checkDirectory(selectedFile);
                gameDataDirectory = selectedFile;
                updateDirectoryPathLabels();
            } catch (IOException e) {
                logger.error("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
                GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);
            }
        }
    }

    @FXML
    protected void updateMaxHeapSizeBox() {
        final JavaHeapSize initialHeapSize = initialHeapSizeBox.getSelectionModel().getSelectedItem();
        final JavaHeapSize maxHeapSize = maxHeapSizeBox.getSelectionModel().getSelectedItem();
        if ((initialHeapSize != null) && (maxHeapSize != null) && (maxHeapSize.compareTo(initialHeapSize) < 0)) {
            initialHeapSizeBox.getSelectionModel().select(maxHeapSize);
        }
    }

    @FXML
    protected void updateInitialHeapSizeBox() {
        final JavaHeapSize initialHeapSize = initialHeapSizeBox.getSelectionModel().getSelectedItem();
        final JavaHeapSize maxHeapSize = maxHeapSizeBox.getSelectionModel().getSelectedItem();
        if ((initialHeapSize != null) && (maxHeapSize != null) && (maxHeapSize.compareTo(initialHeapSize) < 0)) {
            maxHeapSizeBox.getSelectionModel().select(initialHeapSize);
        }
    }

    void update(final LauncherConfiguration config) {
        this.launcherDirectory = config.getLauncherDirectory();
        this.downloadDirectory = config.getDownloadDirectory();
        this.launcherSettings = config.getLauncherSettings();
        this.gameVersions = config.getGameVersions();

        populateJobBox();
        populateHeapSize();
        populateLanguageValues();
        populateLanguageIcons();
        populateSearchForLauncherUpdates();
        populateCloseLauncherAfterGameStart();
        populateSaveDownloadedFiles();
        populateLogLevel();

        gameDirectory = launcherSettings.getGameDirectory();
        gameDataDirectory = launcherSettings.getGameDataDirectory();

        updateDirectoryPathLabels();
        initUserParameterFields();
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
        jobBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldItem, newItem) -> {
            if (jobBox.getItems().isEmpty()) {
                return;
            }
            launcherSettings.setJob(newItem.getJob());
            updateBuildVersionBox();
            logger.debug("Selected gamejob: {} -- {}", launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
        });

        buildVersionBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVersionItem, newVersionItem) -> {
            if (newVersionItem != null) {
                final Integer version = newVersionItem.getVersion();
                launcherSettings.setBuildVersion(version, launcherSettings.getJob());
                logger.debug("Selected gamejob: {} -- {}", launcherSettings.getJob(), launcherSettings.getBuildVersion(launcherSettings.getJob()));
            }
        });
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

    private void populateHeapSize() {
        maxHeapSizeBox.getItems().clear();
        initialHeapSizeBox.getItems().clear();

        // Limit items till 1.5 GB for 32-bit JVM
        final JavaHeapSize[] heapSizeRange = System.getProperty("os.arch").equals("x86")
                ? Arrays.copyOfRange(JavaHeapSize.values(), 0, JavaHeapSize.GB_1_5.ordinal() + 1)
                : JavaHeapSize.values();

        for (JavaHeapSize heapSize : heapSizeRange) {
            maxHeapSizeBox.getItems().add(heapSize);
            initialHeapSizeBox.getItems().add(heapSize);
        }
        updateHeapSizeSelection();
    }

    private void populateLanguageValues() {
        languageBox.getItems().clear();
        for (Locale locale : Languages.SUPPORTED_LOCALES) {
            String item = locale.toLanguageTag() + " : " + BundleUtils.getLabel(locale, Languages.SETTINGS_LABEL_KEYS.get(locale));
            if (!locale.equals(Languages.getCurrentLocale())) {
                item += " (" + BundleUtils.getLabel(Languages.SETTINGS_LABEL_KEYS.get(locale)) + ")";
            }
            languageBox.getItems().add(item);

            if (Languages.getCurrentLocale().equals(locale)) {
                languageBox.getSelectionModel().select(item);
            }
        }
        Collator coll = Collator.getInstance();
        languageBox.getItems().sort(coll);
    }

    private void populateLanguageIcons() {
        languageBox.setCellFactory(p -> new SettingsController.LanguageIconListCell());

        // Make the icon visible in the control area for the selected locale
        languageBox.setButtonCell(languageBox.getCellFactory().call(null));
    }

    private void populateSearchForLauncherUpdates() {
        searchForUpdatesBox.setSelected(launcherSettings.isSearchForLauncherUpdates());
    }

    private void populateCloseLauncherAfterGameStart() {
        closeAfterStartBox.setSelected(launcherSettings.isCloseLauncherAfterGameStart());
    }

    private void populateSaveDownloadedFiles() {
        saveDownloadedFilesBox.setSelected(launcherSettings.isKeepDownloadedFiles());
    }

    private void populateLogLevel() {
        logLevelBox.getItems().clear();
        for (LogLevel level : LogLevel.values()) {
            logLevelBox.getItems().add(level);
        }
        updateLogLevelSelection();
    }

    private void updateDirectoryPathLabels() {
        gameDirectoryPath.setText(gameDirectory.toString());
        gameDataDirectoryPath.setText(gameDataDirectory.toString());
        launcherDirectoryPath.setText(launcherDirectory.toString());
        downloadDirectoryPath.setText(downloadDirectory.toString());
    }

    private void updateHeapSizeSelection() {
        maxHeapSizeBox.getSelectionModel().select(launcherSettings.getMaxHeapSize());
        initialHeapSizeBox.getSelectionModel().select(launcherSettings.getInitialHeapSize());
    }

    private void updateLogLevelSelection() {
        logLevelBox.getSelectionModel().select(launcherSettings.getLogLevel());
    }

    private void initUserParameterFields() {
        //if the VM parameters are left default do not display, the prompt message will show
        if (!launcherSettings.getUserJavaParameters().equals(BaseLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT)) {
            userJavaParametersField.setText(launcherSettings.getUserJavaParameters());
        }
        //if the Game parameters are left default do not display, the prompt message will show
        if (!launcherSettings.getUserGameParameters().equals(BaseLauncherSettings.USER_GAME_PARAMETERS_DEFAULT)) {
            userGameParametersField.setText(launcherSettings.getUserGameParameters());
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    private static class LanguageIconListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            // Pass along the locale text
            super.updateItem(item, empty);
            this.setText(item);

            if (item == null || empty) {
                this.setGraphic(null);
            } else {
                // Get the key that represents the locale in ImageBundle (flag_xx)
                String countryCode = this.getText().split(":")[0].trim();
                String id = "flag_" + countryCode;

                try {
                    // Get the appropriate flag icon via BundleUtils
                    Image icon = BundleUtils.getFxImage(id);

                    ImageView iconImageView = new ImageView(icon);
                    iconImageView.setFitHeight(11);
                    iconImageView.setPreserveRatio(true);
                    this.setGraphic(iconImageView);
                } catch (MissingResourceException e) {
                    logger.warn("ImageBundle key {} not found", id);
                } catch (NullPointerException e) {
                    logger.warn("Flag icon in ImageBundle key {} missing or corrupt", id);
                }
            }
        }
    }
}
