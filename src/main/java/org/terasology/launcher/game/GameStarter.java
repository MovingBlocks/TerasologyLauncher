/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.game;

import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Takes the game and runtime options, provides something that will launch a process.
 *
 * @see <a href="https://docs.oracle.com/en/java/javase/14/docs/specs/man/java.html#overview-of-java-options">java command manual</a>
 */
class GameStarter implements Callable<Process> {
    final ProcessBuilder processBuilder;

    /**
     * @param gamePath the directory under which we will find {@code libs/Terasology.jar}, also used as the process's
     *     working directory
     * @param gameDataDirectory {@code -homedir}, the directory where Terasology's data files (saves & etc) are kept
     * @param heapMin java's {@code -Xms}
     * @param heapMax java's {@code -Xmx}
     * @param javaParams additional arguments for the {@code java} command line
     * @param gameParams additional arguments for the Terasology command line
     * @param logLevel the minimum level of log events Terasology will include on its output stream to us
     */
    GameStarter(Path gamePath, Path gameDataDirectory, JavaHeapSize heapMin, JavaHeapSize heapMax, List<String> javaParams, List<String> gameParams,
                LogLevel logLevel) {
        final List<String> processParameters = new ArrayList<>();
        processParameters.add(getRuntimePath().toString());

        if (heapMin.isUsed()) {
            processParameters.add("-Xms" + heapMin.getSizeParameter());
        }
        if (heapMax.isUsed()) {
            processParameters.add("-Xmx" + heapMax.getSizeParameter());
        }
        if (!logLevel.isDefault()) {
            processParameters.add("-DlogOverrideLevel=" + logLevel.name());
        }
        processParameters.addAll(javaParams);

        processParameters.add("-jar");
        processParameters.add(gamePath.resolve(Path.of("libs", "Terasology.jar")).toString());
        processParameters.add("-homedir=" + gameDataDirectory.toAbsolutePath().toString());
        processParameters.addAll(gameParams);

        processBuilder = new ProcessBuilder(processParameters)
                .directory(gamePath.toFile())
                .redirectErrorStream(true);
    }

    /**
     * Start the game in a new process.
     *
     * @return the newly started process
     * @throws IOException from {@link ProcessBuilder#start()}
     */
    @Override
    public Process call() throws IOException {
        return processBuilder.start();
    }

    /**
     * @return the executable {@code java} file to run the game with
     */
    Path getRuntimePath() {
        return Paths.get(System.getProperty("java.home"), "bin", "java");
    }
}
