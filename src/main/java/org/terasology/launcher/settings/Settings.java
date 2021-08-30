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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Optional;
import java.util.Properties;

//TODO: should this be called `SettingsController` and also carry out some UI handling, e.g., displaying error messages
//      to the user?
public final class Settings {
    public static final String DEFAULT_FILE_NAME = "TerasologyLauncherSettings.properties";
    public static final String JSON_FILE_NAME = "settings.json";

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static final String COMMENT_SETTINGS = "Terasology Launcher - Settings";

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Path.class, new PathConverter())
            .setPrettyPrinting()
            .create();

    private Settings() {
    }

    //TODO: change contract to load a file with fixed name from the path such that this method can decide on file format
    public static LauncherSettings load(final Path path) {
        // TODO: try to load from JSON, fall-back to Properties
        Path json = path.getParent().resolve(JSON_FILE_NAME);
        if (Files.exists(json)) {
            logger.debug("Loading launcher settings from '{}'.", json);
            try (FileReader reader = new FileReader(json.toFile())) {
                SettingsObject settings = gson.fromJson(reader, SettingsObject.class);
                logger.info(settings.toString());
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
                return new LauncherSettings(properties);
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
            settings.getProperties().store(outputStream, COMMENT_SETTINGS);
        }

        //TODO: For the switch, only write JSON. For some failover safety we may write both formats for one or two
        //      releases before fully deprecating the Properties.
        SettingsObject json = new SettingsObject(
                settings.getLocale(),
                settings.getMaxHeapSize(),
                settings.getInitialHeapSize(),
                settings.getLogLevel(),
                settings.getGameDirectory(),
                settings.getGameDataDirectory(),
                settings.isKeepDownloadedFiles(),
                settings.isShowPreReleases(),
                settings.isCloseLauncherAfterGameStart(),
                settings.getLastPlayedGameVersion().orElse(null),
                Optional.ofNullable(settings.getBaseJavaParameters()).map(params -> Arrays.asList(params.split("\\s"))).orElse(null),
                Optional.ofNullable(settings.getUserJavaParameters()).map(params -> Arrays.asList(params.split("\\s"))).orElse(null),
                Optional.ofNullable(settings.getUserGameParameters()).map(params -> Arrays.asList(params.split("\\s"))).orElse(null)
        );

        Path jsonPath = path.getParent().resolve(JSON_FILE_NAME);
        logger.debug("Writing launcher settings to '{}'.", jsonPath);
        try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
            gson.toJson(json, writer);
            writer.flush();
        }
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

    public static LauncherSettings getDefault() {
        return new LauncherSettings(new Properties());
    };
}
