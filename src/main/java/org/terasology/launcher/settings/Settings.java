// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import com.google.gson.Gson;
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
import org.hildan.fxgson.FxGson;
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
import java.util.Locale;
import java.util.Properties;

//TODO: should this be called `SettingsController` and also carry out some UI handling, e.g., displaying error messages
//      to the user?
public final class Settings {
    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static final String LEGACY_FILE_NAME = "TerasologyLauncherSettings.properties";
    private static final String JSON_FILE_NAME = "settings.json";

    @SuppressWarnings("checkstyle:ConstantName")
    private static final Gson gson = FxGson.coreBuilder()
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
        userJavaParameters = new SimpleListProperty<>(FXCollections.observableArrayList("-XX:MaxGCPauseMillis=20"));
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

    static LauncherSettings toLegacy(Settings settings) {
        LauncherSettings legacy = new LauncherSettings(new Properties());

        legacy.setLocale(settings.locale.get());
        legacy.setMaxHeapSize(settings.maxHeapSize.get());
        legacy.setInitialHeapSize(settings.minHeapSize.get());
        legacy.setLogLevel(settings.logLevel.get());
        legacy.setGameDirectory(settings.gameDirectory.get());
        legacy.setGameDirectory(settings.gameDataDirectory.get());
        legacy.setKeepDownloadedFiles(settings.keepDownloadedFiles.get());
        legacy.setShowPreReleases(settings.showPreReleases.get());
        legacy.setCloseLauncherAfterGameStart(settings.closeLauncherAfterGameStart.get());
        legacy.setLastPlayedGameVersion(settings.lastPlayedGameVersion.get());

        legacy.setUserJavaParameters(String.join(" ", settings.userJavaParameters.get()));
        legacy.setUserGameParameters(String.join(" ", settings.userGameParameters.get()));

        return legacy;
    }

    /**
     * Load the launcher settings from disk.
     *
     * The given {@code path} must be the direct parent folder of where the launcher settings are stored.
     *
     * Launcher settings can be persistent in different formats. They are attempted to load in the following order:
     * <ol>
     *     <li>JSON</li>
     *     <li>Java {@link Properties}</li>
     * </ol>
     *
     * @param path the path to the folder containing the launcher settings file
     * @return the launcher settings if present and readable, or {@code null} otherwise
     */
    //TODO: change contract to handle missing file and IO errors better
    public static Settings load(final Path path) {
        Path json = path.resolve(JSON_FILE_NAME);
        if (Files.exists(json)) {
            logger.debug("Loading launcher settings from '{}'.", json);
            try (FileReader reader = new FileReader(json.toFile())) {
                return gson.fromJson(reader, Settings.class);
            } catch (IOException e) {
                logger.error("Error while loading launcher settings from file.", e);
            }
        }
        Path legacy = path.resolve(LEGACY_FILE_NAME);
        if (Files.exists(legacy)) {
            logger.debug("Loading launcher settings from '{}'.", legacy);

            // load settings
            try (InputStream inputStream = Files.newInputStream(legacy)) {
                Properties properties = new Properties();
                properties.load(inputStream);
                return fromLegacy(new LauncherSettings(properties));
            } catch (IOException e) {
                logger.error("Error while loading launcher settings from file.", e);
            }
        }

        return null;
    }

    /**
     * Write the launcher settings to disk.
     *
     * The given {@code path} must be the direct parent folder of where the launcher settings should be stored.
     *
     * The launcher settings are persisted to different formats (to have a fail-over phase before deprecating the legacy
     * format). Calling this method will store the settings in the following format:
     * <ul>
     *     <li>JSON</li>
     *     <li>Java {@link Properties}</li>
     * </ul>
     *
     * @param settings the launcher settings to persist
     * @param path the path to the folder where the launcher settings file should be written to
     * @throws IOException
     */
    public static synchronized void store(final Settings settings, final Path path) throws IOException {
        logger.debug("Writing launcher settings to '{}'.", path);
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }

        Path legacyPath = path.resolve(LEGACY_FILE_NAME);
        try (OutputStream outputStream = Files.newOutputStream(legacyPath)) {
            toLegacy(settings).getProperties().store(outputStream, "Terasology Launcher - Settings");
        }

        //TODO: For the switch, only write JSON. For some failover safety we may write both formats for one or two
        //      releases before fully deprecating the Properties.
        Path jsonPath = path.resolve(JSON_FILE_NAME);
        logger.debug("Writing launcher settings to '{}'.", jsonPath);
        try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
            gson.toJson(settings, writer);
            writer.flush();
        }
    }

    public static Settings getDefault() {
        return new Settings();
    }

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
