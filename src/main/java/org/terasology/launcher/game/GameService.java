// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javafx.concurrent.Service;
import javafx.concurrent.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.platform.UnsupportedPlatformException;
import org.terasology.launcher.settings.Settings;

import java.io.IOException;
import java.util.concurrent.Executors;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * This service starts and monitors the game process.
 * <p>
 * Its {@linkplain #GameService() constructor} requires no arguments. Use {@link #start(GameInstallation, Settings)} to
 * start the game process; the zero-argument form of {@code start()} will not have enough information.
 * <p>
 * The Boolean value of this service is true when it believes the game process has started <em>successfully.</em>
 * There will be some time between when this service is started and when that value is set. It can be observed on
 * {@link #valueProperty()} or retrieved as {@link #getValue()}.
 * <p>
 * The {@link Worker} interface of this Service provides a proxy to its current {@link RunGameTask}.
 * Many of the methods the Worker interface defines are not used by this task type. In particular, the
 * {@link Worker#progressProperty() progress} and {@link Worker#workDoneProperty() workDone} properties have no
 * information for you that reflect the state of the game process.
 * <p>
 * This service will be in {@link State#RUNNING RUNNING} state as long as the game process is live. It enters
 * {@link State#SUCCEEDED SUCCEEDED} after the game process exits with no error code, or {@link State#FAILED FAILED} if
 * the game failed to start or it terminates with an error. Whether it succeeded or failed, the service will then
 * reset to {@link State#READY READY} to be started again.
 * <p>
 * This service {@linkplain #cancel() does not support cancellation}.
 * <ul>
 *   <li>For details on how the arguments to the process are constructed, see the source for {@link GameStarter}.
 *   <li>For details on how output from the game process is treated, see {@link RunGameTask}.
 * </ul>
 */
public class GameService extends Service<Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private GameInstallation gamePath;
    private Settings settings;

    public GameService() {
        setExecutor(Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("GameService-%d")
                        .setDaemon(true)
                        .setUncaughtExceptionHandler(this::exceptionHandler)
                        .build()
        ));
    }

    /**
     * Start a new game process with these settings.
     *
     * @param gameInstallation the directory under which we will find libs/Terasology.jar, also used as the process's
     *                     working directory
     * @param settings     supplies other settings relevant to configuring a process
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public void start(GameInstallation gameInstallation, Settings settings) {
        this.gamePath = gameInstallation;
        this.settings = settings;

        start();
    }

    /**
     * Use {@link #start(GameInstallation, Settings)} instead.
     * <p>
     * It is an error to call this method before providing the configuration.
     */
    @Override
    public void start() {
        super.start();
    }

    /**
     * Cancellation is unsupported. Do not attempt this method.
     * <p>
     * Rationale: We do not terminate a running game process, and we don't want to lose our thread keeping track of
     * the process while it's still live. If we did, our “is a game already running?” logic would fail.
     * <p>
     * If you are using this with some kind of generic Service Manager that always invokes this method as part of an
     * orderly shutdown, that could be a good reason to change this from throwing an exception to logging a warning.
     * Until then, this fails loudly so you won't call this method thinking it does something and then become confused
     * when nothing happens.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean cancel() {
        throw new UnsupportedOperationException("GameService does not cancel.");
    }

    /**
     * Restarting is unsupported. Do not attempt this method.
     * <p>
     * See {@link #cancel()} for rationale.
     */
    @Override
    public void restart() {
        super.restart();
    }

    /**
     * Creates a new task to run the game with the current settings.
     * <p>
     * This class's configuration fields <em>must</em> be set before this is called.
     *
     * @throws com.google.common.base.VerifyException when fields are unset
     * @throws RuntimeException                       when required files in the game directory are missing or inaccessible
     */
    @Override
    protected RunGameTask createTask() throws GameVersionNotSupportedException {
        verifyNotNull(settings);

        GameStarter starter;
        try {
            starter = new GameStarter(verifyNotNull(gamePath), settings.gameDataDirectory.get(),
                    settings.minHeapSize.get(), settings.maxHeapSize.get(),
                    settings.userJavaParameters.get(),
                    settings.userGameParameters.get(),
                    settings.logLevel.get());
        } catch (IOException | UnsupportedPlatformException e) {
            throw new RuntimeException("Error using this as a game directory: " + gamePath, e);
        }
        return new RunGameTask(starter);
    }

    /**
     * After a task completes, reset to ready for the next.
     */
    @Override
    protected void succeeded() {
        reset();  // Ready to go again!
    }

    /**
     * Checks to see if the failure left any exceptions behind, then resets to ready.
     */
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

    private void exceptionHandler(Thread thread, Throwable thrown) {
        logger.error("Unhandled exception", thrown);
    }
}
