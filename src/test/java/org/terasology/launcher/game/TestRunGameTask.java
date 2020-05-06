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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.log.Level;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.matchers.LogMatchers;
import org.terasology.launcher.SlowTest;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;
import org.threeten.extra.MutableClock;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.util.concurrent.Futures.allAsList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.describedAs;
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
        gameTask.starter = UnixProcesses.COMPLETES_SUCCESSFULLY;

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
        gameTask.starter = UnixProcesses.COMPLETES_WITH_ERROR;

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
        gameTask.starter = MockProcesses.EXCEPTION_THROWING_START;

        executor.submit(gameTask);
        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        Throwable exc = thrown.getCause();
        assertThat(exc, instanceOf(RunGameTask.GameStartError.class));
        assertThat(exc.getCause(), instanceOf(MockProcesses.OurIOException.class));
    }

    @SlowTest
    public void testExeNotFound() {
        // not disabled-on-Windows because all platforms should be capable of failing
        gameTask.starter = UnixProcesses.NO_SUCH_COMMAND;

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
        gameTask.starter = new UnixProcesses.SelfDestructingProcess(5);

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


    @Test
    public void testSuccessEvent() throws Exception {
        var clock = MutableClock.epochUTC();
        var hop = Duration.ofMillis(100);

        final String confirmedStart = "CONFIRMED_START";

        final Logger logger = LoggerFactory.getLogger(TestRunGameTask.class);

        Runnable handleAdvance = () -> {
            WaitForAsyncUtils.waitForFxEvents(1);
            clock.add(hop);
            logger.debug("TICK {}", clock.instant().toEpochMilli());
            WaitForAsyncUtils.waitForFxEvents(1);
        };

        gameTask.starter = () -> new MockProcesses.OneLineAtATimeProcess(
                spyingIterator(List.of(
                        "some babble\n",
                        confirmedStart + "\n",
                        "more babble\n",
                        "have a nice day etc\n"
                ), handleAdvance)
        );

        final SettableFuture<Boolean> theValue = SettableFuture.create();
        final SettableFuture<Instant> gotValueAt = SettableFuture.create();
        final SettableFuture<Instant> taskDoneAt = SettableFuture.create();

        gameTask.valueProperty().addListener(started -> {
            theValue.set(gameTask.valueProperty().getValue());
            clock.add(7, ChronoUnit.MILLIS);
            gotValueAt.set(clock.instant());
        });

        WaitForAsyncUtils.asyncFx(() ->
            gameTask.addEventHandler(
                    WorkerStateEvent.WORKER_STATE_SUCCEEDED, (event) -> {
                        clock.add(11, ChronoUnit.MILLIS);
                        taskDoneAt.set(clock.instant());
                     }
            )
        ).get();

        executor.submit(gameTask);

        try {
            //noinspection UnstableApiUsage
            allAsList(
                   theValue, gotValueAt, taskDoneAt
            ).get(20, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            logger.warn("Failed to get futures because {}", e.getLocalizedMessage());
            var result = gameTask.get();
            logger.warn("And yet gameTask returned? {}", result);
            throw e;
        }

        // get()ing the list made sure these are all complete
        assertTrue(theValue.get());

        // Assert that we got the value significantly before the process finished.
        assertThat(Duration.between(gotValueAt.get(), taskDoneAt.get()),
                   describedAs("Time between %0 and %1 should be greater than %2",
                               greaterThan(hop.multipliedBy(2)),
                               gotValueAt.get(), taskDoneAt.get(), hop.multipliedBy(2)));
    }


    public static Iterator<String> spyingIterator(List<String> list, Runnable onNext) {
        return list.stream().takeWhile(string -> {
            onNext.run();
            return true;
        }).iterator();
    }
}
