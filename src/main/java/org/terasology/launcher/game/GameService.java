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
import javafx.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.settings.BaseLauncherSettings;

import java.nio.file.Path;
import java.util.concurrent.Executors;

import static com.google.common.base.Verify.verifyNotNull;


public class GameService extends Service<Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private Path gamePath;
    private BaseLauncherSettings settings;

    public GameService() {
        setExecutor(Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("GameService-%d")
                    .setDaemon(true)
                    .setUncaughtExceptionHandler(this::exceptionHandler)
                    .build()
        ));
    }

    @SuppressWarnings("CheckStyle")
    public void start(Path gamePath, BaseLauncherSettings settings) {
        this.gamePath = gamePath;
        this.settings = settings;

        start();
    }

    @Override
    protected RunGameTask createTask() {
        verifyNotNull(settings);
        var starter = new GameStarter(verifyNotNull(gamePath), settings.getGameDataDirectory(),
                                      settings.getMaxHeapSize(), settings.getInitialHeapSize(),
                                      settings.getUserJavaParameterList(), settings.getUserGameParameterList(),
                                      settings.getLogLevel());
        return new RunGameTask(starter);
    }

    private void exceptionHandler(Thread thread, Throwable thrown) {
        logger.error("Unhandled exception", thrown);
    }

    @Override
    protected void succeeded() {
        reset();  // Ready to go again!
    }

    @Override
    protected void failed() {
        // "Uncaught" exceptions from javafx's Task are actually caught and kept in a property,
        // so if we want them logged we have to explicitly dig them out.
        var error = getException();
        if (error != null) {
            exceptionHandler(Thread.currentThread(), error);
        }
        reset();  // Ready to try again!
    }
}
