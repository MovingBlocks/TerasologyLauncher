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

class ConfigValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigValidator.class);

    private final Config defaultConfig;

    ConfigValidator(final Config defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    Config validate(final Config config) {
        final GameConfig gameConfig = config.getGameConfig();
        return config.rebuilder()
                .gameConfig(gameConfig.rebuilder()
                        .maxMemory(validateMaxMemory(gameConfig))
                        .initMemory(validateInitMemory(gameConfig))
                        .build())
                .build();
    }

    private JavaHeapSize validateMaxMemory(final GameConfig gameConfig) {
        final JavaHeapSize valid;
        if (!(System.getProperty("os.arch").equals("x86")
                && gameConfig.getMaxMemory().compareTo(JavaHeapSize.GB_1_5) > 0)
        ) {
            valid = gameConfig.getMaxMemory();
        } else {
            valid = defaultConfig.getGameConfig().getMaxMemory();
            logger.warn("Max memory cannot be greater than 1.5 GB for a 32-bit JVM");
            logger.debug("Continuing with max memory: {}", valid);
        }
        return valid;
    }

    private JavaHeapSize validateInitMemory(final GameConfig gameConfig) {
        final JavaHeapSize valid;
        if (gameConfig.getInitMemory().compareTo(gameConfig.getMaxMemory()) < 0) {
            valid = gameConfig.getInitMemory();
        } else {
            valid = gameConfig.getMaxMemory();
            logger.warn("Initial heap size cannot be greater than max heap size");
            logger.debug("Continuing with init memory: {}", valid);
        }
        return valid;
    }
}
