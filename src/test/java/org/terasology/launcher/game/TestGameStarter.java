// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.launcher.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.terasology.launcher.util.JavaHeapSize;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.terasology.launcher.Matchers.hasItemsFrom;

public class TestGameStarter {
    static final String JAVA_ARG_1 = "-client";
    static final String JAVA_ARG_2 = "--enable-preview";
    static final String GAME_ARG_1 = "--no-splash";
    static final String GAME_DIR = "game";
    static final String GAME_DATA_DIR = "game_data";
    static final JavaHeapSize HEAP_MIN = JavaHeapSize.NOT_USED;
    static final JavaHeapSize HEAP_MAX = JavaHeapSize.GB_4;
    static final Level LOG_LEVEL = Level.INFO;

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

    private GameStarter newStarter() throws IOException {
        return newStarter(Path.of("libs", "Terasology.jar"));
    }

    private GameStarter newStarter(Path relativeGameJarPath) throws IOException {
        return new GameStarter(new StubInstallation(gamePath, relativeGameJarPath),
                gameDataPath, HEAP_MIN, HEAP_MAX, javaParams, gameParams, LOG_LEVEL);
    }

    @Test
    public void testConstruction() throws IOException {
        GameStarter starter = newStarter();
        assertNotNull(starter);
    }

    @Test
    public void testJre() throws IOException {
        GameStarter task = newStarter();
        // This is the sort of test where the code under test and the expectation are just copies
        // of the same source. But since there's a plan to separate the launcher runtime from the
        // game runtime, the runtime location seemed like a good thing to specify in its own test.
        assertTrue(task.getRuntimePath().startsWith(Path.of(System.getProperty("java.home"))));
    }

    static Stream<Arguments> provideJarPaths() {
        return Stream.of(
                Arguments.of(Path.of("libs", "Terasology.jar")),
                Arguments.of(Path.of("Terasology-5.2.0-SNAPSHOT", "lib", "Terasology.jar"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideJarPaths")
    public void testBuildProcess(Path jarRelativePath) throws IOException {
        GameStarter starter = newStarter(jarRelativePath);
        ProcessBuilder processBuilder = starter.processBuilder;
        final Path gameJar = gamePath.resolve(jarRelativePath);

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
