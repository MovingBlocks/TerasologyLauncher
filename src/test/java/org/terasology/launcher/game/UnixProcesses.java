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

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class UnixProcesses {
    static final Callable<Process> COMPLETES_SUCCESSFULLY = runProcess("true");
    static final Callable<Process> COMPLETES_WITH_ERROR = runProcess("false");
    static final Callable<Process> NO_SUCH_COMMAND = runProcess(() -> {
        // If you have a program with this name on your path while running these tests,
        // you have incredible luck.
        return "nope" + new Random()
            .ints(16, 0, 255).mapToObj(
                i -> Integer.toString(i, Character.MAX_RADIX)
            )
            .collect(Collectors.joining());
    });

    private UnixProcesses() { }

    private static Callable<Process> runProcess(String... command) {
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        return processBuilder::start;
    }

    private static Callable<Process> runProcess(Supplier<String> command) {
        return runProcess(command.get());
    }

    private static class SlowTicker implements Callable<Process> {
        private final int seconds;

        SlowTicker(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public Process call() throws IOException {
            var pb = new ProcessBuilder(
                    "/bin/bash", "-c",
                    String.format("for i in $( seq %s ) ; do echo $i ; sleep 1 ; done", seconds)
            );
            var proc = pb.start();
            LoggerFactory.getLogger(SlowTicker.class).debug(" ⏲ Ticker PID {}", proc.pid());
            return proc;
        }
    }

    static class SelfDestructingProcess extends SlowTicker {
        SelfDestructingProcess(final int seconds) {
            super(seconds);
        }

        @Override
        public Process call() throws IOException {
            var proc = super.call();
            new ScheduledThreadPoolExecutor(1).schedule(
                    // looks like destroy = SIGTERM,
                    // destroyForcibly = SIGKILL
                    proc::destroy, 100, TimeUnit.MILLISECONDS);
            return proc;
        }
    }
}
