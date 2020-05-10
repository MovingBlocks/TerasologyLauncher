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
import org.terasology.launcher.packages.Package;
import org.terasology.launcher.settings.BaseLauncherSettings;

import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Verify.verifyNotNull;


public class GameService extends Service<Boolean> {
    private Package pkg;
    private Path gamePath;
    private BaseLauncherSettings settings;

    public GameService() {
        setExecutor(new ThreadPoolExecutor(
                1, 1,
                0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadFactoryBuilder()
                        .setNameFormat("GameService-%d")
                        .setDaemon(true)   // TODO: also UncaughtExceptionHandler
                        .build()));
    }

    @SuppressWarnings("CheckStyle")
    public void start(Package pkg, Path gamePath, BaseLauncherSettings settings) {
        this.pkg = pkg;
        this.gamePath = gamePath;
        this.settings = settings;

        // TODO: alternate success conditions
        //   - stayed alive long enough

        start();
    }

    @Override
    protected RunGameTask createTask() {
        return new RunGameTask(verifyNotNull(pkg), verifyNotNull(gamePath), verifyNotNull(settings));
    }

    @Override
    protected void succeeded() {
        reset();
    }

    @Override
    protected void failed() {
        reset();
    }
}
