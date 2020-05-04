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

import org.apache.commons.io.input.NullInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spf4j.log.Level;
import org.spf4j.test.log.LogAssert;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.log.annotations.CollectLogs;
import org.spf4j.test.log.junit4.Spf4jTestLogJUnitRunner;
import org.spf4j.test.matchers.LogMatchers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.allOf;


@RunWith(Spf4jTestLogJUnitRunner.class)
public class TestRunGameTaskVintage {

    public TestLoggers testLogs;

    @Before
    public void setUp() {
        testLogs = TestLoggers.sys();
    }

    @Test
    @CollectLogs(minLevel = Level.TRACE)
    public void testGameOutput() throws InterruptedException {
        String[] gameOutputLines = {"LineOne", "LineTwo"};

        Process gameProcess = new HappyGameProcess(String.join("\n", gameOutputLines));

        RunGameTask gameTask = new RunGameTask(null);

        var hasGameOutputFormat = LogMatchers.hasFormatWithPattern("^Game output.*");

        LogAssert detailedExpectation = testLogs.expect(RunGameTask.class.getName(), Level.TRACE,
                allOf(hasGameOutputFormat, LogMatchers.hasArguments(gameOutputLines[0])),
                allOf(hasGameOutputFormat, LogMatchers.hasArguments(gameOutputLines[1]))
        );

        gameTask.monitorProcess(gameProcess);

        detailedExpectation.assertObservation();
    }

//    @Test
//    @Ignore
//    public void testGameExitSuccessful() {
//        // Run game process
//        GameRunner gameRunner = new GameRunner(gameProcess);
//        gameRunner.run();
//
//        // Make sure GameRunner logs that the game exited
//        verify((Logger) Whitebox.getInternalState(GameRunner.class, "logger"), atLeastOnce()).debug(
//                "Game closed with the exit value '{}'.",
//                0
//        );
//    }
//
//    @Test
//    @Ignore
//    public void testGameEarlyInterrupt() {
//        GameRunner gameRunner = new GameRunner(gameProcess);
//
//        // Simulate GameRunner running on a thread
//        Thread gameThread = mock(Thread.class);
//        when(Thread.currentThread()).thenReturn(gameThread);
//
//        // Simulate early game thread interruption
//        when(gameThread.isInterrupted()).thenReturn(true);
//
//        // Run game process
//        gameRunner.run();
//
//        // Make sure GameRunner logs that the game thread was interrupted
//        verify((Logger) Whitebox.getInternalState(GameRunner.class, "logger")).debug("Game thread interrupted.");
//    }
//
//    @Test
//    @Ignore
//    public void testGameLateInterrupt() throws Exception {
//        // Simulate late game thread interruption (while game is running)
//        when(gameProcess.waitFor()).thenThrow(new InterruptedException());
//
//        // Run game process
//        GameRunner gameRunner = new GameRunner(gameProcess);
//        gameRunner.run();
//
//        // Make sure GameRunner logs an error with an InterruptedException
//        verify((Logger) Whitebox.getInternalState(GameRunner.class, "logger"))
//                .error(eq("The game thread was interrupted!"),
//                        any(InterruptedException.class));
//    }
//
//    @Test
//    @Ignore
//    public void testGameOutputError() throws Exception {
//        // Simulate an invalid output stream (that throws an IOException when read from because it's unhappy with its life)
//        InputStream badStream = mock(InputStream.class);
//        when(badStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Unhappy with life!"));
//
//        when(gameProcess.getInputStream()).thenReturn(badStream);
//
//        // Run game process
//        GameRunner gameRunner = new GameRunner(gameProcess);
//        gameRunner.run();
//
//        // Make sure GameRunner logs an error with an IOException
//        verify((Logger) Whitebox.getInternalState(GameRunner.class, "logger"))
//                .error(eq("Could not read game output!"), any(IOException.class));
//    }


    static class HappyGameProcess extends Process {

        private final InputStream inputStream;

        HappyGameProcess() {
            inputStream = new NullInputStream(0);
        }

        HappyGameProcess(String processOutput) {
            inputStream = new ByteArrayInputStream(processOutput.getBytes());
        }

        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException("Stub.");
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public InputStream getErrorStream() {
            throw new UnsupportedOperationException("Stub; implement if we stop merging stdout and error streams.");
        }

        @Override
        public int waitFor() {
            return exitValue();
        }

        /**
         * Returns the exit value for the process.
         *
         * @return the exit value of the process represented by this
         * {@code Process} object.  By convention, the value
         * {@code 0} indicates normal termination.
         * @throws IllegalThreadStateException if the process represented
         *                                     by this {@code Process} object has not yet terminated
         */
        @Override
        public int exitValue() {
            return 0;
        }

        /**
         * Kills the process.
         * Whether the process represented by this {@code Process} object is
         * {@linkplain #supportsNormalTermination normally terminated} or not is
         * implementation dependent.
         * Forcible process destruction is defined as the immediate termination of a
         * process, whereas normal termination allows the process to shut down cleanly.
         * If the process is not alive, no action is taken.
         * <p>
         * The {@link CompletableFuture} from {@link #onExit} is
         * {@linkplain CompletableFuture#complete completed}
         * when the process has terminated.
         */
        @Override
        public void destroy() {

        }
    }
}
