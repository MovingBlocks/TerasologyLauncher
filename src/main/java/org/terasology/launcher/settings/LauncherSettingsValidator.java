// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
                    s -> !(System.getProperty("os.arch").equals("x86") && s.maxHeapSize.get().compareTo(JavaHeapSize.GB_1_5) > 0),
                    "Max heap size cannot be greater than 1.5 GB for a 32-bit JVM",
                    s -> s.maxHeapSize.set(JavaHeapSize.GB_1_5)
            ),

            // Rule for initial heap size
            new SettingsValidationRule(
                    s -> s.minHeapSize.get().compareTo(s.maxHeapSize.get()) < 0,
                    "Initial heap size cannot be greater than max heap size",
                    s -> s.minHeapSize.set(s.maxHeapSize.get())
            ),

            new SettingsValidationRule(
                    s -> s.userJavaParameters.get().stream().anyMatch(DEPRECATED_PARAMETERS::contains),
                    "Ensure unsupported JVM arguments are removed",
                    s -> s.userJavaParameters.setAll(removeUnsupportedJvmParameters(s.userJavaParameters.get()))
            )
    );

    private LauncherSettingsValidator() {
    }

    private static List<String> removeUnsupportedJvmParameters(final List<String> params) {
        List<String> correctedParams = Lists.newArrayList(params);
        correctedParams.removeAll(DEPRECATED_PARAMETERS);
        return correctedParams;
    }

    /**
     * Validates an {@link LauncherSettings} instance against a list of rules.
     * Also applies a correction to the settings if it breaks any rule.
     *
     * @param settings the settings to be validated
     */
    public static void validate(Settings settings) {
        for (SettingsValidationRule rule : RULES) {
            if (rule.isBrokenBy(settings)) {
                logger.warn(rule.getInvalidationMessage());
                rule.correct(settings);
            }
        }
    }
}
