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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

public class TestRunGameTask {

    @Test
    public void testProcessKiller() throws InterruptedException, ExecutionException {
        RunGameTask gameTask = new RunGameTask(null);
        gameTask.starter = new SlowTicker();

        var executor = Executors.newSingleThreadExecutor();
        var result = executor.submit(gameTask);
        Thread.sleep(3000);
        fail(String.format("Bonk %s", result.get()));
        executor.shutdown();
        executor.awaitTermination(0, TimeUnit.NANOSECONDS);
    }

    static class SlowTicker implements IGameStarter {
        @Override
        public Process start() throws IOException {
            final Logger logger = LoggerFactory.getLogger(SlowTicker.class);
            logger.warn("Did you get this far?");
            var pb = new ProcessBuilder("/bin/bash", "-c",
                    "for i in $( seq 100 ) ; do echo $i ; sleep 1 ; done");
            var proc = pb.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.warn(" ⏲ Ticker PID {}", proc.pid());
            return proc;
        }
    }
}
