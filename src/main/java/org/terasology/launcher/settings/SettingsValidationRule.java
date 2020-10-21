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
