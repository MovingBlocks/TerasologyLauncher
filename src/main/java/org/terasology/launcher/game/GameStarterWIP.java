package org.terasology.launcher.game;

import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class GameStarterWIP {
    final ProcessBuilder processBuilder;

    GameStarterWIP(Path gamePath, Path gameDataDirectory, JavaHeapSize heapMin, JavaHeapSize heapMax, List<String> javaParams, List<String> gameParams,
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

        processBuilder = new ProcessBuilder(processParameters).directory(gamePath.toFile());
    }

    public Process start() throws IOException {
        return processBuilder.start();
    }

    Path getRuntimePath() {
        return Paths.get(System.getProperty("java.home"),"bin", "java");
    }
}
