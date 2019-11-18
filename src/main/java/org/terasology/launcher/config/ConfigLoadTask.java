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
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.LauncherDirectoryUtils;
import org.terasology.launcher.util.LauncherStartFailedException;
import org.terasology.launcher.util.OperatingSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoadTask extends Task<LauncherConfig> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoadTask.class);
    private static final String CONFIG_FILE = "config.json";

    private final Gson gson = new Gson();

    @Override
    protected LauncherConfig call() throws Exception {
        final OperatingSystem os = OperatingSystem.getOS();
        if (os == OperatingSystem.UNKNOWN) {
            logger.error("Unsupported OS: {} {} {}",
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.arch"));
            throw new LauncherStartFailedException();
        }

        final Path launcherDir = LauncherDirectoryUtils.getApplicationDirectory(
                os, LauncherDirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
        final Path configPath = launcherDir.resolve(CONFIG_FILE);
        // TODO: Use local methods for all stuff above

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(configPath))
        )) {
            return gson.fromJson(reader, LauncherConfig.class);
        } catch (IOException e) {
            logger.error("Failed to read config file: {}", configPath);
            throw new LauncherStartFailedException();
        }
    }
}
