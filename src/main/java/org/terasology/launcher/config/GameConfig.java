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

import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.nio.file.Path;
import java.util.Objects;

public final class GameConfig {
    private final Path installDir;
    private final Path dataDir;
    private final JavaHeapSize maxMemory;
    private final JavaHeapSize initMemory;
    private final String javaParam;
    private final String gameParam;
    private final LogLevel logLevel;

    private GameConfig(Builder builder) {
        this.installDir = builder.installDir;
        this.dataDir = builder.dataDir;
        this.maxMemory = builder.maxMemory;
        this.initMemory = builder.initMemory;
        this.javaParam = builder.javaParam;
        this.gameParam = builder.gameParam;
        this.logLevel = builder.logLevel;
    }

    public Path getInstallDir() {
        return installDir;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public JavaHeapSize getMaxMemory() {
        return maxMemory;
    }

    public JavaHeapSize getInitMemory() {
        return initMemory;
    }

    public String getJavaParam() {
        return javaParam;
    }

    public String getGameParam() {
        return gameParam;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Nested builder class to create {@link GameConfig} instances
     * using descriptive methods.
     */
    public static final class Builder {
        private Path installDir;
        private Path dataDir;
        private JavaHeapSize maxMemory;
        private JavaHeapSize initMemory;
        private String javaParam;
        private String gameParam;
        private LogLevel logLevel;

        private Builder() {
            javaParam = "";
            gameParam = "";
        }

        public Builder installDir(Path newInstallDir) {
            installDir = newInstallDir;
            return this;
        }

        public Builder dataDir(final Path newDataDir) {
            dataDir = newDataDir;
            return this;
        }

        public Builder maxMemory(final JavaHeapSize newMaxMemory) {
            maxMemory = newMaxMemory;
            return this;
        }

        public Builder initMemory(final JavaHeapSize newInitMemory) {
            initMemory = newInitMemory;
            return this;
        }

        public Builder javaParam(final String newJavaParam) {
            javaParam = (newJavaParam != null) ? newJavaParam : "";
            return this;
        }

        public Builder gameParam(final String newGameParam) {
            gameParam = (newGameParam != null) ? newGameParam : "";
            return this;
        }

        public Builder logLevel(final LogLevel newLogLevel) {
            logLevel = newLogLevel;
            return this;
        }

        public GameConfig build() {
            Objects.requireNonNull(installDir, "installDir must not be null");
            Objects.requireNonNull(dataDir, "dataDir must not be null");
            Objects.requireNonNull(maxMemory, "maxMemory must not be null");
            Objects.requireNonNull(initMemory, "initMemory must not be null");
            Objects.requireNonNull(logLevel, "logLevel must not be null");

            return new GameConfig(this);
        }
    }
}
