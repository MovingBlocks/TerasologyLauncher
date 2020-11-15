// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Provides methods to check settings values and correct the invalid ones.
 */
public class SettingsValidationRule {
    private final Predicate<LauncherSettings> condition;
    private final String invalidationMessage;
    private final Consumer<LauncherSettings> correction;

    public SettingsValidationRule(
            Predicate<LauncherSettings> condition,
            String invalidationMessage,
            Consumer<LauncherSettings> correction
    ) {
        this.condition = condition;
        this.invalidationMessage = invalidationMessage;
        this.correction = correction;
    }

    public boolean isBrokenBy(LauncherSettings settings) {
        return !condition.test(settings);
    }

    public String getInvalidationMessage() {
        return invalidationMessage;
    }

    public void correct(LauncherSettings settings) {
        correction.accept(settings);
    }
}
