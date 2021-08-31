// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Provides methods to check settings values and correct the invalid ones.
 */
public class SettingsValidationRule {
    private final Predicate<LegacyLauncherSettings> condition;
    private final String invalidationMessage;
    private final Consumer<LegacyLauncherSettings> correction;

    public SettingsValidationRule(
            Predicate<LegacyLauncherSettings> condition,
            String invalidationMessage,
            Consumer<LegacyLauncherSettings> correction
    ) {
        this.condition = condition;
        this.invalidationMessage = invalidationMessage;
        this.correction = correction;
    }

    public boolean isBrokenBy(LegacyLauncherSettings settings) {
        return !condition.test(settings);
    }

    public String getInvalidationMessage() {
        return invalidationMessage;
    }

    public void correct(LegacyLauncherSettings settings) {
        correction.accept(settings);
    }
}
