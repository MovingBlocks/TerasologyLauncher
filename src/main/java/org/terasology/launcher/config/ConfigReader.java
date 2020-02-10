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

import com.google.gson.Gson;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link Service} to read a config file from the disk.
 * On success the {@link Config} instance is validated
 * and provided back to the {@link ConfigManager}.
 */
class ConfigReader extends Service<Config> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);

    private final Path configPath;
    private final Path launcherPath;
    private final Gson decoder;
    private final ConfigValidator validator;

    ConfigReader(final Path configPath, final Path launcherPath, final Gson decoder, final ConfigValidator validator) {
        this.configPath = configPath;
        this.launcherPath = launcherPath;
        this.decoder = decoder;
        this.validator = validator;
    }

    @Override
    protected Task<Config> createTask() {
        return new Task<Config>() {
            @Override
            protected Config call() throws IOException {
                if (Files.exists(configPath)) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(Files.newInputStream(configPath))
                    )) {
                        final Config config = decoder.fromJson(reader, Config.Builder.class)
                                .launcherDir(launcherPath)
                                .build();

                        return validator.validate(config);
                    }
                } else {
                    logger.warn("Config file was not found: {}", configPath);
                    cancel();
                }
                return null;
            }
        };
    }

    @Override
    protected void succeeded() {
        logger.debug("Finished reading config file: {}", configPath);
    }

    @Override
    protected void cancelled() {
        logger.debug("Cancelled reading config file: {}", configPath);
    }

    @Override
    protected void failed() {
        logger.error("Failed reading config file: {}", configPath);
    }
}
