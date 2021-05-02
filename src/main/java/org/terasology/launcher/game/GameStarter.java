// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.terasology.launcher.util.JavaHeapSize;

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
    private static final Logger logger = LoggerFactory.getLogger(GameStarter.class);

    final ProcessBuilder processBuilder;

    /**
     * @param gamePath          the directory under which we will find {@code libs/Terasology.jar}, also used as the process's
     *                          working directory
     * @param gameDataDirectory {@code -homedir}, the directory where Terasology's data files (saves & etc) are kept
     * @param heapMin           java's {@code -Xms}
     * @param heapMax           java's {@code -Xmx}
     * @param javaParams        additional arguments for the {@code java} command line
     * @param gameParams        additional arguments for the Terasology command line
     * @param logLevel          the minimum level of log events Terasology will include on its output stream to us
     */
    GameStarter(Path gamePath, Path gameDataDirectory,
                JavaHeapSize heapMin, JavaHeapSize heapMax,
                List<String> javaParams,
                List<String> gameParams,
                Level logLevel) {
        final List<String> processParameters = new ArrayList<>();
        processParameters.add(getRuntimePath().toString());

        if (heapMin.isUsed()) {
            processParameters.add("-Xms" + heapMin.getSizeParameter());
        }
        if (heapMax.isUsed()) {
            processParameters.add("-Xmx" + heapMax.getSizeParameter());
        }
        processParameters.add("-DlogOverrideLevel=" + logLevel.name());
        processParameters.addAll(javaParams);

        // Locate the main game jar. Currently, Terasology has custom build logic to put libraries into a 'libs'
        // (plural) subdirectory. As we plan to switch to using default Gradle behavior we have to do a quick check
        // how the game distribution was build (i.e., custom 'libs' or default 'lib').
        //TODO: this should probably be part of ReleaseMetadata and be determined further up the hierarchy
        processParameters.add("-jar");
        if (gamePath.resolve("libs").toFile().isDirectory()) {
            // custom Terasology build logic puts libraries into 'libs' subdirectory
            processParameters.add(gamePath.resolve(Path.of("libs", "Terasology.jar")).toString());
        } else {
            // Gradle defaults to putting libraries in a 'lib' subdirectory
            processParameters.add(gamePath.resolve(Path.of("lib", "Terasology.jar")).toString());
        }

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
        logger.info("Starting game with: {}", String.join(" ", processBuilder.command()));
        return processBuilder.start();
    }

    /**
     * @return the executable {@code java} file to run the game with
     */
    Path getRuntimePath() {
        return Paths.get(System.getProperty("java.home"), "bin", "java");
    }
}
