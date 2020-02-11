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

import org.terasology.launcher.packages.Package;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Stores all application-wide configuration values.
 * It is immutable and can be instantiated only with
 * the help of a {@link Builder} instance.
 */
public final class Config {
    private final GameConfig gameConfig;
    private final Locale locale;
    private final transient Path launcherDir;
    private final boolean checkUpdatesOnLaunch;
    private final boolean closeAfterGameStarts;
    private final boolean cacheGamePackages;
    private final Package selectedPackage;

    private Config(final Builder builder) {
        this.gameConfig = builder.gameConfig;
        this.locale = builder.locale;
        this.launcherDir = builder.launcherDir;
        this.checkUpdatesOnLaunch = builder.checkUpdatesOnLaunch;
        this.closeAfterGameStarts = builder.closeAfterGameStarts;
        this.cacheGamePackages = builder.cacheGamePackages;
        this.selectedPackage = builder.selectedPackage;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public Locale getLocale() {
        return locale;
    }

    public Path getLauncherDir() {
        return launcherDir;
    }

    public boolean isCheckUpdatesOnLaunch() {
        return checkUpdatesOnLaunch;
    }

    public boolean isCloseAfterGameStarts() {
        return closeAfterGameStarts;
    }

    public boolean isCacheGamePackages() {
        return cacheGamePackages;
    }

    public Package getSelectedPackage() {
        return selectedPackage;
    }

    /**
     * Provides a pre-filled {@link Builder} instance
     * with all configurations copied from this. Use it
     * to create a new {@link Config} instance with
     * modified configurations.
     *
     * @return a pre-filled {@link Builder} instance
     */
    public Builder rebuilder() {
        return new Builder(this);
    }

    /**
     * Provides an empty {@link Builder} instance to fill
     * configurations using builder pattern.
     *
     * @return an empty {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Nested builder class to create {@link Config} instances
     * using descriptive methods.
     */
    public static final class Builder {
        private GameConfig gameConfig;
        private Locale locale;
        private transient Path launcherDir;
        private boolean checkUpdatesOnLaunch;
        private boolean closeAfterGameStarts;
        private boolean cacheGamePackages;
        private Package selectedPackage;

        private Builder() { }

        private Builder(final Config last) {
            gameConfig = last.gameConfig;
            locale = last.locale;
            launcherDir = last.launcherDir;
            checkUpdatesOnLaunch = last.checkUpdatesOnLaunch;
            closeAfterGameStarts = last.closeAfterGameStarts;
            cacheGamePackages = last.cacheGamePackages;
            selectedPackage = last.selectedPackage;
        }

        public Builder gameConfig(final GameConfig newGameConfig) {
            gameConfig = newGameConfig;
            return this;
        }

        public Builder locale(final Locale newLocale) {
            locale = newLocale;
            return this;
        }

        public Builder launcherDir(final Path newLauncherDir) {
            launcherDir = newLauncherDir;
            return this;
        }

        public Builder checkUpdatesOnLaunch(final boolean value) {
            checkUpdatesOnLaunch = value;
            return this;
        }

        public Builder closeAfterGameStarts(final boolean value) {
            closeAfterGameStarts = value;
            return this;
        }

        public Builder cacheGamePackages(final boolean value) {
            cacheGamePackages = value;
            return this;
        }

        public Builder selectedPackage(final Package newSelectedPackage) {
            selectedPackage = newSelectedPackage;
            return this;
        }

        public Config build() {
            Objects.requireNonNull(gameConfig, "gameConfig must not be null");
            Objects.requireNonNull(locale, "locale must not be null");
            Objects.requireNonNull(launcherDir, "launcherDir must not be null");

            return new Config(this);
        }
    }
}
