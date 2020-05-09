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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.terasology.launcher.packages.Package;
import org.terasology.launcher.settings.BaseLauncherSettings;

import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * TODO: interface for Controller to bind success/fail handlers to this, instead
 *         of re-binding to each Task.
 * TODO: would it help to make this a Guava Service or JavaFX Service?
 */
public class GameService {
    private final ThreadPoolExecutor executor;

    public GameService() {
        // Constructing a ThreadPoolExecutor instead of using Executors.newSingleThreadExecutor
        // because ExecutorService has no getActiveCount.
        executor = new ThreadPoolExecutor(
                1, 1,
                0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadFactoryBuilder()
                        .setNameFormat("GameService-%d")
                        .setDaemon(true)   // TODO: also UncaughtExceptionHandler
                        .build());
    }

    public boolean isRunning() {
        return executor.getActiveCount() > 0;
    }

    /**
     * Create a new task for these game settings.
     *
     * Returns a task that has not yet been started.
     *
     * TODO: This can probably be hidden from the public interface.
     *
     */
    public RunGameTask createTask(Package pkg, Path gamePath, BaseLauncherSettings settings) {
        return new RunGameTask(pkg, gamePath, settings);
    }

    public void execute(RunGameTask task) {
        executor.execute(task);
    }
}
