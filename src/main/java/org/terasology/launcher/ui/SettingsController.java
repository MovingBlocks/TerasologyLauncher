// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.util.I18N;
import org.terasology.launcher.util.JavaHeapSize;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private Path launcherDirectory;
    private Settings launcherSettings;
    private ApplicationController appController;

    private Path gameDirectory;
    private Path gameDataDirectory;

    private Stage stage;

    private Locale oldLocale;

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
    private Button resetSettingsButton;
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
    private TitledPane advancedOptionsPane;
    @FXML
    private Label otherSettingsLabel;

    @FXML
    protected void cancelSettingsAction(ActionEvent event) {
        I18N.localeProperty().unbind();
        if (!I18N.getCurrentLocale().equals(oldLocale)) {
            Platform.runLater(() -> I18N.setLocale(oldLocale));
        }
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    @FXML
    protected void saveSettingsAction(ActionEvent event) {
        // save gameDirectory
        launcherSettings.gameDirectory.set(gameDirectory);

        // save gameDataDirectory
        launcherSettings.gameDataDirectory.set(gameDataDirectory);

        // save heap size settings
        launcherSettings.maxHeapSize.set(maxHeapSizeBox.getSelectionModel().getSelectedItem());
        launcherSettings.minHeapSize.set(initialHeapSizeBox.getSelectionModel().getSelectedItem());

        // save log level settings
        launcherSettings.logLevel.set(logLevelBox.getSelectionModel().getSelectedItem());

        // save languageBox settings
        launcherSettings.locale.set(I18N.getCurrentLocale());

        // save closeLauncherAfterGameStart
        launcherSettings.closeLauncherAfterGameStart.set(closeAfterStartBox.isSelected());

        // save showPreReleases
        launcherSettings.showPreReleases.set(showPreReleasesBox.isSelected());

        // save saveDownloadedFiles
        launcherSettings.keepDownloadedFiles.set(saveDownloadedFilesBox.isSelected());

        //save userParameters (java & game), if textfield is empty then set to defaults
        if (userJavaParametersField.getText().isEmpty()) {
            logger.debug("Reapplying default Java parameters: {}", Settings.getDefault().userJavaParameters);
            launcherSettings.userJavaParameters.setAll(Settings.getDefault().userJavaParameters);
        } else {
            logger.debug("User defined Java parameters: {}", userJavaParametersField.getText());
            launcherSettings.userJavaParameters.setAll(asParameterList(userJavaParametersField.getText()));
        }
        if (userGameParametersField.getText().isEmpty()) {
            logger.debug("Reapplying default game parameters: {}", Settings.getDefault().userGameParameters);
            launcherSettings.userGameParameters.setAll(Settings.getDefault().userGameParameters);
        } else {
            logger.debug("User defined game parameters: {}", userGameParametersField.getText());
            launcherSettings.userGameParameters.setAll(asParameterList(userGameParametersField.getText()));
        }

        // store changed settings
        try {
            Settings.store(launcherSettings, launcherDirectory);
        } catch (IOException e) {
            //TODO: unify error handling, probably to Settings a.k.a. SettingsController?
            logger.error("The launcher settings cannot be stored to '{}'.", launcherDirectory, e);
            Dialogs.showError(stage, I18N.getLabel("message_error_storeSettings"));
        } finally {
            ((Node) event.getSource()).getScene().getWindow().hide();
        }
    }

    @FXML
    protected void openGameDirectoryAction() {
        Dialogs.openFileBrowser(stage, gameDirectory, I18N.getLabel("message_error_gameDirectory"));
    }

    @FXML
    protected void openGameDataDirectoryAction() {
        Dialogs.openFileBrowser(stage, gameDataDirectory, I18N.getLabel("message_error_gameDataDirectory"));
    }

    @FXML
    protected void openLauncherDirectoryAction() {
        Dialogs.openFileBrowser(stage, launcherDirectory, I18N.getLabel("message_error_launcherDirectory"));
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

    void initialize(final Path newLauncherDirectory, final Settings newLauncherSettings,
                    final Stage newStage, final ApplicationController newAppController) {
        this.launcherDirectory = newLauncherDirectory;
        this.launcherSettings = newLauncherSettings;
        this.stage = newStage;
        this.appController = newAppController;

        // back up the current locale before doing anything else
        oldLocale = I18N.getCurrentLocale();

        populateHeapSize();
        populateLanguageValues();
        populateCloseLauncherAfterGameStart();
        populateSaveDownloadedFiles();
        populateShowPreReleases();
        populateLogLevel();

        gameDirectory = newLauncherSettings.gameDirectory.get();
        gameDataDirectory = newLauncherSettings.gameDataDirectory.get();

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

        gameSettingsTitle.textProperty().bind(I18N.labelBinding("settings_game_title"));
        maxHeapSizeLabel.textProperty().bind(I18N.labelBinding("settings_game_maxHeapSize"));
        initialHeapSizeLabel.textProperty().bind(I18N.labelBinding("settings_game_initialHeapSize"));
        gameDirectoryOpenButton.textProperty().bind(I18N.labelBinding("settings_game_gameDirectory_open"));
        gameDataDirectoryOpenButton.textProperty().bind(I18N.labelBinding("settings_game_gameDataDirectory_open"));
        gameDirectoryLabel.textProperty().bind(I18N.labelBinding("settings_game_gameDirectory"));
        gameDataDirectoryLabel.textProperty().bind(I18N.labelBinding("settings_game_gameDataDirectory"));

        userJavaParametersField.promptTextProperty().bind(I18N.labelBinding("settings_game_javaParsPrompt"));
        userGameParametersField.promptTextProperty().bind(I18N.labelBinding("settings_game_gameParsPrompt"));

        javaParametersLabel.textProperty().bind(I18N.labelBinding("settings_game_javaParameters"));
        gameParametersLabel.textProperty().bind(I18N.labelBinding("settings_game_gameParameters"));
        logLevelLabel.textProperty().bind(I18N.labelBinding("settings_game_logLevel"));

        // Launcher settings

        launcherSettingsTitle.textProperty().bind(I18N.labelBinding("settings_launcher_title"));
        chooseLanguageLabel.textProperty().bind(I18N.labelBinding("settings_launcher_chooseLanguage"));
        closeAfterStartBox.textProperty().bind(I18N.labelBinding("settings_launcher_closeLauncherAfterGameStart"));
        saveDownloadedFilesBox.textProperty().bind(I18N.labelBinding("settings_launcher_saveDownloadedFiles"));
        showPreReleasesBox.textProperty().bind(I18N.labelBinding("settings_launcher_showPreReleases"));
        launcherDirectoryLabel.textProperty().bind(I18N.labelBinding("settings_launcher_launcherDirectory"));
        launcherDirectoryOpenButton.textProperty().bind(I18N.labelBinding("settings_launcher_launcherDirectory_open"));
        saveSettingsButton.textProperty().bind(I18N.labelBinding("settings_save"));
        cancelSettingsButton.textProperty().bind(I18N.labelBinding("settings_cancel"));
        resetSettingsButton.textProperty().bind(I18N.labelBinding("settings_reset"));
        advancedOptionsPane.textProperty().bind(I18N.labelBinding("settings_advanced_options"));
        otherSettingsLabel.textProperty().bind(I18N.labelBinding("settings_other"));
    }

    private void populateHeapSize() {
        // Limit items till 1.5 GB for 32-bit JVM
        final JavaHeapSize[] heapSizeRange = System.getProperty("os.arch").equals("x86")
                ? Arrays.copyOfRange(JavaHeapSize.values(), 0, JavaHeapSize.GB_1_5.ordinal() + 1)
                : JavaHeapSize.values();

        initialHeapSizeBox.getItems().clear();
        maxHeapSizeBox.getItems().clear();

        initialHeapSizeBox.setButtonCell(new MemorySizeCell());
        maxHeapSizeBox.setButtonCell(new MemorySizeCell());

        initialHeapSizeBox.setCellFactory(param -> new MemorySizeCell());
        maxHeapSizeBox.setCellFactory(param -> new MemorySizeCell());

        for (JavaHeapSize heapSize : heapSizeRange) {
            maxHeapSizeBox.getItems().add(heapSize);
            initialHeapSizeBox.getItems().add(heapSize);
        }

        updateHeapSizeSelection();
    }

    private void populateLanguageValues() {
        languageBox.getItems().clear();
        for (Locale locale : I18N.getSupportedLocales()) {
            languageBox.getItems().add(locale);
            if (I18N.getCurrentLocale().equals(locale)) {
                languageBox.getSelectionModel().select(locale);
            }
        }

        // Set up custom cell factory to render the Locales with flag and translation
        languageBox.setCellFactory(p -> new LanguageIconListCell());

        // Make the icon visible in the control area for the selected locale
        languageBox.setButtonCell(languageBox.getCellFactory().call(null));

        I18N.localeProperty().bind(
                Bindings.createObjectBinding(() -> languageBox.selectionModelProperty().getValue().selectedItemProperty().get(),
                        languageBox.selectionModelProperty(),
                        languageBox.selectionModelProperty().getValue().selectedItemProperty()));

    }

    private void populateCloseLauncherAfterGameStart() {
        closeAfterStartBox.setSelected(launcherSettings.closeLauncherAfterGameStart.get());
    }

    private void populateShowPreReleases() {
        showPreReleasesBox.setSelected(launcherSettings.showPreReleases.get());
    }

    private void populateSaveDownloadedFiles() {
        saveDownloadedFilesBox.setSelected(launcherSettings.keepDownloadedFiles.get());
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
        maxHeapSizeBox.getSelectionModel().select(launcherSettings.maxHeapSize.get());
        initialHeapSizeBox.getSelectionModel().select(launcherSettings.minHeapSize.get());
    }

    private void updateLogLevelSelection() {
        logLevelBox.getSelectionModel().select(launcherSettings.logLevel.get());
    }

    private void initUserParameterFields() {
        //if the VM parameters are left default do not display, the prompt message will show
        List<String> defaultParams = Settings.getDefault().userJavaParameters;
        List<String> userJavaParams = launcherSettings.userJavaParameters.get();
        if (!(defaultParams.containsAll(userJavaParams) && userJavaParams.containsAll(defaultParams))) {
            userJavaParametersField.setText(String.join(" ", launcherSettings.userJavaParameters.get()));
        }
        //if the Game parameters are left default do not display, the prompt message will show
        if (!launcherSettings.userGameParameters.get().isEmpty()) {
            userGameParametersField.setText(String.join(" ", launcherSettings.userGameParameters.get()));
        }
    }

    private List<String> asParameterList(String text) {
        return Arrays.stream(text.split("\\s"))
                .filter(param -> !param.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private static class LanguageIconListCell extends ListCell<Locale> {
        @Override
        protected void updateItem(Locale item, boolean empty) {
            // Pass along the locale text
            super.updateItem(item, empty);

            if (item == null || empty) {
                this.setGraphic(null);
            } else {
                this.setText(item.toLanguageTag() + ": " + I18N.getLabel("settings_language_" + item.toLanguageTag()));
                // Get the key that represents the locale in ImageBundle (flag_xx)
                String countryCode = item.toLanguageTag();
                String id = "flag_" + countryCode;

                // Get the appropriate flag icon via BundleUtils
                Image icon = I18N.getFxImage(id);
                if (icon != null) {
                    ImageView iconImageView = new ImageView(icon);
                    iconImageView.setFitHeight(11);
                    iconImageView.setPreserveRatio(true);
                    this.setGraphic(iconImageView);
                } else {
                    logger.warn("Flag icon for key '{}' could not be loaded.", id);

                }
            }
        }
    }

    private static class MemorySizeCell extends ListCell<JavaHeapSize> {
        @Override
        protected void updateItem(JavaHeapSize item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                this.textProperty().unbind();
                this.setText(null);
            } else {
                this.textProperty().bind(I18N.labelBinding(item.getLabelKey()));
            }
        }
    }
}
