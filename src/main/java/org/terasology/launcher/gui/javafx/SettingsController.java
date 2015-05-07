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

package org.terasology.launcher.gui.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.LauncherSettings;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.JobItem;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.game.VersionItem;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private File launcherDirectory;
    private File downloadDirectory;
    private LauncherSettings launcherSettings;
    private TerasologyGameVersions gameVersions;

    private File gameDirectory;
    private File gameDataDirectory;

    private Stage stage;

    @FXML
    private ComboBox<JobItem> jobBox;
    @FXML
    private ComboBox<VersionItem> buildVersionBox;
    @FXML
    private ComboBox<JavaHeapSize> maxHeapSizeBox;
    @FXML
    private ComboBox<JavaHeapSize> initialHeapSizeBox;
    @FXML
    private ChoiceBox<String> languageBox;
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

        // save languageBox settings
        Languages.update(Languages.SUPPORTED_LOCALES.get(languageBox.getSelectionModel().getSelectedIndex()));
        launcherSettings.setLocale(Languages.getCurrentLocale());

        // save searchForLauncherUpdates
        launcherSettings.setSearchForLauncherUpdates(searchForUpdatesBox.isSelected());

        // save closeLauncherAfterGameStart
        launcherSettings.setCloseLauncherAfterGameStart(closeAfterStartBox.isSelected());

        // save saveDownloadedFiles
        launcherSettings.setSaveDownloadedFiles(saveDownloadedFilesBox.isSelected());

        //save userParameters (java & game), if textfield is empty then set to defaults
        if (userJavaParametersField.getText().isEmpty()) {
            launcherSettings.setUserJavaParameters(LauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        }
        if (userGameParametersField.getText().isEmpty()) {
            launcherSettings.setUserGameParameters(LauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
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
        try {
            DirectoryUtils.checkDirectory(gameDirectory);
            Desktop.getDesktop().open(gameDirectory);
        } catch (IOException e) {
            logger.error("The game directory can not be opened! '{}'", gameDirectory, e);
            GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory);
        }
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
        try {
            DirectoryUtils.checkDirectory(gameDataDirectory);
            Desktop.getDesktop().open(gameDataDirectory);
        } catch (IOException e) {
            logger.error("The game data directory can not be opened! '{}'", gameDataDirectory, e);
            GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory);
        }
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
        try {
            DirectoryUtils.checkDirectory(launcherDirectory);
            Desktop.getDesktop().open(launcherDirectory);
        } catch (IOException e) {
            logger.error("The game launcher directory can not be opened! '{}'", launcherDirectory, e);
            GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_launcherDirectory") + "\n" + launcherDirectory);
        }
    }

    @FXML
    protected void openDownloadDirectoryAction() {
        try {
            DirectoryUtils.checkDirectory(downloadDirectory);
            Desktop.getDesktop().open(downloadDirectory);
        } catch (IOException e) {
            logger.error("The game download directory can not be opened! '{}'", downloadDirectory, e);
            GuiUtils.showErrorMessageDialog(stage, BundleUtils.getLabel("message_error_downloadDirectory") + "\n" + downloadDirectory);
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

    public void initialize(final File newLauncherDirectory, final File newDownloadDirectory, final LauncherSettings newLauncherSettings,
                           final TerasologyGameVersions newGameVersions, final Stage newStage) {
        this.launcherDirectory = newLauncherDirectory;
        this.downloadDirectory = newDownloadDirectory;
        this.launcherSettings = newLauncherSettings;
        this.gameVersions = newGameVersions;
        this.stage = newStage;

        populateJobBox();
        populateHeapSize();
        populateLanguage();
        populateSearchForLauncherUpdates();
        populateCloseLauncherAfterGameStart();
        populateSaveDownloadedFiles();
        gameDirectory = newLauncherSettings.getGameDirectory();
        gameDataDirectory = newLauncherSettings.getGameDataDirectory();

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

    private void populateLanguage() {
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

    private void populateSearchForLauncherUpdates() {
        searchForUpdatesBox.setSelected(launcherSettings.isSearchForLauncherUpdates());
    }

    private void populateCloseLauncherAfterGameStart() {
        closeAfterStartBox.setSelected(launcherSettings.isCloseLauncherAfterGameStart());
    }

    private void populateSaveDownloadedFiles() {
        saveDownloadedFilesBox.setSelected(launcherSettings.isSaveDownloadedFiles());
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

    private void initUserParameterFields()  {
        //if the VM parameters are left default do not display, the prompt message will show
        if (!launcherSettings.getUserJavaParameters().equals(LauncherSettings.USER_JAVA_PARAMETERS_DEFAULT)) {
            userJavaParametersField.setText(launcherSettings.getUserJavaParameters());
        }
        //if the Game parameters are left default do not display, the prompt message will show
        if (!launcherSettings.getUserGameParameters().equals(LauncherSettings.USER_GAME_PARAMETERS_DEFAULT)) {
            userJavaParametersField.setText(launcherSettings.getUserJavaParameters());
        }
    }
}
