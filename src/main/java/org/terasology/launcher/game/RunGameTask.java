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
import org.terasology.launcher.settings.BaseLauncherSettings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

public final class RunGameTask extends Task<Boolean> {
    static final int EXIT_CODE_OK = 0;

    // We'll only see this if the game's log level is INFO or higher, but it is by default.
    static final Predicate<String> START_MATCH = Pattern.compile("TerasologyEngine.+Initialization completed")
            .asPredicate();

    private static final Logger logger = LoggerFactory.getLogger(RunGameTask.class);
    public final Package pkg;
    protected Callable<Process> starter;
    private boolean valueSet;

    protected RunGameTask(Package pkg) {
        this.pkg = pkg;
    }

    public RunGameTask(final Package pkg, final Path gamePath, final BaseLauncherSettings launcherSettings) {
        this.pkg = pkg;
        this.starter = new GameStarter(gamePath, launcherSettings.getGameDataDirectory(), launcherSettings.getMaxHeapSize(),
                                       launcherSettings.getInitialHeapSize(), launcherSettings.getUserJavaParameterList(),
                                       launcherSettings.getUserGameParameterList(), launcherSettings.getLogLevel());
    }

    @Override
    protected Boolean call() throws GameStartError, GameExitError, InterruptedException {
        verifyNotNull(this.starter);
        verify(!this.isDone());
        Process process;
        try {
            process = this.starter.call();
        } catch (Exception e) {
            throw new GameStartError(e);
        }
        monitorProcess(process);
        return true;
    }

    void monitorProcess(Process process) throws InterruptedException, GameExitError {
        checkNotNull(process);
        logger.debug("Game process is {}", process);
        updateMessage("Game running as process " + process.pid());

        // log each line of process output
        var gameOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        gameOutput.lines().forEachOrdered(this::handleOutputLine);

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
    }

    protected void handleOutputLine(String line) {
        if ((!valueSet) && START_MATCH.test(line)) {
            // we have an extra flag just for this because we can't check
            // the content of valueProperty in this thread.
            valueSet = true;
            this.updateValue(true);
        }
        logger.info("Game output: {}", line);
    }

    public abstract static class RunGameError extends Exception { }

    public static class GameStartError extends RunGameError {
        GameStartError(final Exception e) {
            super();
            this.initCause(e);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).addValue(this.getCause()).toString();
        }
    }

    public static class GameExitError extends RunGameError {
        public final int exitValue;

        GameExitError(final int exitValue) {
            this.exitValue = exitValue;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("exitValue", exitValue).toString();
        }
    }
}
