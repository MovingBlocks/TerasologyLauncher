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

package org.terasology.launcher.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.JavaHeapSize;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

class ConfigValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigValidator.class);
    private static final List<Rule> RULES = Arrays.asList(
            // TODO: Fix validation for immutable Config
            // Max heap size
            new Rule(
                    c -> !(System.getProperty("os.arch").equals("x86")
                            && c.getMaxMemory().compareTo(JavaHeapSize.GB_1_5) > 0),
                    "Max memory cannot be greater than 1.5 GB for a 32-bit JVM",
                    c -> c.setMaxMemory(JavaHeapSize.GB_1_5)
            ),

            // Initial heap size
            new Rule(
                    c -> c.getInitMemory().compareTo(c.getMaxMemory()) < 0,
                    "Initial memory cannot be greater than max heap size",
                    c -> c.setInitMemory(c.getMaxMemory())
            )
    );

    Config validate(Config config) {
        for (Rule rule : RULES) {
            if (rule.brokenBy(config)) {
                logger.error("Invalid configuration: {}", rule.getErrorMsg());
                rule.correct(config);
            }
        }
        return config;
    }

    private static class Rule {
        private final Predicate<Config> predicate;
        private final String errorMsg;
        private final Consumer<Config> correction;

        Rule(Predicate<Config> predicate,
             String errorMsg,
             Consumer<Config> correction
        ) {
            this.predicate = predicate;
            this.errorMsg = errorMsg;
            this.correction = correction;
        }

        boolean brokenBy(Config config) {
            return !predicate.test(config);
        }

        String getErrorMsg() {
            return errorMsg;
        }

        void correct(Config config) {
            correction.accept(config);
        }
    }
}
