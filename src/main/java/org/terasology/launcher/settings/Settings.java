// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

//TODO: should this be called `SettingsController` and also carry out some UI handling, e.g., displaying error messages
//      to the user?
public final class Settings {
    public static final String DEFAULT_FILE_NAME = "TerasologyLauncherSettings.properties";

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static final String COMMENT_SETTINGS = "Terasology Launcher - Settings";

    private Settings() {
    }

    public static LauncherSettings load(final Path path) {
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
    }

    public static LauncherSettings getDefault() {
        return new LauncherSettings(new Properties());
    };
}
