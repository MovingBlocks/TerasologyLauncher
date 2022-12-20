// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spf4j.log.Level;
import org.spf4j.test.log.LogAssert;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.matchers.LogMatchers;
import org.terasology.launcher.SlowTest;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(5)
@ExtendWith(ApplicationExtension.class)
public class TestRunGameTask {

    static final int EXIT_CODE_OK = 0;
    static final int EXIT_CODE_ERROR = 1;
    static final int EXIT_CODE_SIGKILL = 0b10000000 + 9;   // SIGKILL = 9
    static final int EXIT_CODE_SIGTERM = 0b10000000 + 15;  // SIGTERM = 15

    private ExecutorService executor;

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
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        assertThat(executor.shutdownNow(), empty());
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        assertTrue(executor.isTerminated());
    }

    @Test
    public void testGameOutput() throws InterruptedException, RunGameTask.RunGameError {
        String[] gameOutputLines = {"LineOne", "LineTwo"};
        Process gameProcess = new MockProcesses.HappyGameProcess(String.join("\n", gameOutputLines));

        var hasGameOutputFormat = LogMatchers.hasFormatWithPattern("^Game output.*");

        // try-with-resources to auto-close LogAssert
        try (LogAssert detailedExpectation = TestLoggers.sys().expect(
                RunGameTask.class.getName(), Level.INFO,
                allOf(hasGameOutputFormat, LogMatchers.hasArguments(gameOutputLines[0])),
                allOf(hasGameOutputFormat, LogMatchers.hasArguments(gameOutputLines[1]))
        )) {
            new NonTimingGameTask(null).monitorProcess(gameProcess);

            detailedExpectation.assertObservation();
        }
    }

    @SlowTest
    @DisabledOnOs(OS.WINDOWS)
    public void testGameExitSuccessful() throws InterruptedException, ExecutionException {
        var gameTask = new NonTimingGameTask(UnixProcesses.COMPLETES_SUCCESSFULLY);

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
    public void testGameExitError() throws InterruptedException {
        var gameTask = new RunGameTask(UnixProcesses.COMPLETES_WITH_ERROR);

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
        var gameTask = new RunGameTask(MockProcesses.EXCEPTION_THROWING_START);

        executor.submit(gameTask);

        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        Throwable exc = thrown.getCause();
        assertThat(exc, instanceOf(RunGameTask.GameStartError.class));
        assertThat(exc.getCause(), instanceOf(MockProcesses.OurIOException.class));
    }

    @SlowTest
    public void testExeNotFound() {
        // not disabled-on-Windows because all platforms should be capable of failing
        var gameTask = new RunGameTask(UnixProcesses.NO_SUCH_COMMAND);

        executor.submit(gameTask);

        var thrown = assertThrows(ExecutionException.class, gameTask::get);
        Throwable exc = thrown.getCause();
        assertThat(exc, instanceOf(RunGameTask.GameStartError.class));
        var cause = exc.getCause();
        assertThat(cause, instanceOf(IOException.class));
        assertThat(cause, anyOf(
                hasToString(containsString("No such file")),
                hasToString(containsString("The system cannot find the file specified"))
        ));
    }

    @SlowTest
    @DisabledOnOs(OS.WINDOWS)
    public void testTerminatedProcess() {
        var gameTask = new RunGameTask(new UnixProcesses.SelfDestructingProcess(5));

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
        // Matches {@link RunGameTask.START_MATCH}
        final String confirmedStart = "terasology.engine.TerasologyEngine - Initialization completed";

        final List<String> mockOutputLines = List.of(
                "some babble\n",
                confirmedStart + "\n",
                "more babble\n",
                "have a nice day etc\n"
        );

        // A record of observed events (thread-safe).
        final Queue<Happenings.ValuedHappening<Boolean>> actualHistory = new ConcurrentLinkedQueue<>();

        final List<Happenings.ValuedHappening<Boolean>> expectedHistory = List.of(
                Happenings.PROCESS_OUTPUT_LINE.val(),
                Happenings.PROCESS_OUTPUT_LINE.val(),
                Happenings.TASK_VALUE_SET.val(true),  // that line was the confirmedStart event!
                Happenings.PROCESS_OUTPUT_LINE.val(),
                Happenings.PROCESS_OUTPUT_LINE.val(),
                Happenings.TASK_COMPLETED.val()
        );

        final Runnable handleLineSent = () -> Platform.runLater(
                () -> actualHistory.add(Happenings.PROCESS_OUTPUT_LINE.val()));

        // This makes our "process," which streams out its lines and runs the callback after each.
        final Process lineAtATimeProcess = new MockProcesses.OneLineAtATimeProcess(
                spyingIterator(mockOutputLines, handleLineSent));

        // RunGameTask, the code under test, finally appears.
        final var gameTask = new RunGameTask(() -> lineAtATimeProcess);

        // Arrange to record when things happen.
        gameTask.valueProperty().addListener(
                (x, y, newValue) -> actualHistory.add(Happenings.TASK_VALUE_SET.val(newValue))
        );

        gameTask.addEventHandler(
                WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                (event) -> actualHistory.add(Happenings.TASK_COMPLETED.val())
        );

        // Act!
        executor.submit(gameTask);

        var actualReturnValue = gameTask.get();  // task.get blocks until it has run to completion

        // Assert!
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(actualReturnValue);

        assertIterableEquals(expectedHistory, actualHistory, renderColumns(actualHistory, expectedHistory));
    }

    @Test
    public void testFastExitDoesNotResultInSuccess() {
        final List<String> mockOutputLines = List.of(
                "this is a line from some process\n",
                "oh, everything is over already, goodbye"
        );

        // A record of observed events (thread-safe).
        final Queue<Happenings.ValuedHappening<Boolean>> actualHistory = new ConcurrentLinkedQueue<>();

        final List<Happenings.ValuedHappening<Boolean>> expectedHistory = List.of(
                Happenings.PROCESS_OUTPUT_LINE.val(),
                Happenings.PROCESS_OUTPUT_LINE.val(),
                Happenings.TASK_FAILED.val()
        );

        final Runnable handleLineSent = () -> Platform.runLater(
                () -> actualHistory.add(Happenings.PROCESS_OUTPUT_LINE.val()));

        // This makes our "process," which streams out its lines and runs the callback after each.
        final Process lineAtATimeProcess = new MockProcesses.OneLineAtATimeProcess(
                spyingIterator(mockOutputLines, handleLineSent));

        // RunGameTask, the code under test, finally appears.
        final var gameTask = new RunGameTask(() -> lineAtATimeProcess);

        // Arrange to record when things happen.
        gameTask.valueProperty().addListener(
                (x, y, newValue) -> actualHistory.add(Happenings.TASK_VALUE_SET.val(newValue))
        );

        gameTask.addEventHandler(
                WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                (event) -> actualHistory.add(Happenings.TASK_COMPLETED.val())
        );
        gameTask.addEventHandler(
                WorkerStateEvent.WORKER_STATE_FAILED,
                (event) -> actualHistory.add(Happenings.TASK_FAILED.val())
        );

        // Act!
        executor.submit(gameTask);

        var thrown = assertThrows(ExecutionException.class, gameTask::get);

        WaitForAsyncUtils.waitForFxEvents();
        assertThat(thrown.getCause(), instanceOf(RunGameTask.GameExitTooSoon.class));

        assertIterableEquals(expectedHistory, actualHistory, renderColumns(actualHistory, expectedHistory));
    }

    public static <T> Supplier<String> renderColumns(Iterable<T> actualIterable, Iterable<T> expectedIterable) {
        return () -> {
            var outputs = new StringBuilder(256);

            var expectedIter = expectedIterable.iterator();
            var actualIter = actualIterable.iterator();
            var ended = "░".repeat(20);

            outputs.append(String.format("%27s\t%s\n", "Expected", "Actual"));
            do {
                var expected = expectedIter.hasNext() ? expectedIter.next() : ended;
                var actual = actualIter.hasNext() ? actualIter.next() : ended;
                var compared = expected.equals(actual) ? " " : "❌";

                outputs.append(String.format("%27s %s %s\n", expected, compared, actual));
            } while (expectedIter.hasNext() || actualIter.hasNext());

            return outputs.toString();
        };
    }


    /**
     * An Iterator that runs the given callback every iteration.
     *
     * @param list   to be iterated over
     * @param onNext to be called each iteration
     */
    public static <T> Iterator<T> spyingIterator(List<T> list, Runnable onNext) {
        return list.stream().takeWhile(string -> {
            onNext.run();
            return true;
        }).iterator();
    }

    /**
     * Things that happen in RunGameTask that we want to make assertions about.
     */
    enum Happenings {
        PROCESS_OUTPUT_LINE,
        TASK_VALUE_SET,
        TASK_COMPLETED,
        TASK_FAILED;

        <T> ValuedHappening<T> val(T value) {
            return new ValuedHappening<>(this, value);
        }

        <T> ValuedHappening<T> val() {
            return new ValuedHappening<>(this, null);
        }

        static final class ValuedHappening<T> extends Pair<Happenings, T> {
            private ValuedHappening(final Happenings key, final T value) {
                super(key, value);
            }
        }
    }


    static class NonTimingGameTask extends RunGameTask {
        NonTimingGameTask(final Callable<Process> starter) {
            super(starter);
        }

        @Override
        protected void startTimer() {
            // no timers here.
        }
    }
}
