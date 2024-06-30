// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.terasology.launcher.platform.UnsupportedPlatformException;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.platform.Platform;

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

    /**
     * @param gameInstallation      the directory under which we will find {@code libs/Terasology.jar}, also used as the process's
     *                          working directory
     * @param gameDataDirectory {@code -homedir}, the directory where Terasology's data files (saves & etc) are kept
     * @param heapMin           java's {@code -Xms}
     * @param heapMax           java's {@code -Xmx}
     * @param javaParams        additional arguments for the {@code java} command line
     * @param gameParams        additional arguments for the Terasology command line
     * @param logLevel          the minimum level of log events Terasology will include on its output stream to us
     */
    GameStarter(GameInstallation gameInstallation, Path gameDataDirectory, JavaHeapSize heapMin, JavaHeapSize heapMax,
                List<String> javaParams, List<String> gameParams, Level logLevel)
            throws IOException, GameVersionNotSupportedException, UnsupportedPlatformException {
        Semver engineVersion = gameInstallation.getEngineVersion();
        var gamePath = gameInstallation.getPath();

        final boolean isMac = Platform.getPlatform().isMac();
        final List<String> processParameters = new ArrayList<>();

        processParameters.add(getRuntimePath(engineVersion).toString());

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
        processParameters.add(gameInstallation.getGameJarPath().toString());

        // Parameters after this are for the game facade, not the java runtime.
        processParameters.add(homeDirParameter(gameDataDirectory, engineVersion));
        processParameters.addAll(gameParams);

        if (isMac) {
            // splash screen uses awt, so no awt => no splash
            processParameters.add(noSplashParameter(engineVersion));
        }

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
    Path getRuntimePath(Semver engineVersion) throws GameVersionNotSupportedException {
        //TODO: Select the right JRE based on VersionHistory#getJavaVersionForEngine. Probably something along the lines
        //      of the following:
        //        Semver minJavaVersion = VersionHistory.getJavaVersionForEngine(engineVersion); // may throw GameVersionNotSupportedException
        //        <Installation> JRE jre = JreManager.getJreFor(platform, minJavaVersion);       // may throw GameVersionNotSupportedException
        //        return Paths.get(jre.getPath(), "bin", "java");
        if (VersionHistory.JAVA17.isProvidedBy(engineVersion)) {
            // throw exception as the version is not supported
            throw new GameVersionNotSupportedException(engineVersion);
        }
        return Paths.get(System.getProperty("java.home"), "bin", "java");
    }

    String homeDirParameter(Path gameDataDirectory, Semver engineVersion) {
        if (terasologyUsesPosixOptions(engineVersion)) {
            return "--homedir=" + gameDataDirectory.toAbsolutePath();
        } else {
            return "-homedir=" + gameDataDirectory.toAbsolutePath();
        }
    }

    String noSplashParameter(Semver engineVersion) {
        return terasologyUsesPosixOptions(engineVersion) ? "--no-splash" : "-noSplash";
    }

    boolean terasologyUsesPosixOptions(Semver engineVersion) {
        return VersionHistory.PICOCLI.isProvidedBy(engineVersion);
    }
}
