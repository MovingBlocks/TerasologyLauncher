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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.packages.PackageManager;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GuiUtils;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.LogLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private Path launcherDirectory;
    private BaseLauncherSettings launcherSettings;
    private PackageManager packageManager;
    private ApplicationController appController;

    private Path gameDirectory;
    private Path gameDataDirectory;

    private Stage stage;

    @FXML
    private Label gameSettingsTitle;
    @FXML
    private Label maxHeapSizeLabel;
    @FXML
    private Label initialHeapSizeLabel;
    @FXML
    private Button gameDirectoryOpenButton;
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
    private Label launcherSettingsTitle;
    @FXML
    private Label chooseLanguageLabel;
    @FXML
    private CheckBox closeAfterStartBox;
    @FXML
    private CheckBox saveDownloadedFilesBox;
    @FXML
    private Label launcherDirectoryLabel;
    @FXML
    private Button launcherDirectoryOpenButton;
    @FXML
    private CheckBox searchForUpdatesBox;
    @FXML
    private Button saveSettingsButton;
    @FXML
    private Button cancelSettingsButton;
    @FXML
    private ComboBox<JavaHeapSize> maxHeapSizeBox;
    @FXML
    private ComboBox<JavaHeapSize> initialHeapSizeBox;
    @FXML
    private ComboBox<String> languageBox;
    @FXML
    private TextField gameDirectoryPath;
    @FXML
    private TextField gameDataDirectoryPath;
    @FXML
    private TextField launcherDirectoryPath;
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
    protected void openGameDataDirectoryAction() {
        GuiUtils.openFileBrowser(stage, gameDataDirectory, BundleUtils.getLabel("message_error_gameDataDirectory"));
    }

    @FXML
    protected void editGameDataDirectoryAction() {
        final Path selectedFile = GuiUtils.chooseDirectoryDialog(stage, gameDataDirectory, BundleUtils.getLabel("settings_game_gameDataDirectory_edit_title"));
        if (selectedFile != null) {
            try {
                FileUtils.ensureWritableDir(selectedFile);
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

    void initialize(final Path newLauncherDirectory, final BaseLauncherSettings newLauncherSettings,
                    final PackageManager newPackageManager, final Stage newStage, final ApplicationController newAppController) {
        this.launcherDirectory = newLauncherDirectory;
        this.launcherSettings = newLauncherSettings;
        this.packageManager = newPackageManager;
        this.stage = newStage;
        this.appController = newAppController;

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

        gameSettingsTitle.setText(BundleUtils.getLabel("settings_game_title"));
        maxHeapSizeLabel.setText(BundleUtils.getLabel("settings_game_maxHeapSize"));
        initialHeapSizeLabel.setText(BundleUtils.getLabel("settings_game_initialHeapSize"));
        gameDirectoryOpenButton.setText(BundleUtils.getLabel("settings_game_gameDirectory_open"));
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

        launcherSettingsTitle.setText(BundleUtils.getLabel("settings_launcher_title"));
        chooseLanguageLabel.setText(BundleUtils.getLabel("settings_launcher_chooseLanguage"));
        closeAfterStartBox.setText(BundleUtils.getLabel("settings_launcher_closeLauncherAfterGameStart"));
        saveDownloadedFilesBox.setText(BundleUtils.getLabel("settings_launcher_saveDownloadedFiles"));
        launcherDirectoryLabel.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory"));
        launcherDirectoryOpenButton.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory_open"));
        searchForUpdatesBox.setText(BundleUtils.getLabel("settings_launcher_searchForLauncherUpdates"));
        saveSettingsButton.setText(BundleUtils.getLabel("settings_save"));
        cancelSettingsButton.setText(BundleUtils.getLabel("settings_cancel"));
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
        languageBox.setCellFactory(p -> new LanguageIconListCell());

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
