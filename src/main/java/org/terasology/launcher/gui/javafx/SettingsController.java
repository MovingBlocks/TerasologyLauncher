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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.JobItem;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.game.VersionItem;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.LogLevel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private File launcherDirectory;
    private File downloadDirectory;
    private BaseLauncherSettings launcherSettings;
    private TerasologyGameVersions gameVersions;

    private File gameDirectory;
    private File gameDataDirectory;

    private Stage stage;

    @FXML
    private Tab gameTab;
    @FXML
    private Label maxHeapSizeLabel;
    @FXML
    private Label initialHeapSizeLabel;
    @FXML
    private Label jobLabel;
    @FXML
    private Label buildVersionLabel;
    @FXML
    private Button gameDirectoryOpenButton;
    @FXML
    private Button gameDirectoryEditButton;
    @FXML
    private Button gameDataDirectoryOpenButton;
    @FXML
    private Button gameDataDirectoryEditButton;
    @FXML
    private Label gameDirectoryLabel;
    @FXML
    private Label gameDataDirectoryLabel;
    @FXML
    private Label javaParametersLabel;
    @FXML
    private Label gameParametersLabel;
    @FXML
    private Label logLevelLabel;
    @FXML
    private Tab launcherTab;
    @FXML
    private Label chooseLanguageLabel;
    @FXML
    private Label closeLauncherLabel;
    @FXML
    private Label saveDownloadedFilesLabel;
    @FXML
    private Label launcherDirectoryLabel;
    @FXML
    private Label downloadDirectoryLabel;
    @FXML
    private Button launcherDirectoryOpenButton;
    @FXML
    private Button downloadDirectoryOpenButton;
    @FXML
    private Label searchForUpdatesLabel;
    @FXML
    private Button saveSettingsButton;
    @FXML
    private Button cancelSettingsButton;

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
    private Label gameDirectoryPath;
    @FXML
    private Label gameDataDirectoryPath;
    @FXML
    private Label launcherDirectoryPath;
    @FXML
    private Label downloadDirectoryPath;
    @FXML
    private TextField userJavaParametersField;
    @FXML
    private TextField userGameParametersField;
    @FXML
    private ComboBox<LogLevel> logLevelBox;

    @FXML
    protected void cancelSettingsAction(ActionEvent event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
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
        } catch (IOException e) {
            logger.error("The launcher settings can not be stored! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
            GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_storeSettings"));
        } finally {
            ((Node) event.getSource()).getScene().getWindow().hide();
        }
    }

    @FXML
    protected void openGameDirectoryAction() {
        GuiUtils.openFileBrowser(stage, gameDirectory, BundleUtils.getLabel("message_error_gameDirectory"));
    }

    @FXML
    protected void editGameDirectoryAction() {
        final File selectedFile = GuiUtils.chooseDirectoryDialog(stage, gameDirectory, BundleUtils.getLabel("settings_game_gameDirectory_edit_title"));
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
    protected void openGameDataDirectoryAction() {
        GuiUtils.openFileBrowser(stage, gameDataDirectory, BundleUtils.getLabel("message_error_gameDataDirectory"));
    }

    @FXML
    protected void editGameDataDirectoryAction() {
        final File selectedFile = GuiUtils.chooseDirectoryDialog(stage, gameDataDirectory, BundleUtils.getLabel("settings_game_gameDataDirectory_edit_title"));
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
    protected void openLauncherDirectoryAction() {
        GuiUtils.openFileBrowser(stage, launcherDirectory, BundleUtils.getLabel("message_error_launcherDirectory"));
    }

    @FXML
    protected void openDownloadDirectoryAction() {
        GuiUtils.openFileBrowser(stage, downloadDirectory, BundleUtils.getLabel("message_error_downloadDirectory"));
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

    public void initialize(final File newLauncherDirectory, final File newDownloadDirectory, final BaseLauncherSettings newLauncherSettings,
                           final TerasologyGameVersions newGameVersions, final Stage newStage) {
        this.launcherDirectory = newLauncherDirectory;
        this.downloadDirectory = newDownloadDirectory;
        this.launcherSettings = newLauncherSettings;
        this.gameVersions = newGameVersions;
        this.stage = newStage;

        populateJobBox();
        populateHeapSize();
        populateLanguageValues();
        populateLanguageIcons();
        populateSearchForLauncherUpdates();
        populateCloseLauncherAfterGameStart();
        populateSaveDownloadedFiles();
        populateLogLevel();

        gameDirectory = newLauncherSettings.getGameDirectory();
        gameDataDirectory = newLauncherSettings.getGameDataDirectory();

        updateDirectoryPathLabels();
        initUserParameterFields();

        setLabelStrings();
    }

    /**
     * Used to assign localized label strings via BundleUtils.
     * Allows for fallback strings to be assigned if the localization-specific ones
     * are absent/empty
     */
    private void setLabelStrings() {
        // Game settings

        gameTab.setText(BundleUtils.getLabel("settings_game_title"));
        maxHeapSizeLabel.setText(BundleUtils.getLabel("settings_game_maxHeapSize"));
        initialHeapSizeLabel.setText(BundleUtils.getLabel("settings_game_initialHeapSize"));
        jobLabel.setText(BundleUtils.getLabel("settings_game_job"));
        buildVersionLabel.setText(BundleUtils.getLabel("settings_game_buildVersion"));
        gameDirectoryOpenButton.setText(BundleUtils.getLabel("settings_game_gameDirectory_open"));
        gameDirectoryEditButton.setText(BundleUtils.getLabel("settings_game_gameDirectory_edit"));
        gameDataDirectoryOpenButton.setText(BundleUtils.getLabel("settings_game_gameDataDirectory_open"));
        gameDataDirectoryEditButton.setText(BundleUtils.getLabel("settings_game_gameDataDirectory_edit"));
        gameDirectoryLabel.setText(BundleUtils.getLabel("settings_game_gameDirectory"));
        gameDataDirectoryLabel.setText(BundleUtils.getLabel("settings_game_gameDataDirectory"));

        userJavaParametersField.setPromptText(BundleUtils.getLabel("settings_game_javaParsPrompt"));
        userGameParametersField.setPromptText(BundleUtils.getLabel("settings_game_gameParsPrompt"));

        javaParametersLabel.setText(BundleUtils.getLabel("settings_game_javaParameters"));
        gameParametersLabel.setText(BundleUtils.getLabel("settings_game_gameParameters"));
        logLevelLabel.setText(BundleUtils.getLabel("settings_game_logLevel"));

        // Launcher settings

        launcherTab.setText(BundleUtils.getLabel("settings_launcher_title"));
        chooseLanguageLabel.setText(BundleUtils.getLabel("settings_launcher_chooseLanguage"));
        closeLauncherLabel.setText(BundleUtils.getLabel("settings_launcher_closeLauncherAfterGameStart"));
        saveDownloadedFilesLabel.setText(BundleUtils.getLabel("settings_launcher_saveDownloadedFiles"));
        launcherDirectoryLabel.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory"));
        downloadDirectoryLabel.setText(BundleUtils.getLabel("settings_launcher_downloadDirectory"));
        launcherDirectoryOpenButton.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory_open"));
        downloadDirectoryOpenButton.setText(BundleUtils.getLabel("settings_launcher_downloadDirectory_open"));
        searchForUpdatesLabel.setText(BundleUtils.getLabel("settings_launcher_searchForLauncherUpdates"));
        saveSettingsButton.setText(BundleUtils.getLabel("settings_save"));
        cancelSettingsButton.setText(BundleUtils.getLabel("settings_cancel"));
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
                }
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
        for (JavaHeapSize heapSize : JavaHeapSize.values()) {
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
    }

    private void populateLanguageIcons() {
        languageBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> p) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        this.setText(item);
                        if (item == null || empty) {
                            this.setGraphic(null);
                        } else {
                            String id = "flag_" + this.getText().split(":")[0].trim();
                            try {
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
                };
            }
        });
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
        gameDirectoryPath.setText(gameDirectory.getPath());
        gameDataDirectoryPath.setText(gameDataDirectory.getPath());
        launcherDirectoryPath.setText(launcherDirectory.getPath());
        downloadDirectoryPath.setText(downloadDirectory.getPath());
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
}
