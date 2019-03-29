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

/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package org.terasology.launcher.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.JavaHeapSize;

import static org.terasology.launcher.settings.BaseLauncherSettings.PROPERTY_MAX_HEAP_SIZE;

/**
 * Provides methods to validate launcher settings
 */
public class LauncherSettingsValidator {
    private static final Logger logger = LoggerFactory.getLogger(LauncherSettingsValidator.class);

    public boolean validateMaxHeapSize(BaseLauncherSettings settings) {
        final JavaHeapSize currentMaxJavaHeapSize = settings.getMaxHeapSize();

        // Checks if JVM is 32 bit
        if (System.getProperty("sun.arch.data.model").equals("32")
                && currentMaxJavaHeapSize.compareTo(JavaHeapSize.GB_1_5) > 0) {

            logger.warn("Cannot set '{}' as '{}' for a 32-bit JVM.",
                    currentMaxJavaHeapSize.getSizeParameter(), PROPERTY_MAX_HEAP_SIZE);

            settings.setMaxHeapSize(JavaHeapSize.GB_1_5);

            logger.warn("Proceeding with '{}' as the '{}'.",
                    JavaHeapSize.GB_1_5.getSizeParameter(), PROPERTY_MAX_HEAP_SIZE);

            return true;
        }
        return false;
    }

    public boolean validateInitialHeapSize(BaseLauncherSettings settings) {
        final JavaHeapSize currentMaxJavaHeapSize = settings.getMaxHeapSize();

        if (settings.getInitialHeapSize().compareTo(currentMaxJavaHeapSize) > 0) {
            settings.setInitialHeapSize(currentMaxJavaHeapSize);
            return true;
        }
        return false;
    }
}
