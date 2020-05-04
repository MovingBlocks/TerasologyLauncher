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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jersey.repackaged.com.google.common.base.Throwables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.spf4j.log.Level;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.matchers.LogMatchers;
import org.terasology.launcher.SlowTest;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(ApplicationExtension.class)
public class TestRunGameTask {

    static final int EXIT_CODE_OK = 0;
    static final int EXIT_CODE_ERROR = 1;

    private ExecutorService executor;

    @SuppressWarnings("SameParameterValue")
    static ExecutorService singleThreadExecutor(String nameFormat) {
        var builder = new ThreadFactoryBuilder().setNameFormat(nameFormat);
        return Executors.newSingleThreadExecutor(builder.build());
    }

    @BeforeEach
    void setUp() {
        executor = singleThreadExecutor("gameTask-%s");
    }

    @SlowTest
    @DisabledOnOs(OS.WINDOWS)
    public void testGameExitSuccessful() throws InterruptedException, ExecutionException {
        RunGameTask gameTask = new RunGameTask(null);
        gameTask.starter = new CompletesSuccessfully();

        // we can use TestLogger expectations without Slf4jTestRunner, we just can't
        // depend on their annotations. I think.
        var hasExitMessage = TestLoggers.sys().expect(
                RunGameTask.class.getName(), Level.DEBUG,
                allOf(
                    LogMatchers.hasFormatWithPattern("Game closed with the exit value.*"),
                    LogMatchers.hasArguments(EXIT_CODE_OK)
                )
        );

        executor.submit(gameTask);
        assertEquals(EXIT_CODE_OK, gameTask.get());

        hasExitMessage.assertObservation(100, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void testGameExitError() throws InterruptedException {
        RunGameTask gameTask = new RunGameTask(null);
        gameTask.starter = new ExitsWithErrorStatus();

        var hasExitMessage = TestLoggers.sys().expect(
                RunGameTask.class.getName(), Level.DEBUG,
                allOf(
                    LogMatchers.hasFormatWithPattern("Game closed with the exit value.*"),
                    LogMatchers.hasArguments(EXIT_CODE_ERROR)
                )
        );

        executor.submit(gameTask);
        try {
            assertEquals(EXIT_CODE_ERROR, gameTask.get());
        } catch (ExecutionException e) {
            // see the stack trace from in-thread
            // can we have a test Extension for this please?
            Throwables.propagate(e.getCause());
        }

        hasExitMessage.assertObservation(100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testBadStarter() {
        RunGameTask gameTask = new RunGameTask(null);
        gameTask.starter = new ExceptionThrowingStarter();

        executor.submit(gameTask);
        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        assertThat(thrown.getCause(), instanceOf(OurIOException.class));
    }

    @Test
    public void testExeNotFound() {
        RunGameTask gameTask = new RunGameTask(null);
        gameTask.starter = new NoSuchCommand();

        executor.submit(gameTask);
        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        var cause = thrown.getCause();
        assertThat(cause, allOf(
                instanceOf(IOException.class),
                hasToString(containsStringIgnoringCase("no such file"))
       ));
    }

    @Disabled("use to experiment with how processes work")
    @Test
    public void testProcessKiller() throws InterruptedException, ExecutionException {
        RunGameTask gameTask = new RunGameTask(null);
        gameTask.starter = new SlowTicker(4);

        var result = executor.submit(gameTask);
//        Thread.sleep(2500);
        executor.shutdown();
        executor.awaitTermination(2500, TimeUnit.MILLISECONDS);
        result.get();
    }

    static class SlowTicker implements IGameStarter {
        private final int seconds;

        SlowTicker(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public Process start() throws IOException {
            var pb = new ProcessBuilder("/bin/bash", "-c", String.format("for i in $( seq %s ) ; do echo $i ; sleep 1 ; done", seconds));
            var proc = pb.start();
            LoggerFactory.getLogger(SlowTicker.class).info(" ⏲ Ticker PID {}", proc.pid());
            return proc;
        }
    }

    static class CompletesSuccessfully implements IGameStarter {
        @Override
        public Process start() throws IOException {
            return new ProcessBuilder("true").start();
        }
    }

    static class ExitsWithErrorStatus implements IGameStarter {
        @Override
        public Process start() throws IOException {
            return new ProcessBuilder("false").start();
        }
    }

    static class OurIOException extends IOException {
        OurIOException(final String grumpy) {
            super(grumpy);
        }
    }

    static class ExceptionThrowingStarter implements IGameStarter {
        @Override
        public Process start() throws IOException {
            throw new OurIOException("GRUMPY");
        }
    }

    static class NoSuchCommand implements IGameStarter {
        @Override
        public Process start() throws IOException {
            // If you have a program with this name on your path while running these tests,
            // you have incredible luck.
            var filename = "nope" + new Random().ints(16, 0, 255).mapToObj(i -> Integer.toString(i, Character.MAX_RADIX)).collect(Collectors.joining());
            final ProcessBuilder processBuilder = new ProcessBuilder(filename);
            processBuilder.redirectErrorStream(true);
            return processBuilder.start();
        }
    }
}
