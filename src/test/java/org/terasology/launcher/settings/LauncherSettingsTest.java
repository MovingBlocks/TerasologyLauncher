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

import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.terasology.launcher.util.Languages;


import static org.junit.Assert.assertEquals;

/**
 * Created by Skaldarnar on 30.01.2016.
 */
public class LauncherSettingsTest {

    protected BaseLauncherSettings baseLauncherSettings;
    protected LauncherSettings settings;

    @Before
    public void setUp() throws Exception {
        baseLauncherSettings = new BaseLauncherSettings(null);
        baseLauncherSettings.properties.clear();

        baseLauncherSettings.properties.setProperty(BaseLauncherSettings.PROPERTY_LOCALE, Locale.ENGLISH.toLanguageTag());

        settings = new LauncherSettingsDecorator(baseLauncherSettings);
    }

    @Test
    public void testGetUndecoratedLocale() throws Exception {
        assertEquals(Languages.DEFAULT_LOCALE, settings.getLocale());
    }

    @Test
    public void testSetDecoratorLocale() throws Exception {
        assertEquals(baseLauncherSettings.getLocale(), settings.getLocale());
        settings.setLocale(Locale.GERMAN);
        assertEquals(Locale.GERMAN, settings.getLocale());
    }

    @Test
    public void testDeleteDecoratorLocale() throws Exception {
        settings.setLocale(Locale.GERMAN);
        settings.setLocale(null);
        assertEquals(baseLauncherSettings.getLocale(), settings.getLocale());

    }
}