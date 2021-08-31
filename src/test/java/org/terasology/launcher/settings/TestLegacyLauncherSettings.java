// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.Profile;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_GAME_DATA_DIRECTORY;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_GAME_DIRECTORY;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_INITIAL_HEAP_SIZE;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_LOCALE;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_LOG_LEVEL;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_MAX_HEAP_SIZE;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_SAVE_DOWNLOADED_FILES;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_USER_GAME_PARAMETERS;
import static org.terasology.launcher.settings.LegacyLauncherSettings.PROPERTY_USER_JAVA_PARAMETERS;

class TestLegacyLauncherSettings {
    @TempDir
    Path tempDirectory;
    @TempDir
    Path gameDirectory;
    @TempDir
    Path gameDataDirectory;

    private LegacyLauncherSettings legacyLauncherSettings;
    private Path testPropertiesFile;

    private String locale;
    private String maxHeapSize;
    private String initialHeapSize;
    private String closeLauncherAfterGameStart;
    private String saveDownloadedFiles;
    private String userJavaParameters;
    private String userGameParameters;
    private String logLevel;

    private void assertPropertiesEqual() throws Exception {
        assertEquals(legacyLauncherSettings.getLocale(), Locale.forLanguageTag(locale));
        assertEquals(legacyLauncherSettings.getMaxHeapSize(), JavaHeapSize.valueOf(maxHeapSize));
        assertEquals(legacyLauncherSettings.getInitialHeapSize(), JavaHeapSize.valueOf(initialHeapSize));
        assertEquals(legacyLauncherSettings.isCloseLauncherAfterGameStart(), Boolean.valueOf(closeLauncherAfterGameStart));
        assertEquals(legacyLauncherSettings.getGameDirectory(), gameDirectory);
        assertEquals(legacyLauncherSettings.getGameDataDirectory(), gameDataDirectory);
        assertEquals(legacyLauncherSettings.isKeepDownloadedFiles(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(legacyLauncherSettings.getUserJavaParameters(), userJavaParameters);
        assertEquals(legacyLauncherSettings.getUserGameParameters(), userGameParameters);
        assertEquals(legacyLauncherSettings.getLogLevel(), Level.valueOf(logLevel));
    }

    @BeforeEach
    void setup() {
        testPropertiesFile = tempDirectory.resolve(LegacyLauncherSettings.LAUNCHER_SETTINGS_FILE_NAME);

        legacyLauncherSettings = Settings.getDefault();
    }

    @Test
    void testInitWithValues() throws Exception {
        //initialise properties with sample values
        locale = "en";
        maxHeapSize = "GB_2_5";
        initialHeapSize = "GB_1_5";
        closeLauncherAfterGameStart = "false";
        saveDownloadedFiles = "false";
        userJavaParameters = "-XXnoSystemGC";
        userGameParameters = "-headless";
        logLevel = "DEBUG";

        //set properties
        Properties testProperties = new Properties();
        testProperties.setProperty(PROPERTY_LOCALE, locale);
        testProperties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxHeapSize);
        testProperties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialHeapSize);
        testProperties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, closeLauncherAfterGameStart);
        testProperties.setProperty(PROPERTY_GAME_DIRECTORY, gameDirectory.toUri().toString());
        testProperties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, gameDataDirectory.toUri().toString());
        testProperties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, saveDownloadedFiles);
        testProperties.setProperty(PROPERTY_USER_JAVA_PARAMETERS, userJavaParameters);
        testProperties.setProperty(PROPERTY_USER_GAME_PARAMETERS, userGameParameters);
        testProperties.setProperty(PROPERTY_LOG_LEVEL, logLevel);

        //store in properties file
        try (OutputStream output = Files.newOutputStream(testPropertiesFile)) {
            testProperties.store(output, null);
        }

        legacyLauncherSettings = Settings.load(testPropertiesFile);
        legacyLauncherSettings.init();
        assertPropertiesEqual();
    }

    @Test
    void testInitDefault() throws Exception {
        //null properties file

        legacyLauncherSettings = Settings.getDefault();
        legacyLauncherSettings.init();

        assertEquals(legacyLauncherSettings.getLocale(), Languages.DEFAULT_LOCALE);
        assertEquals(legacyLauncherSettings.getMaxHeapSize(), LegacyLauncherSettings.MAX_HEAP_SIZE_DEFAULT);
        assertEquals(legacyLauncherSettings.getInitialHeapSize(), LegacyLauncherSettings.INITIAL_HEAP_SIZE_DEFAULT);
        assertEquals(legacyLauncherSettings.isCloseLauncherAfterGameStart(), LegacyLauncherSettings.CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT);
        assertNull(legacyLauncherSettings.getGameDirectory());
        assertNull(legacyLauncherSettings.getGameDataDirectory());
        assertEquals(legacyLauncherSettings.isKeepDownloadedFiles(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(legacyLauncherSettings.getUserJavaParameters(), LegacyLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        assertEquals(legacyLauncherSettings.getUserGameParameters(), LegacyLauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        assertEquals(legacyLauncherSettings.getLogLevel(), Level.INFO);
    }

    @Test
    void testSetters() throws Exception {
        //re-initialise properties with sample values
        locale = "fr";
        maxHeapSize = "GB_4";
        initialHeapSize = "GB_3";
        closeLauncherAfterGameStart = "true";
        saveDownloadedFiles = "true";
        userJavaParameters = "-XXUseParNewGC -XXUseConcMarkSweepGC";
        userGameParameters = "-noCrashReport";
        logLevel = "INFO";

        //set using setters
        legacyLauncherSettings.setLocale(Locale.forLanguageTag(locale));
        legacyLauncherSettings.setMaxHeapSize(JavaHeapSize.valueOf(maxHeapSize));
        legacyLauncherSettings.setInitialHeapSize(JavaHeapSize.valueOf(initialHeapSize));
        legacyLauncherSettings.setCloseLauncherAfterGameStart(Boolean.parseBoolean(closeLauncherAfterGameStart));
        legacyLauncherSettings.setGameDirectory(gameDirectory);
        legacyLauncherSettings.setGameDataDirectory(gameDataDirectory);
        legacyLauncherSettings.setKeepDownloadedFiles(Boolean.parseBoolean(saveDownloadedFiles));
        legacyLauncherSettings.setUserJavaParameters(userJavaParameters);
        legacyLauncherSettings.setUserGameParameters(userGameParameters);
        legacyLauncherSettings.setLogLevel(Level.valueOf(logLevel));

        assertPropertiesEqual();
    }

    @Test
    void canRecognizeLastGamePlayed() {
        final var displayVersion = "alpha-20";
        GameIdentifier id = new GameIdentifier(displayVersion, Build.NIGHTLY, Profile.OMEGA);
        // Second object so as not to rely on instance identity.
        GameIdentifier expectedId = new GameIdentifier(displayVersion, Build.NIGHTLY, Profile.OMEGA);

        legacyLauncherSettings.setLastPlayedGameVersion(id);
        assertEquals(Optional.of(expectedId), legacyLauncherSettings.getLastPlayedGameVersion());
    }
}
