// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.vdurmont.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Platform;

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
final class GameStarter implements Callable<Process> {
    private static final Logger logger = LoggerFactory.getLogger(GameStarter.class);

    final ProcessBuilder processBuilder;
    private final Semver engineVersion;

    /**
     * @param installation          the directory under which we will find {@code libs/Terasology.jar}, also used as the process's
     *                          working directory
     * @param gameDataDirectory {@code -homedir}, the directory where Terasology's data files (saves & etc) are kept
     * @param heapMin           java's {@code -Xms}
     * @param heapMax           java's {@code -Xmx}
     * @param javaParams        additional arguments for the {@code java} command line
     * @param gameParams        additional arguments for the Terasology command line
     * @param logLevel          the minimum level of log events Terasology will include on its output stream to us
     */
    GameStarter(Installation installation, Path gameDataDirectory, JavaHeapSize heapMin, JavaHeapSize heapMax,
                List<String> javaParams, List<String> gameParams, Level logLevel) throws IOException {
        engineVersion = installation.getEngineVersion();
        var gamePath = installation.path;

        final boolean isMac = Platform.getPlatform().isMac();
        final List<String> processParameters = new ArrayList<>();

        processParameters.add(getRuntimePath().toString());

        if (heapMin.isUsed()) {
            processParameters.add("-Xms" + heapMin.getSizeParameter());
        }
        if (heapMax.isUsed()) {
            processParameters.add("-Xmx" + heapMax.getSizeParameter());
        }
        processParameters.add("-DlogOverrideLevel=" + logLevel.name());

        if (isMac && VersionHistory.LWJGL3.isProvidedBy(engineVersion)) {
            processParameters.add("-XstartOnFirstThread");  // lwjgl3 requires this on OS X
            // awt didn't work either, but maybe fixed on newer versions?
            //   https://github.com/LWJGLX/lwjgl3-awt/issues/1
            processParameters.add("-Djava.awt.headless=true");
        }

        processParameters.addAll(javaParams);

        processParameters.add("-jar");
        processParameters.add(installation.getGameJarPath().toString());

        // Parameters after this are for the game facade, not the java runtime.
        processParameters.add(homeDirParameter(gameDataDirectory));
        processParameters.addAll(gameParams);

        if (isMac) {
            // splash screen uses awt, so no awt => no splash
            processParameters.add(noSplashParameter());
        }

        processBuilder = new ProcessBuilder(processParameters)
                .directory(gamePath.toFile())
                .redirectErrorStream(true);

        //noinspection ConstantConditions
        if (true) {  // MANGO
            var env = processBuilder.environment();
                var libMangoHud = "/usr/lib/mangohud/lib/libMangoHud_dlsym.so:/usr/lib/mangohud/lib/libMangoHud.so";
                env.put("MANGOHUD", "1");
                env.merge("LD_PRELOAD", libMangoHud, (s, value) -> "$value:$libMangoHud");
        }
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

    String homeDirParameter(Path gameDataDirectory) {
        if (terasologyUsesPosixOptions()) {
            return "--homedir=" + gameDataDirectory.toAbsolutePath();
        } else {
            return "-homedir=" + gameDataDirectory.toAbsolutePath();
        }
    }

    String noSplashParameter() {
        return terasologyUsesPosixOptions() ? "--no-splash" : "-noSplash";
    }

    boolean terasologyUsesPosixOptions() {
        return VersionHistory.PICOCLI.isProvidedBy(engineVersion);
    }
}
