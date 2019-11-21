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
import org.terasology.launcher.util.LauncherStartFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

class ConfigLoadService extends Service<LauncherConfig> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoadService.class);

    private final ConfigManager manager;
    private final ConfigValidator validator;

    ConfigLoadService(ConfigManager manager) {
        this.manager = manager;
        validator = new ConfigValidator();
    }

    @Override
    protected Task<LauncherConfig> createTask() {
        return new Task<LauncherConfig>() {
            @Override
            protected LauncherConfig call() throws Exception {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(manager.getConfigPath()))
                )) {
                    final LauncherConfig config = manager.getGson().fromJson(reader, LauncherConfig.class);
                    return validator.validate(config);
                } catch (IOException e) {
                    logger.error("Failed to read config file: {}", manager.getConfigPath());
                    throw new LauncherStartFailedException();
                }
            }
        };
    }

    @Override
    protected void succeeded() {
        manager.setConfig(getValue());
    }
}
