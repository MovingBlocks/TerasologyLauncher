/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasologylauncher.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.updater.GameData;
import org.terasologylauncher.util.Memory;
import org.terasologylauncher.util.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MrBarsack
 * @author Skaldarnar
 */
public final class TerasologyStarter {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyStarter.class);

    private final File terasologyDirectory;
    private final OperatingSystem os;
    private final int maxMemory;
    private final int initialMemory;

    public TerasologyStarter(final File terasologyDirectory, final OperatingSystem os, final int maxMemory,
                             final int initialMemory) {
        this.terasologyDirectory = terasologyDirectory;
        this.os = os;
        this.maxMemory = maxMemory;
        this.initialMemory = initialMemory;
    }

    public boolean startGame() {
        if (os.isWindows()) {
            return startWindows();
        } else if (os.isMac()) {
            return startMac();
        } else if (os.isUnix()) {
            return startLinux();
        } else {
            logger.error("Unknown operating system - cannot start game! {}", os);
        }
        return false;
    }

    private boolean startLinux() {
        final List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(createParameters());
        parameters.add("-jar");
        parameters.add(GameData.getGameJar(terasologyDirectory).getName());
        final ProcessBuilder pb = new ProcessBuilder(parameters);
        pb.directory(terasologyDirectory);
        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Could not start game!", e);
            return false;
        }
        return true;
    }

    private boolean startMac() {
        final List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(createParameters());
        parameters.add("-jar");
        parameters.add(GameData.getGameJar(terasologyDirectory).getName());
        final ProcessBuilder pb = new ProcessBuilder(parameters);
        pb.directory(terasologyDirectory);
        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Could not start game!", e);
            return false;
        }
        return true;
    }

    private boolean startWindows() {
        final List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(createParameters());
        parameters.add("-jar");
        parameters.add(GameData.getGameJar(terasologyDirectory).getName());
        final ProcessBuilder pb = new ProcessBuilder(parameters);
        pb.directory(terasologyDirectory);
        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Could not start game!", e);
            return false;
        }
        return true;
    }

    private List<String> createParameters() {
        final List<String> parameters = new ArrayList<String>();
        // add maximal RAM parameter
        parameters.add("-Xmx" + Memory.getMemoryFromId(maxMemory).getMemoryMB() + "m");
        // add initial RAM parameter
        if (initialMemory >= 0) {
            parameters.add("-Xms" + Memory.getMemoryFromId(initialMemory).getMemoryMB() + "m");
        }
        return parameters;
    }
}
