package org.terasology.launcher.game;

import javafx.concurrent.Task;
import org.terasology.launcher.packages.Package;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class RunGameTask extends Task<Void> {
    private final Package pkg;
    private final Path gamePath;
    private final Path gameDataDirectory;
    private final JavaHeapSize heapMin;
    private final JavaHeapSize heapMax;
    private final List<String> userJavaParameters;
    private final List<String> userGameParameters;
    private final LogLevel logLevel;

    public RunGameTask(Package pkg, Path gamePath, Path gameDataDirectory, JavaHeapSize heapMin, JavaHeapSize heapMax, List<String> javaParams, List<String> gameParams, LogLevel logLevel) {
        this.pkg = pkg;
        this.gamePath = gamePath;
        this.gameDataDirectory = gameDataDirectory;
        this.heapMin = heapMin;
        this.heapMax = heapMax;
        this.userJavaParameters = javaParams;
        this.userGameParameters = gameParams;
        this.logLevel = logLevel;
    }

    /**
     * Invoked when the Task is executed, the call method must be overridden and
     * implemented by subclasses. The call method actually performs the
     * background thread logic. Only the updateProgress, updateMessage, updateValue and
     * updateTitle methods of Task may be called from code within this method.
     * Any other interaction with the Task from the background thread will result
     * in runtime exceptions.
     *
     * @return The result of the background work, if any.
     * @throws Exception an unhandled exception which occurred during the
     *                   background operation
     */
    @Override
    protected Void call() throws Exception {
        // start subprocess
        // monitor output (and pass through to logs)
        // start a countdown
        // stopped before countdown? fail task with error message
        // still running? okay, update progress

        // when process exits, finish task

        // if task gets cancelled, end process


        return null;
    }

    public ProcessBuilder buildProcess() {
        final List<String> processParameters = new ArrayList<>();
        processParameters.add(getRuntimePath().toString());
        processParameters.addAll(createJavaParameters());
        processParameters.add("-jar");
        processParameters.add(gamePath.resolve(Path.of("libs", "Terasology.jar")).toString());
        processParameters.add("-homedir=" + gameDataDirectory.toAbsolutePath().toString());
        processParameters.addAll(userGameParameters);

        return new ProcessBuilder(processParameters).directory(gamePath.toFile());
    }

    Path getRuntimePath() {
        return Paths.get(System.getProperty("java.home"),"bin", "java");
    }

    private List<String> createJavaParameters() {
        final List<String> javaParameters = new ArrayList<>();
        if (heapMin.isUsed()) {
            javaParameters.add("-Xms" + heapMin.getSizeParameter());
        }
        if (heapMax.isUsed()) {
            javaParameters.add("-Xmx" + heapMax.getSizeParameter());
        }
        if (!logLevel.isDefault()) {
            javaParameters.add("-DlogOverrideLevel=" + logLevel.name());
        }
        javaParameters.addAll(userJavaParameters);
        return javaParameters;
    }
}
