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

/**
 * Provides methods to access configuration values and
 * supports reading and writing them to the disk. This
 * is a singleton class and its instance should be
 * accessed only by using the {@code get()} method.
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "config.json";
    private static volatile ConfigManager instance;

    private final Path launcherPath;
    private final Path configPath;
    private final ConfigValidator validator;
    private final Gson gson;
    private Config config;
    private final Service<Config> reader;
    private final Service<Void> writer;

    private ConfigManager() {
        // Prevent calling via reflection
        if (instance != null) {
            throw new RuntimeException("Cannot create second instance of a singleton class");
        }

        launcherPath = resolveLauncherDir();
        configPath = launcherPath.resolve(CONFIG_FILE);
        config = getDefaultConfigFor(launcherPath);
        gson = new GsonBuilder()
                .registerTypeAdapter(Path.class, new PathAdapter())
                .registerTypeAdapter(Package.class, new PackageAdapter())
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        validator = new ConfigValidator(config);

        reader = new ConfigReader(configPath, launcherPath, gson, validator);
        writer = new ConfigWriter(configPath, gson, config);
    }

    /**
     * Resolve the path to the launcher directory for host operating system.
     *
     * @return the absolute path to the launcher application directory
     */
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

    /**
     * The default configuration of the launcher with installation and data directories inside `launcherDir`.
     *
     * @param launcherDir the absolute path to launcher application directory
     * @return the default configuration
     */
    private Config getDefaultConfigFor(final Path launcherDir) {
        final GameConfig gameConfig = GameConfig.builder()
                .installDir(launcherDir.resolve("Terasology"))
                .dataDir(launcherDir.resolve("TerasologyData"))
                .maxMemory(JavaHeapSize.GB_1_5)
                .initMemory(JavaHeapSize.GB_1)
                .javaParam("-XX:+UseParNewGC"
                        + " -XX:+UseConcMarkSweepGC"
                        + " -XX:MaxGCPauseMillis=20"
                        + " -XX:ParallelGCThreads=10")
                .logLevel(LogLevel.DEFAULT)
                .build();

        return Config.builder()
                .gameConfig(gameConfig)
                .locale(Locale.ENGLISH)
                .launcherDir(launcherDir)
                .checkUpdatesOnLaunch(false)
                .cacheGamePackages(true)
                .closeAfterGameStarts(true)
                .build();
    }

    /**
     * Provides a reader service that can be used to read
     * configurations from the local config file. It should
     * be used only from the JavaFX Application thread.
     *
     * @return the reader service
     */
    public Service<Config> getReader() {
        return reader;
    }

    /**
     * Provides a writer service that can be used to write
     * configurations to the local config file. It should
     * be used only from the JavaFX Application thread.
     *
     * @return the writer service
     */
    public Service<Void> getWriter() {
        return writer;
    }

    Path getLauncherPath() {
        return launcherPath;
    }

    Path getConfigPath() {
        return configPath;
    }

    Gson getGson() {
        return gson;
    }

    /**
     * Provides an immutable {@link Config} instance.
     *
     * The initial default configuration may be changed by running the reader service.
     *
     * @return the current launcher configuration as {@link Config} instance
     */
    public Config getConfig() {
        return config;
    }

    void setConfig(final Config config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration 'config'  must not be 'null'!");
        }
        this.config = config;
    }

    /**
     * Provides the only instance of this class.
     *
     * @return the singleton instance
     */
    public static ConfigManager get() {
        // Double check locking
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
}
