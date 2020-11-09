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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.JavaHeapSize;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Provides methods to check launcher settings and correct if invalid.
 */
public final class LauncherSettingsValidator {
    private static final Logger logger = LoggerFactory.getLogger(LauncherSettingsValidator.class);

    private static final Set<String> DEPRECATED_PARAMETERS =
            Sets.newHashSet("-XX:+UseParNewGC", "-XX:+UseConcMarkSweepGC", "-XX:ParallelGCThreads=10");

    private static final List<SettingsValidationRule> RULES = Arrays.asList(
            // Rule for max heap size
            new SettingsValidationRule(
                    s -> !(System.getProperty("os.arch").equals("x86") && s.getMaxHeapSize().compareTo(JavaHeapSize.GB_1_5) > 0),
                    "Max heap size cannot be greater than 1.5 GB for a 32-bit JVM",
                    s -> s.setMaxHeapSize(JavaHeapSize.GB_1_5)
            ),

            // Rule for initial heap size
            new SettingsValidationRule(
                    s -> s.getInitialHeapSize().compareTo(s.getMaxHeapSize()) < 0,
                    "Initial heap size cannot be greater than max heap size",
                    s -> s.setInitialHeapSize(s.getMaxHeapSize())
            ),

            new SettingsValidationRule(
                    s -> s.getUserGameParameterList().stream().anyMatch(DEPRECATED_PARAMETERS::contains),
                    "Ensure unsupported JVM arguments are removed",
                    s -> s.setUserJavaParameters(removeUnsupportedJvmParameters(s.getJavaParameterList()))
            )
    );

    private LauncherSettingsValidator() {
    }

    private static String removeUnsupportedJvmParameters(final List<String> params) {
        List<String> correctedParams = Lists.newArrayList(params);
        correctedParams.removeAll(DEPRECATED_PARAMETERS);
        return String.join(" ", correctedParams);
    }

    /**
     * Validates an {@link LauncherSettings} instance against a list of rules.
     * Also applies a correction to the settings if it breaks any rule.
     *
     * @param settings the settings to be validated
     */
    public static void validate(LauncherSettings settings) {
        for (SettingsValidationRule rule : RULES) {
            if (rule.isBrokenBy(settings)) {
                logger.warn(rule.getInvalidationMessage());
                rule.correct(settings);
            }
        }
    }
}
