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

/**
 * Provides validated configurations by resetting to
 * default values in case any configuration is found
 * invalid.
 */
class ConfigValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigValidator.class);

    private final Config fallbackConfiguration;

    ConfigValidator(final Config defaultConfig) {
        this.fallbackConfiguration = defaultConfig;
    }

    /**
     * Validates the configurations provided by a {@link Config}
     * instance and returns its validated rebuild. If any
     * configuration is found invalid, the default value for
     * that configuration is used while rebuilding the instance.
     *
     * @param config the original {@link Config} instance
     * @return the validated {@link Config} instance
     */
    Config validate(final Config config) {
        final GameConfig gameConfig = config.getGameConfig();
        return config.rebuilder()
                .gameConfig(gameConfig.rebuilder()
                        .maxMemory(validateMaxMemory(gameConfig))
                        .initMemory(validateInitMemory(gameConfig))
                        .build())
                .build();
    }

    /**
     * Checks if the maximum memory is not set to a value greater
     * than 1.5GB when the JVM is 32-bit.
     *
     * @param gameConfig the {@link GameConfig} instance to check from
     * @return the same max memory if valid else the default value
     */
    private JavaHeapSize validateMaxMemory(final GameConfig gameConfig) {
        final JavaHeapSize valid;
        if (!(System.getProperty("os.arch").equals("x86")
                && gameConfig.getMaxMemory().compareTo(JavaHeapSize.GB_1_5) > 0)
        ) {
            valid = gameConfig.getMaxMemory();
        } else {
            valid = fallbackConfiguration.getGameConfig().getMaxMemory();
            logger.warn("Max memory cannot be greater than 1.5 GB for a 32-bit JVM");
            logger.debug("Continuing with max memory: {}", valid);
        }
        return valid;
    }

    /**
     * Checks if the initial memory is set to a value lesser than
     * the maximum memory.
     *
     * @param gameConfig the {@link GameConfig} instance to check from
     * @return the same init memory if valid else the value of max memory
     */
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
