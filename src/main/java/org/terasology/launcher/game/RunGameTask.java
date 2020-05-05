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

import com.google.common.base.MoreObjects;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.packages.Package;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

final class RunGameTask extends Task<Void> {
    static final int EXIT_CODE_OK = 0;

    private static final Logger logger = LoggerFactory.getLogger(RunGameTask.class);

    protected Callable<Process> starter;

    private final Package pkg;

    RunGameTask(Package pkg,
                       Path gamePath,
                       Path gameDataDirectory,
                       JavaHeapSize heapMin,
                       JavaHeapSize heapMax,
                       List<String> javaParams,
                       List<String> gameParams,
                       LogLevel logLevel) {
        this.pkg = pkg;
        this.starter = new GameStarterWIP(
                gamePath,  gameDataDirectory,  heapMin,  heapMax,  javaParams,  gameParams,  logLevel);
    }

    protected RunGameTask(Package pkg) {
        this.pkg = pkg;
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
     */
    @Override
    protected Void call() throws GameStartError, GameExitError, InterruptedException {
        verifyNotNull(this.starter);
        verify(!this.isDone());
        Process process;
        try {
            process = this.starter.call();
        } catch (Exception e) {
            throw new GameStartError(e);
        }
        monitorProcess(process);
        return null;
    }

    void monitorProcess(Process process) throws InterruptedException, GameExitError {
        checkNotNull(process);
        logger.debug("Game process is {}", process);
        updateMessage("Game running as process " + process.pid());

        // log each line of process output
        var gameOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        gameOutput.lines().forEachOrdered(line -> logger.info("Game output: {}", line));

        try {
            // The output has closed, so we _often_ have the exit value immediately, but apparently
            // not always — the tests were flaky. To be safe, waitFor.
            var exitValue = process.waitFor();
            logger.debug("Game closed with the exit value '{}'.", exitValue);

            if (exitValue == EXIT_CODE_OK) {
                updateMessage("Process complete.");
            } else {
                updateMessage("Process exited with code " + exitValue);
                throw new GameExitError(exitValue);
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for game process exit.", e);
            throw e;
        }
    };

    public abstract static class RunGameError extends Exception { };

    public static class GameStartError extends RunGameError {
        GameStartError(final Exception e) {
            super();
            this.initCause(e);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).addValue(this.getCause()).toString();
        }
    };

    public static class GameExitError extends RunGameError {
        public final int exitValue;

        GameExitError(final int exitValue) {
            this.exitValue = exitValue;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("exitValue", exitValue).toString();
        }
    };
}
