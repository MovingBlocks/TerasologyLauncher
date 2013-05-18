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

package org.terasology.launcher.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.OperatingSystem;
import org.terasology.launcher.version.TerasologyGameVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MrBarsack
 * @author Skaldarnar
 */
public final class TerasologyStarter {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyStarter.class);

    private static final int PROCESS_START_SLEEP_TIME = 5000;

    private final TerasologyGameVersion gameVersion;
    private final OperatingSystem os;
    private final JavaHeapSize maxHeapSize;
    private final JavaHeapSize initialHeapSize;

    public TerasologyStarter(final TerasologyGameVersion gameVersion, final OperatingSystem os,
                             final JavaHeapSize maxHeapSize, final JavaHeapSize initialHeapSize) {
        this.gameVersion = gameVersion;
        this.os = os;
        this.maxHeapSize = maxHeapSize;
        this.initialHeapSize = initialHeapSize;
    }

    public boolean startGame() {
        if (os.isWindows()) {
            return startWindows();
        } else if (os.isMac()) {
            return startMac();
        } else if (os.isUnix()) {
            return startLinux();
        } else {
            logger.error("Unknown operating system '{}'. Cannot start game!", os);
        }
        return false;
    }

    private boolean startLinux() {
        final List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(createParameters());
        parameters.add("-jar");
        parameters.add(gameVersion.getGameJar().getName());
        return startProcess(parameters);
    }

    private boolean startMac() {
        final List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(createParameters());
        parameters.add("-jar");
        parameters.add(gameVersion.getGameJar().getName());
        return startProcess(parameters);
    }

    private boolean startWindows() {
        final List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(createParameters());
        parameters.add("-jar");
        parameters.add(gameVersion.getGameJar().getName());
        return startProcess(parameters);
    }

    private List<String> createParameters() {
        final List<String> parameters = new ArrayList<String>();
        if (initialHeapSize.isUsed()) {
            parameters.add("-Xms" + initialHeapSize.getSizeParameter());
        }
        if (maxHeapSize.isUsed()) {
            parameters.add("-Xmx" + maxHeapSize.getSizeParameter());
        }
        return parameters;
    }

    private boolean startProcess(final List<String> parameters) {
        final ProcessBuilder pb = new ProcessBuilder(parameters);
        pb.redirectErrorStream(true);
        pb.directory(gameVersion.getInstallationPath());
        logger.debug("Starting game process with '{}' in '{}' for '{}'", parameters, gameVersion.getInstallationPath(),
            gameVersion);
        try {
            final Process p = pb.start();

            final Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        final BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line;
                        while ((line = r.readLine()) != null) {
                            logger.trace("Game output: {}", line);
                        }
                        r.close();
                        logger.debug("Game closed.");
                    } catch (IOException e) {
                        logger.error("Couldn't read game output!", e);
                    }
                }
            });
            t.start();

            Thread.sleep(PROCESS_START_SLEEP_TIME);

            if (!t.isAlive()) {
                final int exitValue = p.waitFor();
                logger.warn("Game finished with exit value '{}'", exitValue);
                return false;
            } else {
                logger.info("Game successfully launched");
            }
        } catch (RuntimeException e) {
            // NullPointerException, SecurityException
            logger.error("Could not start game with parameters '{}' for '{}'!", parameters, gameVersion, e);
            return false;
        } catch (InterruptedException e) {
            logger.error("Could not start game with parameters '{}' for '{}'!", parameters, gameVersion, e);
            return false;
        } catch (IOException e) {
            logger.error("Could not start game with parameters '{}' for '{}'!", parameters, gameVersion, e);
            return false;
        }
        return true;
    }
}
