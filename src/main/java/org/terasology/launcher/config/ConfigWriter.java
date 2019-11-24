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

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link Service} to write the current configurations
 * to a config file on the disk.
 */
class ConfigWriter extends Service<Void> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigWriter.class);

    private final ConfigManager manager;
    private final Path configPath;

    ConfigWriter(ConfigManager manager) {
        this.manager = manager;
        configPath = manager.getConfigPath();
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(Files.newOutputStream(configPath))
                )) {
                    manager.getGson().toJson(manager.getConfig(), Config.class, writer);
                }
                return null;
            }
        };
    }

    @Override
    protected void succeeded() {
        logger.debug("Finished writing config file: {}", configPath);
    }

    @Override
    protected void cancelled() {
        logger.debug("Cancelled writing config file: {}", configPath);
    }

    @Override
    protected void failed() {
        logger.error("Failed writing config file: {}", configPath);
    }
}
