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
package org.terasology.launcher.updater;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.terasology.launcher.TestingUtils;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { DownloadUtils.class, LauncherUpdater.class })
public final class TestLancherUpdater {

    private static String BUILD_NUMBER;
    private static Constructor<TerasologyLauncherVersionInfo> propertiesConstructor;

    @BeforeClass
    public static void setupConstructor() throws Exception {
        propertiesConstructor = TerasologyLauncherVersionInfo.class.getDeclaredConstructor(Properties.class);
        propertiesConstructor.setAccessible(true);

        Field buildNumber = TerasologyLauncherVersionInfo.class.getDeclaredField("BUILD_NUMBER");
        buildNumber.setAccessible(true);
        BUILD_NUMBER = (String) buildNumber.get(null);
    }

    @Test
    public void testUpdateUnavailableInDevelopment() throws Exception {
        // Pass in null to ensure that 'versionInfo.properties' isn't accidentally used
        TerasologyLauncherVersionInfo info = TerasologyLauncherVersionInfo.loadFromInputStream(null);
        LauncherUpdater updater = this.getLauncherUpdater(1, info);

        assertFalse("Update should not be available!", updater.updateAvailable());
    }

    @Test
    public void testUpdateGreaterVersion() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(BUILD_NUMBER, "2");
        TerasologyLauncherVersionInfo info = this.createVersionWithProperties(properties);
        LauncherUpdater updater = this.getLauncherUpdater(3, info);

        assertTrue("Update should be available!", updater.updateAvailable());
    }

    @Test
    public void testUpdateLesserVersion() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(BUILD_NUMBER, "3");
        TerasologyLauncherVersionInfo info = this.createVersionWithProperties(properties);
        LauncherUpdater updater = this.getLauncherUpdater(1, info);

        assertFalse("Update should not be available!", updater.updateAvailable());
    }

    private TerasologyLauncherVersionInfo createVersionWithProperties(Properties properties) throws Exception {
        return propertiesConstructor.newInstance(properties);
    }

    private LauncherUpdater getLauncherUpdater(int buildNum, TerasologyLauncherVersionInfo info) throws Exception {
        TestingUtils.mockBuildVersion(DownloadUtils.TERASOLOGY_LAUNCHER_DEVELOP_JOB_NAME, buildNum);
        LauncherUpdater updater = PowerMockito.spy(new LauncherUpdater(info));

        // Simulates version info and changelog being unavailable
        PowerMockito.doNothing().when(updater, "setNewVersionInfo");
        PowerMockito.doNothing().when(updater, "setNewChangeLog");

        return updater;
    }
}

