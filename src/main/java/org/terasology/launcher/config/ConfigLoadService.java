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
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

class ConfigLoadService extends Service<LauncherConfig> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoadService.class);

    private final ConfigManager manager;
    private final LauncherConfig defaultConfig;
    private final ConfigValidator validator;

    ConfigLoadService(ConfigManager manager) {
        this.manager = manager;
        validator = new ConfigValidator();
        defaultConfig = createDefaultConfig();
    }

    @Override
    protected Task<LauncherConfig> createTask() {
        return new Task<LauncherConfig>() {
            @Override
            protected LauncherConfig call() {
                final Path configFile = manager.getConfigPath();
                if (Files.notExists(configFile)) {
                    logger.info("No config file was found. Proceeding with defaults.");
                    return defaultConfig;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(configFile))
                )) {
                    final LauncherConfig config = manager.getGson().fromJson(reader, LauncherConfig.class);
                    config.setLauncherDir(defaultConfig.getLauncherDir());
                    return validator.validate(config);
                } catch (IOException e) {
                    logger.error("Failed to read config file: {}", configFile);
                    logger.warn("Using default configurations");
                    return defaultConfig;
                }
            }
        };
    }

    @Override
    protected void succeeded() {
        logger.info("Loaded config file: {}", manager.getConfigPath());
        manager.setConfig(getValue());
    }

    private LauncherConfig createDefaultConfig() {
        final Path launcherDir = manager.getLauncherDir();
        final LauncherConfig config = new LauncherConfig();

        config.setInstallDir(launcherDir.resolve("Terasology"));
        config.setDataDir(config.getInstallDir());
        config.setMaxMemory(JavaHeapSize.GB_2);
        config.setInitMemory(JavaHeapSize.GB_1);
        config.setJavaParam("-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:MaxGCPauseMillis=20 -XX:ParallelGCThreads=10");
        config.setGameParam("");
        config.setLogLevel(LogLevel.DEFAULT);
        config.setLocale(Locale.ENGLISH);
        config.setLauncherDir(launcherDir);
        config.setCheckUpdatesOnLaunch(false);
        config.setCacheGamePackages(true);
        config.setCloseAfterGameStart(true);
        config.setSelectedPackage(null);

        return validator.validate(config);
    }
}
