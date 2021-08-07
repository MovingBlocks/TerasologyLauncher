// Copyright 2020 The Terasology Foundation
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
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_GAME_DATA_DIRECTORY;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_GAME_DIRECTORY;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_INITIAL_HEAP_SIZE;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_LOCALE;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_LOG_LEVEL;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_MAX_HEAP_SIZE;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_SAVE_DOWNLOADED_FILES;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_USER_GAME_PARAMETERS;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_USER_JAVA_PARAMETERS;

class TestLauncherSettings {
    @TempDir
    Path tempDirectory;
    @TempDir
    Path gameDirectory;
    @TempDir
    Path gameDataDirectory;

    private LauncherSettings baseLauncherSettings;
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
        assertEquals(baseLauncherSettings.getLocale(), Locale.forLanguageTag(locale));
        assertEquals(baseLauncherSettings.getMaxHeapSize(), JavaHeapSize.valueOf(maxHeapSize));
        assertEquals(baseLauncherSettings.getInitialHeapSize(), JavaHeapSize.valueOf(initialHeapSize));
        assertEquals(baseLauncherSettings.isCloseLauncherAfterGameStart(), Boolean.valueOf(closeLauncherAfterGameStart));
        assertEquals(baseLauncherSettings.getGameDirectory(), gameDirectory);
        assertEquals(baseLauncherSettings.getGameDataDirectory(), gameDataDirectory);
        assertEquals(baseLauncherSettings.isKeepDownloadedFiles(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(baseLauncherSettings.getUserJavaParameters(), userJavaParameters);
        assertEquals(baseLauncherSettings.getUserGameParameters(), userGameParameters);
        assertEquals(baseLauncherSettings.getLogLevel(), Level.valueOf(logLevel));
    }

    @BeforeEach
    void setup() {
        testPropertiesFile = tempDirectory.resolve(BaseLauncherSettings.LAUNCHER_SETTINGS_FILE_NAME);

        baseLauncherSettings = Settings.getDefault();
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

        baseLauncherSettings = Settings.load(testPropertiesFile);
        baseLauncherSettings.init();
        assertPropertiesEqual();
    }

    @Test
    void testInitDefault() throws Exception {
        //null properties file

        baseLauncherSettings = Settings.getDefault();
        baseLauncherSettings.init();

        assertEquals(baseLauncherSettings.getLocale(), Languages.DEFAULT_LOCALE);
        assertEquals(baseLauncherSettings.getMaxHeapSize(), BaseLauncherSettings.MAX_HEAP_SIZE_DEFAULT);
        assertEquals(baseLauncherSettings.getInitialHeapSize(), BaseLauncherSettings.INITIAL_HEAP_SIZE_DEFAULT);
        assertEquals(baseLauncherSettings.isCloseLauncherAfterGameStart(), BaseLauncherSettings.CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT);
        assertNull(baseLauncherSettings.getGameDirectory());
        assertNull(baseLauncherSettings.getGameDataDirectory());
        assertEquals(baseLauncherSettings.isKeepDownloadedFiles(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(baseLauncherSettings.getUserJavaParameters(), BaseLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        assertEquals(baseLauncherSettings.getUserGameParameters(), BaseLauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        assertEquals(baseLauncherSettings.getLogLevel(), Level.INFO);
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
        baseLauncherSettings.setLocale(Locale.forLanguageTag(locale));
        baseLauncherSettings.setMaxHeapSize(JavaHeapSize.valueOf(maxHeapSize));
        baseLauncherSettings.setInitialHeapSize(JavaHeapSize.valueOf(initialHeapSize));
        baseLauncherSettings.setCloseLauncherAfterGameStart(Boolean.parseBoolean(closeLauncherAfterGameStart));
        baseLauncherSettings.setGameDirectory(gameDirectory);
        baseLauncherSettings.setGameDataDirectory(gameDataDirectory);
        baseLauncherSettings.setKeepDownloadedFiles(Boolean.parseBoolean(saveDownloadedFiles));
        baseLauncherSettings.setUserJavaParameters(userJavaParameters);
        baseLauncherSettings.setUserGameParameters(userGameParameters);
        baseLauncherSettings.setLogLevel(Level.valueOf(logLevel));

        assertPropertiesEqual();
    }

    @Test
    void canRecognizeLastGamePlayed() {
        final var displayVersion = "alpha-20";
        final var engineVersion = "5.0.1-SNAPSHOT";
        GameIdentifier id = new GameIdentifier(displayVersion, engineVersion, Build.NIGHTLY, Profile.OMEGA);
        // Second object so as not to rely on instance identity.
        GameIdentifier expectedId = new GameIdentifier(displayVersion, engineVersion,
                Build.NIGHTLY, Profile.OMEGA);

        baseLauncherSettings.setLastPlayedGameVersion(id);
        assertEquals(Optional.of(expectedId), baseLauncherSettings.getLastPlayedGameVersion());
    }
}
