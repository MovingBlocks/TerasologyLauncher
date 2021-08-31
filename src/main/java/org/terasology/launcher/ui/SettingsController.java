// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import javafx.collections.FXCollections;
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
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private Path launcherDirectory;
    private Settings settings;

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
    private ComboBox<Locale> languageBox;
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
                // save heap size settings
        settings.maxHeapSize.set(maxHeapSizeBox.getSelectionModel().getSelectedItem());
        settings.minHeapSize.set(initialHeapSizeBox.getSelectionModel().getSelectedItem());

        // save log level settings
        settings.logLevel.set(logLevelBox.getSelectionModel().getSelectedItem());

        // save closeLauncherAfterGameStart
        settings.closeLauncherAfterGameStart.set(closeAfterStartBox.isSelected());

        // save showPreReleases
        settings.showPreReleases.set(showPreReleasesBox.isSelected());

        // save saveDownloadedFiles
        settings.keepDownloadedFiles.set(saveDownloadedFilesBox.isSelected());

        //save userParameters (java & game), if textfield is empty then set to defaults
        if (userJavaParametersField.getText().isEmpty()) {
            settings.userJavaParameters.setAll(LegacyLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        } else {
            logger.debug("User defined Java parameters: {}", userJavaParametersField.getText());
            settings.userJavaParameters.setAll(userJavaParametersField.getText().trim().split("\\s"));
        }
        if (userGameParametersField.getText().isEmpty()) {
            settings.userGameParameters.setAll(LegacyLauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        } else {
            logger.debug("User defined game parameters: {}", userGameParametersField.getText());
            settings.userGameParameters.setAll(userGameParametersField.getText().trim().split("\\s"));
        }

        // store changed settings
        final Path settingsFile = launcherDirectory.resolve(Settings.LEGACY_FILE_NAME);
        try {
            Settings.store(settings, settingsFile);
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
        Dialogs.openFileBrowser(stage, settings.gameDataDirectory.get(), BundleUtils.getLabel("message_error_gameDirectory"));
    }

    @FXML
    protected void openGameDataDirectoryAction() {
        Dialogs.openFileBrowser(stage, settings.gameDataDirectory.get(), BundleUtils.getLabel("message_error_gameDataDirectory"));
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

    void initialize(final Path newLauncherDirectory, final Settings newSettings, final Stage newStage) {
        this.launcherDirectory = newLauncherDirectory;
        this.settings = newSettings;
        this.stage = newStage;

        populateHeapSize();
        populateLanguageValues();
        populateLanguageIcons();
        populateCloseLauncherAfterGameStart();
        populateSaveDownloadedFiles();
        populateShowPreReleases();
        populateLogLevel();

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

        settings.locale.bind(languageBox.getSelectionModel().selectedItemProperty());
        settings.locale.addListener((observable, oldValue, newValue) -> {
            Languages.update(newValue);
        });

        languageBox.setItems(FXCollections.observableList(Languages.SUPPORTED_LOCALES));
        languageBox.getSelectionModel().select(Languages.getCurrentLocale());
    }

    private void populateLanguageIcons() {
        languageBox.setCellFactory(p -> new LanguageIconListCell());

        // Make the icon visible in the control area for the selected locale
        languageBox.setButtonCell(languageBox.getCellFactory().call(null));
    }

    private void populateCloseLauncherAfterGameStart() {
        closeAfterStartBox.setSelected(settings.closeLauncherAfterGameStart.get());
    }

    private void populateShowPreReleases() {
        showPreReleasesBox.setSelected(settings.showPreReleases.get());
    }

    private void populateSaveDownloadedFiles() {
        saveDownloadedFilesBox.setSelected(settings.keepDownloadedFiles.get());
    }

    private void populateLogLevel() {
        logLevelBox.getItems().clear();
        for (Level level : Level.values()) {
            logLevelBox.getItems().add(level);
        }
        updateLogLevelSelection();
    }

    private void updateDirectoryPathLabels() {
        gameDirectoryPath.setText(settings.gameDirectory.get().toString());
        gameDataDirectoryPath.setText(settings.gameDataDirectory.get().toString());
        launcherDirectoryPath.setText(launcherDirectory.toString());
    }

    private void updateHeapSizeSelection() {
        maxHeapSizeBox.getSelectionModel().select(settings.maxHeapSize.get());
        initialHeapSizeBox.getSelectionModel().select(settings.minHeapSize.get());
    }

    private void updateLogLevelSelection() {
        logLevelBox.getSelectionModel().select(settings.logLevel.get());
    }

    private void initUserParameterFields() {
        userJavaParametersField.setText(
                String.join(" ", settings.userJavaParameters)
        );

        //if the Game parameters are left default do not display, the prompt message will show
        if (!settings.userGameParameters.isEmpty()) {
            userGameParametersField.setText(String.join(" ", settings.userGameParameters));
        }
    }

    private static class LanguageIconListCell extends ListCell<Locale> {
        @Override
        protected void updateItem(Locale locale, boolean empty) {
            // Pass along the locale text
            super.updateItem(locale, empty);

            if (locale == null || empty) {
                this.setGraphic(null);
            } else {

                String item = locale.toLanguageTag() + " : " + BundleUtils.getLabel(locale, Languages.SETTINGS_LABEL_KEYS.get(locale));
                if (!locale.equals(Languages.getCurrentLocale())) {
                    item += " (" + BundleUtils.getLabel(Languages.SETTINGS_LABEL_KEYS.get(locale)) + ")";
                }
                this.setText(item);

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
