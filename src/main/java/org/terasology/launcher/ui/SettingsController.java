// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

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
import org.slf4j.event.Level;
import org.terasology.launcher.settings.LegacyLauncherSettings;
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;

import java.io.IOException;
import java.nio.file.Path;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private Path launcherDirectory;
    private LegacyLauncherSettings legacyLauncherSettings;
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
    private CheckBox showPreReleasesBox;
    @FXML
    private CheckBox saveDownloadedFilesBox;
    @FXML
    private Label launcherDirectoryLabel;
    @FXML
    private Button launcherDirectoryOpenButton;
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
    private ComboBox<Level> logLevelBox;

    @FXML
    protected void cancelSettingsAction(ActionEvent event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    @FXML
    protected void saveSettingsAction(ActionEvent event) {
        // save gameDirectory
        legacyLauncherSettings.setGameDirectory(gameDirectory);

        // save gameDataDirectory
        legacyLauncherSettings.setGameDataDirectory(gameDataDirectory);

        // save heap size settings
        legacyLauncherSettings.setMaxHeapSize(maxHeapSizeBox.getSelectionModel().getSelectedItem());
        legacyLauncherSettings.setInitialHeapSize(initialHeapSizeBox.getSelectionModel().getSelectedItem());

        // save log level settings
        legacyLauncherSettings.setLogLevel(logLevelBox.getSelectionModel().getSelectedItem());

        // save languageBox settings
        Languages.update(Languages.SUPPORTED_LOCALES.get(languageBox.getSelectionModel().getSelectedIndex()));
        legacyLauncherSettings.setLocale(Languages.getCurrentLocale());

        // save closeLauncherAfterGameStart
        legacyLauncherSettings.setCloseLauncherAfterGameStart(closeAfterStartBox.isSelected());

        // save showPreReleases
        legacyLauncherSettings.setShowPreReleases(showPreReleasesBox.isSelected());

        // save saveDownloadedFiles
        legacyLauncherSettings.setKeepDownloadedFiles(saveDownloadedFilesBox.isSelected());

        //save userParameters (java & game), if textfield is empty then set to defaults
        if (userJavaParametersField.getText().isEmpty()) {
            legacyLauncherSettings.setUserJavaParameters(LegacyLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        } else {
            logger.debug("User defined Java parameters: {}", userJavaParametersField.getText());
            legacyLauncherSettings.setUserJavaParameters(userJavaParametersField.getText());
        }
        if (userGameParametersField.getText().isEmpty()) {
            legacyLauncherSettings.setUserGameParameters(LegacyLauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        } else {
            logger.debug("User defined game parameters: {}", userGameParametersField.getText());
            legacyLauncherSettings.setUserGameParameters(userGameParametersField.getText());
        }

        // store changed settings
        final Path settingsFile = launcherDirectory.resolve(Settings.LEGACY_FILE_NAME);
        try {
            Settings.store(legacyLauncherSettings, settingsFile);
        } catch (IOException e) {
            //TODO: unify error handling, probably to Settings a.k.a. SettingsController?
            logger.error("The launcher settings cannot be stored! '{}'", settingsFile, e);
            Dialogs.showError(stage, BundleUtils.getLabel("message_error_storeSettings"));
        } finally {
            ((Node) event.getSource()).getScene().getWindow().hide();
        }
    }

    @FXML
    protected void openGameDirectoryAction() {
        Dialogs.openFileBrowser(stage, gameDirectory, BundleUtils.getLabel("message_error_gameDirectory"));
    }

    @FXML
    protected void openGameDataDirectoryAction() {
        Dialogs.openFileBrowser(stage, gameDataDirectory, BundleUtils.getLabel("message_error_gameDataDirectory"));
    }

    @FXML
    protected void openLauncherDirectoryAction() {
        Dialogs.openFileBrowser(stage, launcherDirectory, BundleUtils.getLabel("message_error_launcherDirectory"));
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

    void initialize(final Path newLauncherDirectory, final LegacyLauncherSettings newLegacyLauncherSettings,
                    final Stage newStage, final ApplicationController newAppController) {
        this.launcherDirectory = newLauncherDirectory;
        this.legacyLauncherSettings = newLegacyLauncherSettings;
        this.stage = newStage;
        this.appController = newAppController;

        populateHeapSize();
        populateLanguageValues();
        populateLanguageIcons();
        populateCloseLauncherAfterGameStart();
        populateSaveDownloadedFiles();
        populateShowPreReleases();
        populateLogLevel();

        gameDirectory = newLegacyLauncherSettings.getGameDirectory();
        gameDataDirectory = newLegacyLauncherSettings.getGameDataDirectory();

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
        showPreReleasesBox.setText(BundleUtils.getLabel("settings_launcher_showPreReleases"));
        launcherDirectoryLabel.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory"));
        launcherDirectoryOpenButton.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory_open"));
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

    private void populateCloseLauncherAfterGameStart() {
        closeAfterStartBox.setSelected(legacyLauncherSettings.isCloseLauncherAfterGameStart());
    }

    private void populateShowPreReleases() {
        showPreReleasesBox.setSelected(legacyLauncherSettings.isShowPreReleases());
    }

    private void populateSaveDownloadedFiles() {
        saveDownloadedFilesBox.setSelected(legacyLauncherSettings.isKeepDownloadedFiles());
    }

    private void populateLogLevel() {
        logLevelBox.getItems().clear();
        for (Level level : Level.values()) {
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
        maxHeapSizeBox.getSelectionModel().select(legacyLauncherSettings.getMaxHeapSize());
        initialHeapSizeBox.getSelectionModel().select(legacyLauncherSettings.getInitialHeapSize());
    }

    private void updateLogLevelSelection() {
        logLevelBox.getSelectionModel().select(legacyLauncherSettings.getLogLevel());
    }

    private void initUserParameterFields() {
        //if the VM parameters are left default do not display, the prompt message will show
        if (!legacyLauncherSettings.getUserJavaParameters().equals(LegacyLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT)) {
            userJavaParametersField.setText(legacyLauncherSettings.getUserJavaParameters());
        }
        //if the Game parameters are left default do not display, the prompt message will show
        if (!legacyLauncherSettings.getUserGameParameters().equals(LegacyLauncherSettings.USER_GAME_PARAMETERS_DEFAULT)) {
            userGameParametersField.setText(legacyLauncherSettings.getUserGameParameters());
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
