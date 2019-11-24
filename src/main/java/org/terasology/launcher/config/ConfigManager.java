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
import com.google.gson.GsonBuilder;
import javafx.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.packages.Package;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LauncherDirectoryUtils;
import org.terasology.launcher.util.LogLevel;
import org.terasology.launcher.util.OperatingSystem;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "config.json";

    private final Path launcherDir;
    private final Path configPath;
    private final Gson gson;
    private final Config defaultConfig;
    private Config config;
    private final Service<Config> reader;
    private final Service<Void> writer;

    public ConfigManager() {
        launcherDir = resolveLauncherDir();
        configPath = launcherDir.resolve(CONFIG_FILE);
        gson = new GsonBuilder()
                .registerTypeAdapter(Path.class, new PathAdapter())
                .registerTypeAdapter(Package.class, new PackageAdapter())
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        defaultConfig = createDefaultConfig();

        reader = new ConfigReader(this);
        writer = new ConfigWriter(this);
    }

    private Path resolveLauncherDir() {
        final OperatingSystem os = OperatingSystem.getOS();
        if (os == OperatingSystem.UNKNOWN) {
            logger.error("Unsupported OS: {} {} {}",
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.arch"));
        }

        return LauncherDirectoryUtils.getApplicationDirectory(
                os, LauncherDirectoryUtils.LAUNCHER_APPLICATION_DIR_NAME);
        // TODO: Use local methods for all stuff above
    }

    private Config createDefaultConfig() {
        return Config.builder()
                .gameConfig(GameConfig.builder()
                        .installDir(launcherDir.resolve("Terasology"))
                        .dataDir(launcherDir.resolve("TerasologyData"))
                        .maxMemory(JavaHeapSize.GB_1_5)
                        .initMemory(JavaHeapSize.GB_1)
                        .javaParam("-XX:+UseParNewGC"
                                + " -XX:+UseConcMarkSweepGC"
                                + " -XX:MaxGCPauseMillis=20"
                                + " -XX:ParallelGCThreads=10")
                        .logLevel(LogLevel.DEFAULT)
                        .build())
                .locale(Locale.ENGLISH)
                .launcherDir(launcherDir)
                .checkUpdatesOnLaunch(false)
                .cacheGamePackages(true)
                .closeAfterGameStarts(true)
                .build();
    }

    public Service<Config> getReader() {
        return reader;
    }

    public Service<Void> getWriter() {
        return writer;
    }

    Path getLauncherDir() {
        return launcherDir;
    }

    Path getConfigPath() {
        return configPath;
    }

    Gson getGson() {
        return gson;
    }

    public Config getDefaultConfig() {
        return defaultConfig;
    }

    public Optional<Config> getConfig() {
        return Optional.ofNullable(config);
    }

    void setConfig(Config config) {
        this.config = config;
    }
}
