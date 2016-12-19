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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
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
    private File testPropertiesFile;
    private File tempLauncherDirectory;

    private String LOCALE;
    private String JOB;
    private String MAX_HEAP_SIZE;
    private String INITIAL_HEAP_SIZE;
    private String BUILD_VERSION;
    private String LAST_BUILD_NUMBER;
    private String SEARCH_FOR_LAUNCHER_UPDATES;
    private String CLOSE_LAUNCHER_AFTER_GAME_START;
    private String GAME_DIRECTORY;
    private String GAME_DATA_DIRECTORY;
    private String SAVE_DOWNLOADED_FILES;
    private String USER_JAVA_PARAMETERS;
    private String USER_GAME_PARAMETERS;
    private String LOG_LEVEL;

    private void assertPropertiesEqual() throws Exception {
        assertEquals(baseLauncherSettings.getLocale(), Locale.forLanguageTag(LOCALE));
        assertEquals(baseLauncherSettings.getJob(), GameJob.valueOf(JOB));
        assertEquals(baseLauncherSettings.getMaxHeapSize(), JavaHeapSize.valueOf(MAX_HEAP_SIZE));
        assertEquals(baseLauncherSettings.getInitialHeapSize(), JavaHeapSize.valueOf(INITIAL_HEAP_SIZE));
        assertEquals(baseLauncherSettings.getBuildVersion(baseLauncherSettings.getJob()), new Integer(BUILD_VERSION));
        assertEquals(baseLauncherSettings.getLastBuildNumber(baseLauncherSettings.getJob()), new Integer(LAST_BUILD_NUMBER));
        assertEquals(baseLauncherSettings.isSearchForLauncherUpdates(), Boolean.valueOf(SEARCH_FOR_LAUNCHER_UPDATES));
        assertEquals(baseLauncherSettings.isCloseLauncherAfterGameStart(), Boolean.valueOf(CLOSE_LAUNCHER_AFTER_GAME_START));
        assertEquals(baseLauncherSettings.getGameDirectory(), new File(new URI(GAME_DIRECTORY)));
        assertEquals(baseLauncherSettings.getGameDataDirectory(), new File(new URI(GAME_DATA_DIRECTORY)));
        assertEquals(baseLauncherSettings.isKeepDownloadedFiles(), Boolean.valueOf(SAVE_DOWNLOADED_FILES));
        assertEquals(baseLauncherSettings.getUserJavaParameters(), USER_JAVA_PARAMETERS);
        assertEquals(baseLauncherSettings.getUserGameParameters(), USER_GAME_PARAMETERS);
        assertEquals(baseLauncherSettings.getLogLevel(), LogLevel.valueOf(LOG_LEVEL));
    }

    @Before
    public void setup() throws Exception {
        tempLauncherDirectory = tempDirectory.newFolder();
        testPropertiesFile = new File(tempLauncherDirectory, BaseLauncherSettings.LAUNCHER_SETTINGS_FILE_NAME);

        baseLauncherSettings = new BaseLauncherSettings(tempLauncherDirectory);
    }

    @Test
    public void testInitWithValues() throws Exception {
        //initialise properties with sample values
        LOCALE = "en";
        JOB = "TerasologyStable";
        MAX_HEAP_SIZE = "GB_2_5";
        INITIAL_HEAP_SIZE = "GB_1_5";
        BUILD_VERSION = String.valueOf(GameJob.valueOf(JOB).getMinBuildNumber() + 1);
        LAST_BUILD_NUMBER = String.valueOf(GameJob.valueOf(JOB).getMinBuildNumber());
        SEARCH_FOR_LAUNCHER_UPDATES = "false";
        CLOSE_LAUNCHER_AFTER_GAME_START = "false";
        GAME_DIRECTORY = tempDirectory.newFolder().toURI().toString();
        GAME_DATA_DIRECTORY = tempDirectory.newFolder().toURI().toString();
        SAVE_DOWNLOADED_FILES = "false";
        USER_JAVA_PARAMETERS = "-XXnoSystemGC";
        USER_GAME_PARAMETERS = "-headless";
        LOG_LEVEL = "DEBUG";

        //set properties
        testProperties = new Properties();
        testProperties.setProperty(PROPERTY_LOCALE, LOCALE);
        testProperties.setProperty(PROPERTY_JOB, JOB);
        testProperties.setProperty(PROPERTY_MAX_HEAP_SIZE, MAX_HEAP_SIZE);
        testProperties.setProperty(PROPERTY_INITIAL_HEAP_SIZE, INITIAL_HEAP_SIZE);
        testProperties.setProperty(PROPERTY_PREFIX_BUILD_VERSION + JOB, BUILD_VERSION);
        testProperties.setProperty(PROPERTY_PREFIX_LAST_BUILD_NUMBER + JOB, LAST_BUILD_NUMBER);
        testProperties.setProperty(PROPERTY_SEARCH_FOR_LAUNCHER_UPDATES, SEARCH_FOR_LAUNCHER_UPDATES);
        testProperties.setProperty(PROPERTY_CLOSE_LAUNCHER_AFTER_GAME_START, CLOSE_LAUNCHER_AFTER_GAME_START);
        testProperties.setProperty(PROPERTY_GAME_DIRECTORY, GAME_DIRECTORY);
        testProperties.setProperty(PROPERTY_GAME_DATA_DIRECTORY, GAME_DATA_DIRECTORY);
        testProperties.setProperty(PROPERTY_SAVE_DOWNLOADED_FILES, SAVE_DOWNLOADED_FILES);
        testProperties.setProperty(PROPERTY_USER_JAVA_PARAMETERS, USER_JAVA_PARAMETERS);
        testProperties.setProperty(PROPERTY_USER_GAME_PARAMETERS, USER_GAME_PARAMETERS);
        testProperties.setProperty(PROPERTY_LOG_LEVEL, LOG_LEVEL);

        //store in properties file
        testProperties.store(new FileOutputStream(testPropertiesFile), null);

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
        assertEquals(baseLauncherSettings.isKeepDownloadedFiles(), Boolean.valueOf(SAVE_DOWNLOADED_FILES));
        assertEquals(baseLauncherSettings.getUserJavaParameters(), BaseLauncherSettings.USER_JAVA_PARAMETERS_DEFAULT);
        assertEquals(baseLauncherSettings.getUserGameParameters(), BaseLauncherSettings.USER_GAME_PARAMETERS_DEFAULT);
        assertEquals(baseLauncherSettings.getLogLevel(), LogLevel.DEFAULT);
    }

    @Test
    public void testSetters() throws Exception {
        //re-initialise properties with sample values
        LOCALE = "fr";
        JOB = "Terasology";
        MAX_HEAP_SIZE = "GB_4";
        INITIAL_HEAP_SIZE = "GB_3";
        BUILD_VERSION = String.valueOf(GameJob.valueOf(JOB).getMinBuildNumber() + 1);
        LAST_BUILD_NUMBER = String.valueOf(GameJob.valueOf(JOB).getMinBuildNumber());
        SEARCH_FOR_LAUNCHER_UPDATES = "true";
        CLOSE_LAUNCHER_AFTER_GAME_START = "true";
        GAME_DIRECTORY = tempDirectory.newFolder().toURI().toString();
        GAME_DATA_DIRECTORY = tempDirectory.newFolder().toURI().toString();
        SAVE_DOWNLOADED_FILES = "true";
        USER_JAVA_PARAMETERS = "-XXUseParNewGC -XXUseConcMarkSweepGC";
        USER_GAME_PARAMETERS = "-noCrashReport";
        LOG_LEVEL = "INFO";

        //set using setters
        baseLauncherSettings.setLocale(Locale.forLanguageTag(LOCALE));
        baseLauncherSettings.setJob(GameJob.valueOf(JOB));
        baseLauncherSettings.setMaxHeapSize(JavaHeapSize.valueOf(MAX_HEAP_SIZE));
        baseLauncherSettings.setInitialHeapSize(JavaHeapSize.valueOf(INITIAL_HEAP_SIZE));
        baseLauncherSettings.setBuildVersion(Integer.parseInt(BUILD_VERSION), GameJob.valueOf(JOB));
        baseLauncherSettings.setLastBuildNumber(Integer.parseInt(LAST_BUILD_NUMBER), GameJob.valueOf(JOB));
        baseLauncherSettings.setSearchForLauncherUpdates(Boolean.valueOf(SEARCH_FOR_LAUNCHER_UPDATES));
        baseLauncherSettings.setCloseLauncherAfterGameStart(Boolean.valueOf(CLOSE_LAUNCHER_AFTER_GAME_START));
        baseLauncherSettings.setGameDirectory(new File(new URI(GAME_DIRECTORY)));
        baseLauncherSettings.setGameDataDirectory(new File(new URI(GAME_DATA_DIRECTORY)));
        baseLauncherSettings.setKeepDownloadedFiles(Boolean.valueOf(SAVE_DOWNLOADED_FILES));
        baseLauncherSettings.setUserJavaParameters(USER_JAVA_PARAMETERS);
        baseLauncherSettings.setUserGameParameters(USER_GAME_PARAMETERS);
        baseLauncherSettings.setLogLevel(LogLevel.valueOf(LOG_LEVEL));

        assertPropertiesEqual();
    }
}
