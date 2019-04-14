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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides methods to check settings value and correct invalid values.
 */
public class SettingsValidationRule {
    private final Predicate<AbstractLauncherSettings> condition;
    private final String invalidationMessage;
    private final Consumer<AbstractLauncherSettings> correction;

    public SettingsValidationRule(
            Predicate<AbstractLauncherSettings> condition,
            String invalidationMessage,
            Consumer<AbstractLauncherSettings> correction
    ) {
        this.condition = condition;
        this.invalidationMessage = invalidationMessage;
        this.correction = correction;
    }

    public boolean isBrokenBy(AbstractLauncherSettings settings) {
        return !condition.test(settings);
    }

    public String getInvalidationMessage() {
        return invalidationMessage;
    }

    public void correct(AbstractLauncherSettings settings) {
        correction.accept(settings);
    }
}
