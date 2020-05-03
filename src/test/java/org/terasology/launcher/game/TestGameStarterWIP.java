/*
 * Copyright 2020 MovingBlocks
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.terasology.launcher.Matchers.hasItemsFrom;

public class TestGameStarterWIP {
    static final String JAVA_ARG_1 = "-client";
    static final String JAVA_ARG_2 = "--enable-preview";
    static final String GAME_ARG_1 = "--no-splash";
    static final String GAME_DIR = "game";
    static final String GAME_DATA_DIR = "game_data";
    static final JavaHeapSize HEAP_MIN = JavaHeapSize.NOT_USED;
    static final JavaHeapSize HEAP_MAX = JavaHeapSize.GB_4;
    static final LogLevel LOG_LEVEL = LogLevel.INFO;

    private final FileSystem fs = FileSystems.getDefault();
    private Path gamePath;
    private List<String> javaParams;
    private List<String> gameParams;
    private Path gameDataPath;

    @BeforeEach
    public void setup() {
        gamePath = fs.getPath(GAME_DIR);
        gameDataPath = fs.getPath(GAME_DATA_DIR);
        javaParams = List.of(JAVA_ARG_1, JAVA_ARG_2);
        gameParams = List.of(GAME_ARG_1);
    }

    @Test
    public void testConstruction() {
        GameStarterWIP starter = newStarter();
        assertNotNull(starter);
    }

    private GameStarterWIP newStarter() {
        return new GameStarterWIP(gamePath, gameDataPath, HEAP_MIN, HEAP_MAX, javaParams, gameParams, LOG_LEVEL);
    }

    @Test
    public void testJre() {
        GameStarterWIP task = newStarter();
        // This is the sort of test where the code under test and the expectation are just copies
        // of the same source. But since there's a plan to separate the launcher runtime from the
        // game runtime, the runtime location seemed like a good thing to specify in its own test.
        assertTrue(task.getRuntimePath().startsWith(Path.of(System.getProperty("java.home"))));
    }

    @Test
    public void testBuildProcess() {
        GameStarterWIP starter = newStarter();
        ProcessBuilder processBuilder = starter.processBuilder;
        final Path gameJar = gamePath.resolve(Path.of("libs", "Terasology.jar"));

        assertNotNull(processBuilder.directory());
        assertEquals(gamePath, processBuilder.directory().toPath());
        assertThat(processBuilder.command(), hasItem(gameJar.toString()));
        assertThat(processBuilder.command(), hasItemsFrom(gameParams));
        assertThat(processBuilder.command(), hasItemsFrom(javaParams));
        // TODO: heap min, heap max, log level
        // could parameterize this test for the things that are optional?
        // heap min, heap max, log level, gameParams and javaParams are all optional.
    }
}
