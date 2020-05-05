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

import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javafx.concurrent.WorkerStateEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.util.concurrent.Futures.allAsList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(ApplicationExtension.class)
public class TestRunGameTask {

    static final int EXIT_CODE_OK = 0;
    static final int EXIT_CODE_ERROR = 1;
    static final int EXIT_CODE_SIGKILL = 0b10000000 + 9;   // SIGKILL = 9
    static final int EXIT_CODE_SIGTERM = 0b10000000 + 15;  // SIGTERM = 15

    static Callable<Process> completesSuccessfully = runProcess("true");
    static Callable<Process> completesWithError = runProcess("false");
    static Callable<Process> exceptionThrowingStart = () -> {
        throw new OurIOException("GRUMPY \uD83D\uDC7F");
    };
    static Callable<Process> noSuchCommand = runProcess(() -> {
        // If you have a program with this name on your path while running these tests,
        // you have incredible luck.
        return "nope" + new Random()
                .ints(16, 0, 255).mapToObj(
                        i -> Integer.toString(i, Character.MAX_RADIX)
                )
                .collect(Collectors.joining());
    });

    private ExecutorService executor;
    private RunGameTask gameTask;

    @SuppressWarnings("SameParameterValue")
    static ExecutorService singleThreadExecutor(String nameFormat) {
        var builder = new ThreadFactoryBuilder().setNameFormat(nameFormat);
        return Executors.newSingleThreadExecutor(builder.build());
    }

    @BeforeEach
    void setUp() {
        // Would it be plausible to do a @BeforeAll thing that provides a thread pool we don't have
        // to tear down for each test? What kind of assertions would we have to make between tests
        // to ensure it's in a clean state?
        executor = singleThreadExecutor("gameTask-%s");
        gameTask = new RunGameTask(null);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        assertThat(executor.shutdownNow(), empty());
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        assertTrue(executor.isTerminated());
        assertTrue(gameTask.isDone());
    }

    @SlowTest
    @DisabledOnOs(OS.WINDOWS)
    public void testGameExitSuccessful() throws InterruptedException, ExecutionException {
        gameTask.starter = completesSuccessfully;

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
        gameTask.get();

        hasExitMessage.assertObservation(100, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void testGameExitError() {
        gameTask.starter = completesWithError;

        var hasExitMessage = TestLoggers.sys().expect(
                RunGameTask.class.getName(), Level.DEBUG,
                allOf(
                    LogMatchers.hasFormatWithPattern("Game closed with the exit value.*"),
                    LogMatchers.hasArguments(EXIT_CODE_ERROR)
                )
        );

        executor.submit(gameTask);
        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        Throwable exc = thrown.getCause();
        assertThat(exc, instanceOf(RunGameTask.GameExitError.class));
        assertEquals(EXIT_CODE_ERROR, ((RunGameTask.GameExitError) exc).exitValue);

        hasExitMessage.assertObservation(100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testBadStarter() {
        gameTask.starter = exceptionThrowingStart;

        executor.submit(gameTask);
        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        Throwable exc = thrown.getCause();
        assertThat(exc, instanceOf(RunGameTask.GameStartError.class));
        assertThat(exc.getCause(), instanceOf(OurIOException.class));
    }

    @SlowTest
    public void testExeNotFound() {
        gameTask.starter = noSuchCommand;

        executor.submit(gameTask);
        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        Throwable exc = thrown.getCause();
        assertThat(exc, instanceOf(RunGameTask.GameStartError.class));
        var cause = exc.getCause();
        assertThat(cause, allOf(
                instanceOf(IOException.class),
                hasToString(containsStringIgnoringCase("no such file"))
       ));
    }

    @SlowTest
    @DisabledOnOs(OS.WINDOWS)
    public void testTerminatedProcess() {
        // FIXME: SelfDestructionProcess is using some very arbitrary timeouts.
        //    Which means it's unnecessarily slow and probably also flaky.
        gameTask.starter = new SelfDestructingProcess(5);

        executor.submit(gameTask);
        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        Throwable exc = thrown.getCause();
        assertThat(exc, instanceOf(RunGameTask.GameExitError.class));
        final int exitValue = ((RunGameTask.GameExitError) exc).exitValue;
        // It is redundant to test both that a value is one thing and
        // also is not a different thing, but it'd be informative test output if
        // it fails with the other signal.
        assertThat(exitValue, allOf(equalTo(EXIT_CODE_SIGTERM), not(EXIT_CODE_SIGKILL)));
    }


    @SlowTest
    @DisabledOnOs(OS.WINDOWS)
    public void testSuccessEvent() throws Exception {
        // FIXME: use a mock process
        //     This test doesn't deal with process exceptions or exit codes, so we could
        //     use a mock process instead of one of these things that's platform-specific.
        final String confirmedStart = "CONFIRMED_START";
        gameTask.starter = runProcess("bash", "-c", "echo yolo " + confirmedStart + "; sleep 1");

        final SettableFuture<Boolean> theValue = SettableFuture.create();
        final SettableFuture<Instant> gotValueAt = SettableFuture.create();
        final SettableFuture<Instant> taskDoneAt = SettableFuture.create();

        gameTask.valueProperty().addListener(started -> {
            theValue.set(gameTask.valueProperty().getValue());
            gotValueAt.set(Instant.now());
        });

        WaitForAsyncUtils.asyncFx(() ->
            gameTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (event) ->
                taskDoneAt.set(Instant.now())
            )
        );

        executor.submit(gameTask);

        //noinspection UnstableApiUsage
        allAsList(
               theValue, gotValueAt, taskDoneAt
        ).get(3000, TimeUnit.SECONDS);

        // get()ing the list made sure these are all complete
        assertTrue(theValue.get());

        // Assert that we got the value significantly before the process finished.
        assertThat(Duration.between(gotValueAt.get(), taskDoneAt.get()),
                   greaterThan(Duration.ofMillis(100)));
    }


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

    private static class SelfDestructingProcess extends SlowTicker {
        SelfDestructingProcess(final int seconds) {
            super(seconds);
        }

        @Override
        public Process call() throws IOException {
            var proc = super.call();
            new ScheduledThreadPoolExecutor(1).schedule(
                    // looks like destroy = SIGTERM,
                    // destroyForcibly = SIGKILL
                    proc::destroy, 100, TimeUnit.MILLISECONDS
            );
            return proc;
        }
    }

    static class OurIOException extends IOException {
        OurIOException(final String grumpy) {
            super(grumpy);
        }
    }
}
