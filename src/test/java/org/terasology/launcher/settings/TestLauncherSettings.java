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
import org.terasology.launcher.util.I18N;
import org.terasology.launcher.util.JavaHeapSize;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
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

    private Settings launcherSettings;
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
        assertEquals(launcherSettings.locale.get(), Locale.forLanguageTag(locale));
        assertEquals(launcherSettings.maxHeapSize.get(), JavaHeapSize.valueOf(maxHeapSize));
        assertEquals(launcherSettings.minHeapSize.get(), JavaHeapSize.valueOf(initialHeapSize));
        assertEquals(launcherSettings.closeLauncherAfterGameStart.get(), Boolean.valueOf(closeLauncherAfterGameStart));
        assertEquals(launcherSettings.gameDirectory.get(), gameDirectory);
        assertEquals(launcherSettings.gameDataDirectory.get(), gameDataDirectory);
        assertEquals(launcherSettings.keepDownloadedFiles.get(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(String.join(" ", launcherSettings.userJavaParameters.get()), userJavaParameters);
        assertEquals(String.join(" ", launcherSettings.userGameParameters.get()), userGameParameters);
        assertEquals(launcherSettings.logLevel.get(), Level.valueOf(logLevel));
    }

    @BeforeEach
    void setup() {
        testPropertiesFile = tempDirectory.resolve(LauncherSettings.LAUNCHER_LEGACY_SETTINGS_FILE_NAME);

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

        launcherSettings = Settings.load(testPropertiesFile.getParent());
        assertPropertiesEqual();
    }

    @Test
    void testInitDefault() throws Exception {
        //null properties file

        launcherSettings = Settings.getDefault();

        assertEquals(launcherSettings.locale.get(), I18N.getDefaultLocale());
        assertEquals(launcherSettings.maxHeapSize.get(), LauncherSettings.MAX_HEAP_SIZE_DEFAULT);
        assertEquals(launcherSettings.minHeapSize.get(), LauncherSettings.INITIAL_HEAP_SIZE_DEFAULT);
        assertEquals(launcherSettings.closeLauncherAfterGameStart.get(), LauncherSettings.CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT);
        assertNull(launcherSettings.gameDirectory.get());
        assertNull(launcherSettings.gameDataDirectory.get());
        assertEquals(launcherSettings.keepDownloadedFiles.get(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(String.join(" ", launcherSettings.userJavaParameters.get()), LauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        assertEquals(String.join(" ", launcherSettings.userGameParameters.get()), LauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        assertEquals(launcherSettings.logLevel.get(), Level.INFO);
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
        launcherSettings.locale.set(Locale.forLanguageTag(locale));
        launcherSettings.maxHeapSize.set(JavaHeapSize.valueOf(maxHeapSize));
        launcherSettings.minHeapSize.set(JavaHeapSize.valueOf(initialHeapSize));
        launcherSettings.closeLauncherAfterGameStart.set(Boolean.parseBoolean(closeLauncherAfterGameStart));
        launcherSettings.gameDirectory.set(gameDirectory);
        launcherSettings.gameDataDirectory.set(gameDataDirectory);
        launcherSettings.keepDownloadedFiles.set(Boolean.parseBoolean(saveDownloadedFiles));
        launcherSettings.userJavaParameters.setAll(userJavaParameters);
        launcherSettings.userGameParameters.setAll(userGameParameters);
        launcherSettings.logLevel.set(Level.valueOf(logLevel));

        assertPropertiesEqual();
    }

    @Test
    void canRecognizeLastGamePlayed() {
        final var displayVersion = "alpha-20";
        GameIdentifier id = new GameIdentifier(displayVersion, Build.NIGHTLY, Profile.OMEGA);
        // Second object so as not to rely on instance identity.
        GameIdentifier expectedId = new GameIdentifier(displayVersion, Build.NIGHTLY, Profile.OMEGA);

        launcherSettings.lastPlayedGameVersion.set(id);
        assertEquals(expectedId, launcherSettings.lastPlayedGameVersion.get());
    }
}
