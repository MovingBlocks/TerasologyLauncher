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

package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.version.TerasologyGameVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class GameStarter {

    private static final Logger logger = LoggerFactory.getLogger(GameStarter.class);

    private static final int PROCESS_START_SLEEP_TIME = 5000;

    private Thread gameThread;

    public GameStarter() {
    }

    public boolean isRunning() {
        return (gameThread != null) && gameThread.isAlive();
    }

    public boolean startGame(final TerasologyGameVersion gameVersion, final JavaHeapSize maxHeapSize,
                             final JavaHeapSize initialHeapSize) {
        if (isRunning()) {
            logger.warn("Couldn't start a new game because another game is running!");
            return false;
        }

        final List<String> javaParameters = createJavaParameters(maxHeapSize, initialHeapSize);
        final List<String> processParameters = createProcessParameters(gameVersion, javaParameters);

        return startProcess(gameVersion, processParameters);
    }

    private List<String> createJavaParameters(final JavaHeapSize maxHeapSize, final JavaHeapSize initialHeapSize) {
        final List<String> javaParameters = new ArrayList<String>();
        if (initialHeapSize.isUsed()) {
            javaParameters.add("-Xms" + initialHeapSize.getSizeParameter());
        }
        if (maxHeapSize.isUsed()) {
            javaParameters.add("-Xmx" + maxHeapSize.getSizeParameter());
        }
        return javaParameters;
    }

    private List<String> createProcessParameters(final TerasologyGameVersion gameVersion,
                                                 final List<String> javaParameters) {
        final List<String> processParameters = new ArrayList<String>();
        processParameters.add("java");
        processParameters.addAll(javaParameters);
        processParameters.add("-jar");
        processParameters.add(gameVersion.getGameJar().getName());
        return processParameters;
    }

    private boolean startProcess(final TerasologyGameVersion gameVersion, final List<String> processParameters) {
        final ProcessBuilder pb = new ProcessBuilder(processParameters);
        pb.redirectErrorStream(true);
        pb.directory(gameVersion.getInstallationPath());
        logger.debug("Starting game process with '{}' in '{}' for '{}'", processParameters,
            gameVersion.getInstallationPath(), gameVersion);
        try {
            final Process p = pb.start();

            gameThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        final BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line;
                        while ((line = r.readLine()) != null) {
                            logger.trace("Game output: {}", line);
                        }
                        r.close();
                        int exitValue = -1;
                        try {
                            exitValue = p.waitFor();
                        } catch (InterruptedException e) {
                            logger.error("Game thread interrupted!", e);
                        }
                        logger.debug("Game closed with exit value '{}'", exitValue);
                    } catch (IOException e) {
                        logger.error("Couldn't read game output!", e);
                    }
                }
            });
            gameThread.start();

            Thread.sleep(PROCESS_START_SLEEP_TIME);

            if (!gameThread.isAlive()) {
                final int exitValue = p.waitFor();
                logger.warn("Game finished with exit value '{}'", exitValue);
                return false;
            } else {
                logger.info("Game successfully launched");
            }
        } catch (RuntimeException e) {
            // NullPointerException, SecurityException
            logger.error("Could not start game with parameters '{}' for '{}'!", processParameters, gameVersion, e);
            return false;
        } catch (InterruptedException e) {
            logger.error("Could not start game with parameters '{}' for '{}'!", processParameters, gameVersion, e);
            return false;
        } catch (IOException e) {
            logger.error("Could not start game with parameters '{}' for '{}'!", processParameters, gameVersion, e);
            return false;
        }
        return true;
    }
}
