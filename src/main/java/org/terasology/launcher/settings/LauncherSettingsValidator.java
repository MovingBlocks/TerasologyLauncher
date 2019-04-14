/*
 * Copyright 2019 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.JavaHeapSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides methods to check launcher settings and correct if invalid.
 */
public class LauncherSettingsValidator {
    private final List<SettingsValidationRule> validationRules;

    private static final Logger logger = LoggerFactory.getLogger(LauncherSettingsValidator.class);

    public LauncherSettingsValidator() {
        validationRules = new ArrayList<>();
        setupRules();
    }

    private void setupRules() {
        // Rule for max heap size
        SettingsValidationRule maxHeapSizeRule = new SettingsValidationRule(
                s -> !System.getProperty("os.arch").equals("x86")
                        || s.getMaxHeapSize().compareTo(JavaHeapSize.GB_1_5) < 0,
                "Max heap size cannot be greater than 1.5 GB for a 32-bit JVM",
                s -> s.setMaxHeapSize(JavaHeapSize.GB_1_5)
        );

        // Rule for initial heap size
        SettingsValidationRule initialHeapSizeRule = new SettingsValidationRule(
                s -> s.getInitialHeapSize().compareTo(s.getMaxHeapSize()) < 0,
                "Initial heap size cannot be greater than max heap size",
                s -> s.setInitialHeapSize(s.getMaxHeapSize())
        );

        // Add all rules to the list
        validationRules.addAll(Arrays.asList(
                maxHeapSizeRule, initialHeapSizeRule
        ));
    }

    public void validate(AbstractLauncherSettings settings) {
        for (SettingsValidationRule rule : validationRules) {
            if (rule.isBrokenBy(settings)) {
                logger.warn(rule.getInvalidationMessage());
                rule.correct(settings);
            }
        }
    }
}
