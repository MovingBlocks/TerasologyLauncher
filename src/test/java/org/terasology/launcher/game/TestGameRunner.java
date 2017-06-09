/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.launcher.game;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GameRunner.class, LoggerFactory.class, Thread.class})
public class TestGameRunner {

    /**
     * Passed around to GameRunners to simulate a running game process.
     */
    private Process gameProcess;

    @Before
    public void setup() throws Exception {
        mockStatic(LoggerFactory.class);
        mockStatic(Thread.class);

        Logger logger = mock(Logger.class);
        when(LoggerFactory.getLogger(any(Class.class))).thenReturn(logger);

        // Create fake game process to give to GameRunner
        gameProcess = mock(Process.class);
        resetGameProcess();
    }

    /**
     * Resets the simulated game process to "successful" behaviour for use in multiple tests.
     *
     * @throws Exception
     */
    private void resetGameProcess() throws Exception {
        when(gameProcess.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(gameProcess.waitFor()).thenReturn(0);
    }

    @Test
    public void testGameOutput() throws Exception {
        resetGameProcess();

        // Simulate game process outputting TEST_STRING
        String testString = "LineOne\nLineTwo";
        when(gameProcess.getInputStream()).thenReturn(new ByteArrayInputStream(testString.getBytes()));

        // Run game process
        GameRunner gameRunner = new GameRunner(gameProcess);
        gameRunner.run();

        // Make sure GameRunner logs TEST_STRING line 1
        verify((Logger) Whitebox.getInternalState(gameRunner, "logger")).trace(
                "Game output: {}",
                testString.substring(0, testString.indexOf("\n"))
        );

        // Make sure GameRunner logs TEST_STRING line 2
        verify((Logger) Whitebox.getInternalState(gameRunner, "logger")).trace(
                "Game output: {}",
                testString.substring(testString.indexOf("\n") + 1)
        );
    }

    @Test
    public void testGameExitSuccessful() throws Exception {
        resetGameProcess();

        // Run game process
        GameRunner gameRunner = new GameRunner(gameProcess);
        gameRunner.run();

        // Make sure GameRunner logs that the game exited
        verify((Logger) Whitebox.getInternalState(gameRunner, "logger"), atLeastOnce()).debug(
                "Game closed with the exit value '{}'.",
                0
        );
    }

    @Test
    public void testGameEarlyInterrupt() throws Exception {
        resetGameProcess();

        GameRunner gameRunner = new GameRunner(gameProcess);

        // Simulate GameRunner running on a thread
        Thread gameThread = mock(Thread.class);
        when(Thread.currentThread()).thenReturn(gameThread);

        // Simulate early game thread interruption
        when(gameThread.isInterrupted()).thenReturn(true);

        // Run game process
        gameRunner.run();

        // Make sure GameRunner logs that the game thread was interrupted
        verify((Logger) Whitebox.getInternalState(gameRunner, "logger")).debug("Game thread interrupted.");
    }

    @Test
    public void testGameLateInterrupt() throws Exception {
        resetGameProcess();

        // Simulate late game thread interruption (while game is running)
        when(gameProcess.waitFor()).thenThrow(new InterruptedException());

        // Run game process
        GameRunner gameRunner = new GameRunner(gameProcess);
        gameRunner.run();

        // Make sure GameRunner logs an error with an InterruptedException
        verify((Logger) Whitebox.getInternalState(gameRunner, "logger")).error(eq("The game thread was interrupted!"), any(InterruptedException.class));
    }

    @Test
    public void testGameOutputError() throws Exception {
        resetGameProcess();

        // Simulate an invalid output stream (that throws an IOException when read from because it's unhappy with its life)
        InputStream badStream = mock(InputStream.class);
        when(badStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Unhappy with life!"));

        when(gameProcess.getInputStream()).thenReturn(badStream);

        // Run game process
        GameRunner gameRunner = new GameRunner(gameProcess);
        gameRunner.run();

        // Make sure GameRunner logs an error with an IOException
        verify((Logger) Whitebox.getInternalState(gameRunner, "logger")).error(eq("Could not read game output!"), any(IOException.class));
    }
}
