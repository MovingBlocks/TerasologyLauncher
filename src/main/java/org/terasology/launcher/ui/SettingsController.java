// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import javafx.beans.binding.Bindings;
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
import org.terasology.launcher.settings.Settings;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

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
        //TODO reset settings to the state currently persisted to file
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    @FXML
    protected void saveSettingsAction(ActionEvent event) {
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

    //TODO: implement 'reset' action to restore default settings

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
        populateLogLevel();

        closeAfterStartBox.selectedProperty().bindBidirectional(settings.closeLauncherAfterGameStart);
        showPreReleasesBox.selectedProperty().bindBidirectional(settings.showPreReleases);
        saveDownloadedFilesBox.selectedProperty().bindBidirectional(settings.keepDownloadedFiles);

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
        gameSettingsTitle.textProperty().bind(BundleUtils.labelBinding(settings.locale,"settings_game_title"));
        maxHeapSizeLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale,"settings_game_maxHeapSize"));

        initialHeapSizeLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_initialHeapSize"));
        gameDirectoryOpenButton.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_gameDirectory_open"));
        gameDataDirectoryOpenButton.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_gameDataDirectory_open"));
        gameDirectoryLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_gameDirectory"));
        gameDataDirectoryLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_gameDataDirectory"));

        userJavaParametersField.promptTextProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_javaParsPrompt"));
        userGameParametersField.promptTextProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_gameParsPrompt"));

        javaParametersLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_javaParameters"));
        gameParametersLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_gameParameters"));
        logLevelLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_game_logLevel"));

        // Launcher settings

        launcherSettingsTitle.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_launcher_title"));
        chooseLanguageLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_launcher_chooseLanguage"));
        closeAfterStartBox.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_launcher_closeLauncherAfterGameStart"));
        saveDownloadedFilesBox.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_launcher_saveDownloadedFiles"));
        showPreReleasesBox.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_launcher_showPreReleases"));
        launcherDirectoryLabel.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_launcher_launcherDirectory"));
        launcherDirectoryOpenButton.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_launcher_launcherDirectory_open"));
        saveSettingsButton.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_save"));
        cancelSettingsButton.textProperty().bind(BundleUtils.labelBinding(settings.locale, "settings_cancel"));
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
        maxHeapSizeBox.getSelectionModel().select(settings.maxHeapSize.get());
        initialHeapSizeBox.getSelectionModel().select(settings.minHeapSize.get());

        settings.minHeapSize.bind(initialHeapSizeBox.valueProperty());
        settings.maxHeapSize.bind(maxHeapSizeBox.valueProperty());
    }

    private void populateLanguageValues() {
        languageBox.setCellFactory(p -> new LanguageIconListCell());
        // Make the icon visible in the control area for the selected locale
        languageBox.setButtonCell(languageBox.getCellFactory().call(null));

        languageBox.setItems(FXCollections.observableList(Languages.SUPPORTED_LOCALES));
        languageBox.getSelectionModel().select(Languages.getCurrentLocale());

        settings.locale.bind(languageBox.getSelectionModel().selectedItemProperty());
        settings.locale.addListener((observable, oldValue, newValue) -> {
            Languages.update(newValue);
        });
    }

    private void populateLogLevel() {
        logLevelBox.setItems(FXCollections.observableArrayList(Level.values()));
        logLevelBox.getSelectionModel().select(settings.logLevel.get());

        settings.logLevel.bind(logLevelBox.valueProperty());
    }

    private void updateDirectoryPathLabels() {
        gameDirectoryPath.textProperty().bind(Bindings.createStringBinding(()-> settings.gameDirectory.get().toString(), settings.gameDirectory));
        gameDataDirectoryPath.textProperty().bind(Bindings.createStringBinding(()-> settings.gameDataDirectory.get().toString(), settings.gameDataDirectory));
        launcherDirectoryPath.setText(launcherDirectory.toString());
    }

    private void initUserParameterFields() {
        userJavaParametersField.setText(
                String.join(" ", settings.userJavaParameters)
        );

        //if the Game parameters are left default do not display, the prompt message will show
        if (!settings.userGameParameters.isEmpty()) {
            userGameParametersField.setText(String.join(" ", settings.userGameParameters));
        }

        settings.userGameParameters.bind(Bindings.createObjectBinding(() -> {
            logger.debug("User defined Java parameters: {}", userJavaParametersField.getText());
            return FXCollections.observableList(Arrays.stream(userJavaParametersField.getText().split("\\s"))
                    .filter(s -> !s.isBlank())
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }, userGameParametersField.textProperty()));

        settings.userGameParameters.bind(Bindings.createObjectBinding(() -> {
            logger.debug("User defined game parameters: {}", userGameParametersField.getText());
            return FXCollections.observableList(Arrays.stream(userGameParametersField.getText().split("\\s"))
                    .filter(s -> !s.isBlank())
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }, userGameParametersField.textProperty()));
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
