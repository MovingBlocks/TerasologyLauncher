/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.settings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.util.LogLevel;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_GAME_DATA_DIRECTORY;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_GAME_DIRECTORY;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_INITIAL_HEAP_SIZE;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_JOB;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_LOCALE;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_LOG_LEVEL;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_MAX_HEAP_SIZE;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_PREFIX_BUILD_VERSION;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_PREFIX_LAST_BUILD_NUMBER;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_SAVE_DOWNLOADED_FILES;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_USER_GAME_PARAMETERS;
import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_USER_JAVA_PARAMETERS;

public class TestBaseLauncherSettings {
    @Rule
    public TemporaryFolder tempDirectory = new TemporaryFolder();

    private BaseLauncherSettings baseLauncherSettings;
    private Properties testProperties;
    private Path testPropertiesFile;
    private Path tempLauncherDirectory;

    private String locale;
    private String job;
    private String maxHeapSize;
    private String initialHeapSize;
    private String buildVersion;
    private String lastBuildNumber;
    private String searchForLauncherUpdates;
    private String closeLauncherAfterGameStart;
    private String gameDirectory;
    private String gameDataDirectory;
    private String saveDownloadedFiles;
    private String userJavaParameters;
    private String userGameParameters;
    private String logLevel;

    private void assertPropertiesEqual() throws Exception {
        assertEquals(baseLauncherSettings.getLocale(), Locale.forLanguageTag(locale));
        assertEquals(baseLauncherSettings.getJob(), GameJob.valueOf(job));
        assertEquals(baseLauncherSettings.getMaxHeapSize(), JavaHeapSize.valueOf(maxHeapSize));
        assertEquals(baseLauncherSettings.getInitialHeapSize(), JavaHeapSize.valueOf(initialHeapSize));
        assertEquals(baseLauncherSettings.getBuildVersion(baseLauncherSettings.getJob()), new Integer(buildVersion));
        assertEquals(baseLauncherSettings.getLastBuildNumber(baseLauncherSettings.getJob()), new Integer(lastBuildNumber));
        assertEquals(baseLauncherSettings.isSearchForLauncherUpdates(), Boolean.valueOf(searchForLauncherUpdates));
        assertEquals(baseLauncherSettings.isCloseLauncherAfterGameStart(), Boolean.valueOf(closeLauncherAfterGameStart));
        assertEquals(baseLauncherSettings.getGameDirectory(), Paths.get(new URI(gameDirectory)));
        assertEquals(baseLauncherSettings.getGameDataDirectory(), Paths.get(new URI(gameDataDirectory)));
        assertEquals(baseLauncherSettings.isKeepDownloadedFiles(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(baseLauncherSettings.getUserJavaParameters(), userJavaParameters);
        assertEquals(baseLauncherSettings.getUserGameParameters(), userGameParameters);
        assertEquals(baseLauncherSettings.getLogLevel(), LogLevel.valueOf(logLevel));
    }

    @Before
    public void setup() throws Exception {
        tempLauncherDirectory = tempDirectory.newFolder().toPath();
        testPropertiesFile = tempLauncherDirectory.resolve(BaseLauncherSettings.LAUNCHER_SETTINGS_FILE_NAME);

        baseLauncherSettings = new BaseLauncherSettings(tempLauncherDirectory);
    }

    @Test
    public void testInitWithValues() throws Exception {
        //initialise properties with sample values
        locale = "en";
        job = "TerasologyStable";
        maxHeapSize = "GB_2_5";
        initialHeapSize = "GB_1_5";
        buildVersion = String.valueOf(GameJob.valueOf(job).getMinBuildNumber() + 1);
        lastBuildNumber = String.valueOf(GameJob.valueOf(job).getMinBuildNumber());
        searchForLauncherUpdates = "false";
        closeLauncherAfterGameStart = "false";
        gameDirectory = tempDirectory.newFolder().toURI().toString();
        gameDataDirectory = tempDirectory.newFolder().toURI().toString();
        saveDownloadedFiles = "false";
        userJavaParameters = "-XXnoSystemGC";
        userGameParameters = "-headless";
        logLevel = "DEBUG";

        //set properties
        testProperties = new Properties();
        testProperties.setProperty(PROPERTY_LOCALE, locale);
        testProperties.setProperty(PROPERTY_JOB, job);
        testProperties.setProperty(PROPERTY_MAX_HEAP_SIZE, maxHeapSize);
        testProperties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, initialHeapSize);
        testProperties.setProperty(PROPERTY_PREFIX_BUILD_VERSION + job, buildVersion);
        testProperties.setProperty(PROPERTY_PREFIX_LAST_BUILD_NUMBER + job, lastBuildNumber);
        testProperties.setProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES, searchForLauncherUpdates);
        testProperties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, closeLauncherAfterGameStart);
        testProperties.setProperty(PROPERTY_GAME_DIRECTORY, gameDirectory);
        testProperties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, gameDataDirectory);
        testProperties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, saveDownloadedFiles);
        testProperties.setProperty(PROPERTY_USER_JAVA_PARAMETERS, userJavaParameters);
        testProperties.setProperty(PROPERTY_USER_GAME_PARAMETERS, userGameParameters);
        testProperties.setProperty(PROPERTY_LOG_LEVEL, logLevel);

        //store in properties file
        try (OutputStream output = Files.newOutputStream(testPropertiesFile)) {
            testProperties.store(output, null);
        }

        baseLauncherSettings.load();
        baseLauncherSettings.init();
        assertPropertiesEqual();
    }

    @Test
    public void testInitDefault() throws Exception {
        //null properties file

        baseLauncherSettings.load();
        baseLauncherSettings.init();

        assertEquals(baseLauncherSettings.getLocale(), Languages.DEFAULT_LOCALE);
        assertEquals(baseLauncherSettings.getJob(), BaseLauncherSettings.JOB_DEFAULT);
        assertEquals(baseLauncherSettings.getMaxHeapSize(), BaseLauncherSettings.MAX_HEAP_SIZE_DEFAULT);
        assertEquals(baseLauncherSettings.getInitialHeapSize(), BaseLauncherSettings.INITIAL_HEAP_SIZE_DEFAULT);
        assertEquals(baseLauncherSettings.getBuildVersion(baseLauncherSettings.getJob()), new Integer(TerasologyGameVersion.BUILD_VERSION_LATEST));
        assertEquals(baseLauncherSettings.getLastBuildNumber(baseLauncherSettings.getJob()), null);
        assertEquals(baseLauncherSettings.isSearchForLauncherUpdates(), BaseLauncherSettings.SEARCH_FOR_LAUNCHER_UPDATES_DEFAULT);
        assertEquals(baseLauncherSettings.isCloseLauncherAfterGameStart(), BaseLauncherSettings.CLOSE_LAUNCHER_AFTER_GAME_START_DEFAULT);
        assertEquals(baseLauncherSettings.getGameDirectory(), null);
        assertEquals(baseLauncherSettings.getGameDataDirectory(), null);
        assertEquals(baseLauncherSettings.isKeepDownloadedFiles(), Boolean.valueOf(saveDownloadedFiles));
        assertEquals(baseLauncherSettings.getUserJavaParameters(), BaseLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        assertEquals(baseLauncherSettings.getUserGameParameters(), BaseLauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        assertEquals(baseLauncherSettings.getLogLevel(), LogLevel.DEFAULT);
    }

    @Test
    public void testSetters() throws Exception {
        //re-initialise properties with sample values
        locale = "fr";
        job = "Terasology";
        maxHeapSize = "GB_4";
        initialHeapSize = "GB_3";
        buildVersion = String.valueOf(GameJob.valueOf(job).getMinBuildNumber() + 1);
        lastBuildNumber = String.valueOf(GameJob.valueOf(job).getMinBuildNumber());
        searchForLauncherUpdates = "true";
        closeLauncherAfterGameStart = "true";
        gameDirectory = tempDirectory.newFolder().toURI().toString();
        gameDataDirectory = tempDirectory.newFolder().toURI().toString();
        saveDownloadedFiles = "true";
        userJavaParameters = "-XXUseParNewGC -XXUseConcMarkSweepGC";
        userGameParameters = "-noCrashReport";
        logLevel = "INFO";

        //set using setters
        baseLauncherSettings.setLocale(Locale.forLanguageTag(locale));
        baseLauncherSettings.setJob(GameJob.valueOf(job));
        baseLauncherSettings.setMaxHeapSize(JavaHeapSize.valueOf(maxHeapSize));
        baseLauncherSettings.setInitialHeapSize(JavaHeapSize.valueOf(initialHeapSize));
        baseLauncherSettings.setBuildVersion(Integer.parseInt(buildVersion), GameJob.valueOf(job));
        baseLauncherSettings.setLastBuildNumber(Integer.parseInt(lastBuildNumber), GameJob.valueOf(job));
        baseLauncherSettings.setSearchForLauncherUpdates(Boolean.valueOf(searchForLauncherUpdates));
        baseLauncherSettings.setCloseLauncherAfterGameStart(Boolean.valueOf(closeLauncherAfterGameStart));
        baseLauncherSettings.setGameDirectory(Paths.get(new URI(gameDirectory)));
        baseLauncherSettings.setGameDataDirectory(Paths.get(new URI(gameDataDirectory)));
        baseLauncherSettings.setKeepDownloadedFiles(Boolean.valueOf(saveDownloadedFiles));
        baseLauncherSettings.setUserJavaParameters(userJavaParameters);
        baseLauncherSettings.setUserGameParameters(userGameParameters);
        baseLauncherSettings.setLogLevel(LogLevel.valueOf(logLevel));

        assertPropertiesEqual();
    }
}
