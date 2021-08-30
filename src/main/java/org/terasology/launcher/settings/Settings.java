// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

//TODO: should this be called `SettingsController` and also carry out some UI handling, e.g., displaying error messages
//      to the user?
public final class Settings {
    public static final String LEGACY_FILE_NAME = "TerasologyLauncherSettings.properties";
    public static final String JSON_FILE_NAME = "settings.json";

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Path.class, new PathConverter())
            .setPrettyPrinting()
            .create();

    public final ObjectProperty<Locale> locale;

    public final ObjectProperty<JavaHeapSize> maxHeapSize;
    public final ObjectProperty<JavaHeapSize> minHeapSize;

    public final ObjectProperty<Level> logLevel;

    public final ObjectProperty<Path> gameDirectory;
    public final ObjectProperty<Path> gameDataDirectory;

    public final BooleanProperty keepDownloadedFiles;
    public final BooleanProperty showPreReleases;
    public final BooleanProperty closeLauncherAfterGameStart;

    public final ObjectProperty<GameIdentifier> lastPlayedGameVersion;

    public final ListProperty<String> userJavaParameters;
    public final ListProperty<String> userGameParameters;

    Settings() {
        locale = new SimpleObjectProperty<>(Languages.getCurrentLocale());
        maxHeapSize = new SimpleObjectProperty<>(JavaHeapSize.NOT_USED);
        minHeapSize = new SimpleObjectProperty<>(JavaHeapSize.NOT_USED);
        logLevel = new SimpleObjectProperty<>(Level.INFO);
        gameDirectory = new SimpleObjectProperty<>();
        gameDataDirectory = new SimpleObjectProperty<>();
        keepDownloadedFiles = new SimpleBooleanProperty(false);
        showPreReleases = new SimpleBooleanProperty(false);
        closeLauncherAfterGameStart = new SimpleBooleanProperty(true);
        lastPlayedGameVersion = new SimpleObjectProperty<>();
        userJavaParameters = new SimpleListProperty<>(FXCollections.observableArrayList());
        userGameParameters = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    static Settings fromLegacy(LauncherSettings legacyLauncherSettings) {
        Settings jsonSettings = new Settings();

        jsonSettings.locale.setValue(legacyLauncherSettings.getLocale());
        jsonSettings.maxHeapSize.setValue(legacyLauncherSettings.getMaxHeapSize());
        jsonSettings.minHeapSize.setValue(legacyLauncherSettings.getInitialHeapSize());
        jsonSettings.logLevel.setValue(legacyLauncherSettings.getLogLevel());
        jsonSettings.gameDirectory.setValue(legacyLauncherSettings.getGameDirectory());
        jsonSettings.gameDataDirectory.setValue(legacyLauncherSettings.getGameDataDirectory());
        jsonSettings.keepDownloadedFiles.setValue(legacyLauncherSettings.isKeepDownloadedFiles());
        jsonSettings.showPreReleases.setValue(legacyLauncherSettings.isShowPreReleases());
        jsonSettings.closeLauncherAfterGameStart.setValue(legacyLauncherSettings.isCloseLauncherAfterGameStart());
        jsonSettings.lastPlayedGameVersion.setValue(legacyLauncherSettings.getLastPlayedGameVersion().orElse(null));

        jsonSettings.userJavaParameters.setAll(legacyLauncherSettings.getJavaParameterList());
        jsonSettings.userGameParameters.setAll(legacyLauncherSettings.getUserGameParameterList());

        return jsonSettings;
    }

    //TODO: change contract to load a file with fixed name from the path such that this method can decide on file format
    public static LauncherSettings load(final Path path) {
        // TODO: try to load from JSON, fall-back to Properties
        Path json = path.getParent().resolve(JSON_FILE_NAME);
        if (Files.exists(json)) {
            logger.debug("Loading launcher settings from '{}'.", json);
            try (FileReader reader = new FileReader(json.toFile())) {
                Settings jsonSettings = gson.fromJson(reader, Settings.class);
                logger.info(jsonSettings.toString());
            } catch (IOException e) {
                logger.error("Error while loading launcher settings from file.", e);
            }
        }
        if (Files.exists(path)) {
            logger.debug("Loading launcher settings from '{}'.", path);

            // load settings
            try (InputStream inputStream = Files.newInputStream(path)) {
                Properties properties = new Properties();
                properties.load(inputStream);
                LauncherSettings legacyLauncherSettings =  new LauncherSettings(properties);

                Settings jsonSettings = fromLegacy(legacyLauncherSettings);

                return legacyLauncherSettings;
            } catch (IOException e) {
                logger.error("Error while loading launcher settings from file.", e);
            }
        }

        return null;
    }

    public static synchronized void store(final LauncherSettings settings, final Path path) throws IOException {
        logger.debug("Writing launcher settings to '{}'.", path);
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            settings.getProperties().store(outputStream, "Terasology Launcher - Settings");
        }

        //TODO: For the switch, only write JSON. For some failover safety we may write both formats for one or two
        //      releases before fully deprecating the Properties.
        Settings jsonSettings = fromLegacy(settings);

        Path jsonPath = path.getParent().resolve(JSON_FILE_NAME);
        logger.debug("Writing launcher settings to '{}'.", jsonPath);
        try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
            gson.toJson(jsonSettings, writer);
            writer.flush();
        }
    }

    public static LauncherSettings getDefault() {
        return new LauncherSettings(new Properties());
    };

    static class PathConverter implements JsonDeserializer<Path>, JsonSerializer<Path> {
        @Override
        public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Paths.get(json.getAsString());
        }

        @Override
        public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }
}
