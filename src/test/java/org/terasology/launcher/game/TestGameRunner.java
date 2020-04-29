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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.spf4j.log.Level;
import org.spf4j.test.log.LogAssert;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.log.annotations.CollectLogs;
import org.spf4j.test.log.annotations.ExpectLog;
import org.spf4j.test.log.annotations.PrintLogs;
import org.spf4j.test.log.junit4.Spf4jTestLogJUnitRunner;
import org.spf4j.test.matchers.LogMatchers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Spf4jTestLogJUnitRunner.class)
@PrepareForTest(GameRunner.class)
public class TestGameRunner {

    /**
     * Passed around to GameRunners to simulate a running game process.
     */
    private Process gameProcess;
    private TestLoggers testLog;

    @Before
    public void setup() throws Exception {
        mockStatic(Thread.class);

        testLog = TestLoggers.sys();

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
    // CollectLogs(TRACE) isn't necessary for this test's assertions.
    // Logs are shown on test failure, but the default doesn't include TRACE.
    // Because TRACE logs are relevant to this test we use this to include them in the output.
    @CollectLogs(minLevel = Level.TRACE)
    public void testGameOutput() throws Exception {
        resetGameProcess();

        // Simulate game process outputting TEST_STRING
        String testString = "LineOne\nLineTwo";
        when(gameProcess.getInputStream()).thenReturn(new ByteArrayInputStream(testString.getBytes()));

        // Can have checks be relatively loose:
        // With logs in any category, at TRACE or above,
        LogAssert broadExpectation = testLog.expect("", Level.TRACE,
                // expect a message that contains "LineOne"
                LogMatchers.hasMatchingMessage(containsString("LineOne")),
                // also a message that contains "LineTwo"
                LogMatchers.hasMatchingMessage(containsString("LineTwo"))
                // or to match the message exactly,
                // LogMatchers.hasMessage("LineTwo")
        );

        // You can also get very specific about how the log message is made.
        // We'll define a matcher to look specifically at records whose
        // format string begin with "Game output"
        var hasGameOutputFormat = LogMatchers.hasFormatWithPattern("^Game output.*");

        // These expectations are restricted specifically to logs sent to a logger named like this class.
        LogAssert detailedExpectation = testLog.expect(GameRunner.class.getName(), Level.TRACE,
                // Check its format against our matcher, and also that it has an argument that is exactly "LineOne"
                allOf(hasGameOutputFormat, LogMatchers.hasArguments("LineOne")),
                // ...and also for line two.
                allOf(hasGameOutputFormat, LogMatchers.hasArguments("LineTwo"))
        );

        // Log expectations must be set up *before* the logs are generated.
        // Now we can run the code under test:
        GameRunner gameRunner = new GameRunner(gameProcess);
        gameRunner.run();

        // And check to see if the assertions held.
        broadExpectation.assertObservation();
        detailedExpectation.assertObservation();

        // be sure to run those assertions! If you set expectations but never check them,
        // this test runner does *not* notice and warn you.
    }

    @Test
    // There's an annotation you can use for simple cases. It auto-asserts when the method finishes.
    @ExpectLog(level = Level.DEBUG, messageRegexp = "Game closed with the exit value '0'.")
    public void testGameExitSuccessful() {
        GameRunner gameRunner = new GameRunner(gameProcess);
        gameRunner.run();
    }

    @Test
    // The log-printer's default setting is to display all logged errors.
    // But we know the code under test will be logging an error and that
    // doesn't mean there's an error in the scenario. We can override the
    // settings for this logger to not print error logs from that class.
    //
    // The error logs will still be collected and displayed if the test fails.
    @PrintLogs(category = "org.terasology.launcher.game.GameRunner", minLevel = Level.OFF, ideMinLevel = Level.OFF, greedy = true)
    public void testGameEarlyInterrupt() throws Exception {
        resetGameProcess();

        GameRunner gameRunner = new GameRunner(gameProcess);

        // Simulate GameRunner running on a thread
        Thread gameThread = mock(Thread.class);
        when(Thread.currentThread()).thenReturn(gameThread);

        // Simulate early game thread interruption
        when(gameThread.isInterrupted()).thenReturn(true);

        var interruptionLogged = testLog.expect("", Level.DEBUG, LogMatchers.hasMessage("Game thread interrupted."));

        // Run game process
        gameRunner.run();

        interruptionLogged.assertObservation();
    }

    @Test
    @PrintLogs(category = "org.terasology.launcher.game.GameRunner", minLevel = Level.OFF, ideMinLevel = Level.OFF, greedy = true)
    public void testGameLateInterrupt() throws Exception {
        // Simulate late game thread interruption (while game is running)
        when(gameProcess.waitFor()).thenThrow(new InterruptedException());

        var loggedException = testLog.expect("", Level.ERROR, Matchers.allOf(
                LogMatchers.hasMatchingExtraThrowable(Matchers.instanceOf(InterruptedException.class)),
                LogMatchers.hasMessage("The game thread was interrupted!")
        ));

        // Run game process
        GameRunner gameRunner = new GameRunner(gameProcess);
        gameRunner.run();

        // Make sure GameRunner logs an error with an InterruptedException
        loggedException.assertObservation();
    }

    @Test
    @PrintLogs(category = "org.terasology.launcher.game.GameRunner", minLevel = Level.OFF, ideMinLevel = Level.OFF, greedy = true)
    public void testGameOutputError() throws Exception {
        // Simulate an invalid output stream (that throws an IOException when read from because it's unhappy with its life)
        InputStream badStream = mock(InputStream.class);
        when(badStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Unhappy with life!"));

        when(gameProcess.getInputStream()).thenReturn(badStream);

        var loggedException = testLog.expect("", Level.ERROR, Matchers.allOf(
                LogMatchers.hasMatchingExtraThrowable(Matchers.instanceOf(IOException.class)),
                LogMatchers.hasMessage("Could not read game output!")
        ));

        // Run game process
        GameRunner gameRunner = new GameRunner(gameProcess);
        gameRunner.run();

        // Make sure GameRunner logs an error with an IOException
        loggedException.assertObservation();
    }
}
