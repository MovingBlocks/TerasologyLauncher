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
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.nio.file.Path;
import java.util.Locale;

public class LauncherConfig {
    // Game settings
    private Path installDir;
    private Path dataDir;
    private JavaHeapSize maxMemory;
    private JavaHeapSize initMemory;
    private String javaParam;
    private String gameParam;
    private LogLevel logLevel;

    // Launcher settings
    private Locale locale;
    private transient Path launcherDir;
    private boolean checkUpdatesOnLaunch;
    private boolean closeAfterGameStart;
    private boolean cacheGamePackages;

    // Miscellaneous
    private Package selectedPackage;

    public Path getInstallDir() {
        return installDir;
    }

    public void setInstallDir(Path installDir) {
        this.installDir = installDir;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public void setDataDir(Path dataDir) {
        this.dataDir = dataDir;
    }

    public JavaHeapSize getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(JavaHeapSize maxMemory) {
        this.maxMemory = maxMemory;
    }

    public JavaHeapSize getInitMemory() {
        return initMemory;
    }

    public void setInitMemory(JavaHeapSize initMemory) {
        this.initMemory = initMemory;
    }

    public String getJavaParam() {
        return javaParam;
    }

    public void setJavaParam(String javaParam) {
        this.javaParam = javaParam;
    }

    public String getGameParam() {
        return gameParam;
    }

    public void setGameParam(String gameParam) {
        this.gameParam = gameParam;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Path getLauncherDir() {
        return launcherDir;
    }

    public void setLauncherDir(Path launcherDir) {
        if (this.launcherDir != null) {
            throw new IllegalStateException("Cannot reset launcherDir");
        }
        this.launcherDir = launcherDir;
    }

    public boolean isCheckUpdatesOnLaunch() {
        return checkUpdatesOnLaunch;
    }

    public void setCheckUpdatesOnLaunch(boolean checkUpdatesOnLaunch) {
        this.checkUpdatesOnLaunch = checkUpdatesOnLaunch;
    }

    public boolean isCloseAfterGameStart() {
        return closeAfterGameStart;
    }

    public void setCloseAfterGameStart(boolean closeAfterGameStart) {
        this.closeAfterGameStart = closeAfterGameStart;
    }

    public boolean isCacheGamePackages() {
        return cacheGamePackages;
    }

    public void setCacheGamePackages(boolean cacheGamePackages) {
        this.cacheGamePackages = cacheGamePackages;
    }

    public Package getSelectedPackage() {
        return selectedPackage;
    }

    public void setSelectedPackage(Package selectedPackage) {
        this.selectedPackage = selectedPackage;
    }
}
