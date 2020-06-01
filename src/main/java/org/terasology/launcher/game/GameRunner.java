/*
 * Copyright 2016 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class GameRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GameRunner.class);

    private final Process p;

    public GameRunner(final Process p) {
        this.p = p;
    }

    @Override
    public void run() {
        try {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
                String line;
                do {
                    line = r.readLine();
                    logger.trace("Game output: {}", line);
                } while (!isInterrupted() && line != null);
            }
            if (isInterrupted()) {
                logger.debug("Game thread interrupted.");
                return;
            }
            int exitValue = -1;
            try {
                exitValue = p.waitFor();
            } catch (InterruptedException e) {
                logger.error("The game thread was interrupted!", e);
            }
            logger.debug("Game closed with the exit value '{}'.", exitValue);
        } catch (IOException e) {
            logger.error("Could not read game output!", e);
        }
    }

    boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }
}
