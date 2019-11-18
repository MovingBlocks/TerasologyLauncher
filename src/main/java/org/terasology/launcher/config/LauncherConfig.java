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
    private Path launcherDir;
    private boolean checkUpdatesOnLaunch;
    private boolean closeAfterGameStart;
    private boolean cacheGamePackages;

    // Miscellaneous
    private Package selectedPackage;
}
