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
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_GAME_DATA_DIRECTORY;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_GAME_DIRECTORY;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_INITIAL_HEAP_SIZE;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_LOCALE;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_LOG_LEVEL;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_MAX_HEAP_SIZE;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_SAVE_DOWNLOADED_FILES;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_USER_GAME_PARAMETERS;
import static org.terasology.launcher.settings.LauncherSettings.PROPERTY_USER_JAVA_PARAMETERS;

class TestLauncherSettings {
    @TempDir
    Path tempDirectory;
    @TempDir
    Path gameDirectory;
    @TempDir
    Path gameDataDirectory;

    private LauncherSettings launcherSettings;
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
        assertEquals(launcherSettings.getLocale(), Locale.forLanguageTag(locale));
        assertEquals(launcherSettings.getMaxHeapSize(), JavaHeapSize.valueOf(maxHeapSize));
        assertEquals(launcherSettings.getInitialHeapSize(), JavaHeapSize.valueOf(initialHeapSize));
        assertEquals(launcherSettings.isCloseLauncherAfterGameStart(), Boolean.valueOf(closeLauncherAfterGameStart));
        assertEquals(launcherSettings.getGameDirectory(), gameDirectory);
        assertEquals(launcherSettings.getGameDataDirectory(), gameDataDirectory);
        assertEquals(launcherSettings.isKeepDownloadedFiles(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(launcherSettings.getUserJavaParameters(), userJavaParameters);
        assertEquals(launcherSettings.getUserGameParameters(), userGameParameters);
        assertEquals(launcherSettings.getLogLevel(), Level.valueOf(logLevel));
    }

    @BeforeEach
    void setup() {
        testPropertiesFile = tempDirectory.resolve(LauncherSettings.LAUNCHER_SETTINGS_FILE_NAME);

        launcherSettings = Settings.getDefault();
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

        launcherSettings = Settings.load(testPropertiesFile);
        launcherSettings.init();
        assertPropertiesEqual();
    }

    @Test
    void testInitDefault() throws Exception {
        //null properties file

        launcherSettings = Settings.getDefault();
        launcherSettings.init();

        assertEquals(launcherSettings.getLocale(), Languages.DEFAULT_LOCALE);
        assertEquals(launcherSettings.getMaxHeapSize(), LauncherSettings.MAX_HEAP_SIZE_DEFAULT);
        assertEquals(launcherSettings.getInitialHeapSize(), LauncherSettings.INITIAL_HEAP_SIZE_DEFAULT);
        assertEquals(launcherSettings.isCloseLauncherAfterGameStart(), LauncherSettings.CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT);
        assertNull(launcherSettings.getGameDirectory());
        assertNull(launcherSettings.getGameDataDirectory());
        assertEquals(launcherSettings.isKeepDownloadedFiles(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(launcherSettings.getUserJavaParameters(), LauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        assertEquals(launcherSettings.getUserGameParameters(), LauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        assertEquals(launcherSettings.getLogLevel(), Level.INFO);
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
        launcherSettings.setLocale(Locale.forLanguageTag(locale));
        launcherSettings.setMaxHeapSize(JavaHeapSize.valueOf(maxHeapSize));
        launcherSettings.setInitialHeapSize(JavaHeapSize.valueOf(initialHeapSize));
        launcherSettings.setCloseLauncherAfterGameStart(Boolean.parseBoolean(closeLauncherAfterGameStart));
        launcherSettings.setGameDirectory(gameDirectory);
        launcherSettings.setGameDataDirectory(gameDataDirectory);
        launcherSettings.setKeepDownloadedFiles(Boolean.parseBoolean(saveDownloadedFiles));
        launcherSettings.setUserJavaParameters(userJavaParameters);
        launcherSettings.setUserGameParameters(userGameParameters);
        launcherSettings.setLogLevel(Level.valueOf(logLevel));

        assertPropertiesEqual();
    }

    @Test
    void canRecognizeLastGamePlayed() {
        final var displayVersion = "alpha-20";
        GameIdentifier id = new GameIdentifier(displayVersion, Build.NIGHTLY, Profile.OMEGA);
        // Second object so as not to rely on instance identity.
        GameIdentifier expectedId = new GameIdentifier(displayVersion, Build.NIGHTLY, Profile.OMEGA);

        launcherSettings.setLastPlayedGameVersion(id);
        assertEquals(Optional.of(expectedId), launcherSettings.getLastPlayedGameVersion());
    }
}
