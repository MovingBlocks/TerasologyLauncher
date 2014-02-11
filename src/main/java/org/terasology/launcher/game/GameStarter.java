/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.launcher.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.JavaHeapSize;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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

    public void dispose() {
        if (gameThread != null) {
            gameThread.interrupt();
        }
        gameThread = null;
    }

    public boolean startGame(final TerasologyGameVersion gameVersion, final File gameDataDirectory, final JavaHeapSize maxHeapSize, final JavaHeapSize initialHeapSize) {
        if (isRunning()) {
            logger.warn("The game can not be started because another game is already running! '{}'", gameThread);
            return false;
        }

        final List<String> javaParameters = createJavaParameters(maxHeapSize, initialHeapSize);
        final List<String> processParameters = createProcessParameters(gameVersion, gameDataDirectory, javaParameters);

        return startProcess(gameVersion, processParameters);
    }

    private List<String> createJavaParameters(final JavaHeapSize maxHeapSize, final JavaHeapSize initialHeapSize) {
        final List<String> javaParameters = new ArrayList<>();
        if (initialHeapSize.isUsed()) {
            javaParameters.add("-Xms" + initialHeapSize.getSizeParameter());
        }
        if (maxHeapSize.isUsed()) {
            javaParameters.add("-Xmx" + maxHeapSize.getSizeParameter());
        }
        return javaParameters;
    }

    private List<String> createProcessParameters(final TerasologyGameVersion gameVersion, final File gameDataDirectory, final List<String> javaParameters) {
        final List<String> processParameters = new ArrayList<>();
        processParameters.add("java");
        processParameters.addAll(javaParameters);
        processParameters.add("-jar");
        processParameters.add(gameVersion.getGameJar().getName());
        processParameters.add("-homedir=" + gameDataDirectory.getPath());

        return processParameters;
    }

    private boolean startProcess(final TerasologyGameVersion gameVersion, final List<String> processParameters) {
        final ProcessBuilder pb = new ProcessBuilder(processParameters);
        pb.redirectErrorStream(true);
        pb.directory(gameVersion.getInstallationPath());
        logger.debug("Starting game process with '{}' in '{}' for '{}'", processParameters, gameVersion.getInstallationPath(), gameVersion);
        try {
            final Process p = pb.start();

            gameThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        try (final BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
                            String line;
                            do {
                                line = r.readLine();
                                logger.trace("Game output: {}", line);
                            } while (!Thread.currentThread().isInterrupted() && (line != null));
                        }
                        if (Thread.currentThread().isInterrupted()) {
                            logger.debug("Game thread interrupted.");
                            return;
                        }
                        int exitValue = -1;
                        try {
                            exitValue = p.waitFor();
                        } catch (InterruptedException e) {
                            logger.error("The game thread was interrupted!", e);
                        }
                        logger.debug("Game closed with the exit value '{}'.", exitValue);
                    } catch (IOException e) {
                        logger.error("Could not read game output!", e);
                    }
                }
            });
            gameThread.setName("game" + gameVersion.getBuildNumber());
            gameThread.start();

            Thread.sleep(PROCESS_START_SLEEP_TIME);

            if (!gameThread.isAlive()) {
                final int exitValue = p.waitFor();
                logger.warn("The game was stopped early. It returns with the exit value '{}'.", exitValue);
                return false;
            } else {
                logger.info("The game is successfully launched.");
            }
        } catch (InterruptedException | IOException | RuntimeException e) {
            logger.error("The game could not be started due to an error! Parameters '{}' for '{}'!", processParameters, gameVersion, e);
            return false;
        }
        return true;
    }
}
