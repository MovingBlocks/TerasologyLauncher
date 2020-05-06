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

import javafx.application.Platform;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spf4j.log.Level;
import org.spf4j.test.log.LogAssert;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.log.annotations.CollectLogs;
import org.spf4j.test.log.junit4.Spf4jTestLogJUnitRunner;
import org.spf4j.test.matchers.LogMatchers;

import static org.hamcrest.Matchers.allOf;


@RunWith(Spf4jTestLogJUnitRunner.class)
public class TestRunGameTaskVintage {

    public TestLoggers testLogs;

    @BeforeClass
    public static void setUpClass() {
        // If we don't use the full TestFX junit4 runner, we need to do something
        // to keep RunGameTask content when it sends updates to the Application Thread.
        Platform.startup(null);
    }

    @Before
    public void setUp() {
        testLogs = TestLoggers.sys();
    }

    @Test
    @CollectLogs(minLevel = Level.TRACE)
    public void testGameOutput() throws InterruptedException, RunGameTask.GameExitError {
        String[] gameOutputLines = {"LineOne", "LineTwo"};

        Process gameProcess = new MockProcesses.HappyGameProcess(String.join("\n", gameOutputLines));

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
}
