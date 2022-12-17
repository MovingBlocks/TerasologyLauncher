// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Provides factory methods for timers that are manipulated from and execute their action on the JavaFX application
 * thread.
 * <p>
 * Derived from ReactFX.
 *
 * @see <a href="https://github.com/TomasMikula/ReactFX/blob/v2.0-M5/reactfx/src/main/java/org/reactfx/util/FxTimer.java">ReactFX</a>
 */
public final class FxTimer {
    private final Duration actionTime;
    private final Timeline timeline;
    private final Runnable action;

    private long seq;


    private FxTimer(java.time.Duration actionTime, java.time.Duration period, Runnable action, int cycles) {
        this.actionTime = Duration.millis(actionTime.toMillis());
        this.timeline = new Timeline();
        this.action = action;

        timeline.getKeyFrames().add(new KeyFrame(this.actionTime)); // used as placeholder
        if (!period.equals(actionTime)) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(period.toMillis())));
        }

        timeline.setCycleCount(cycles);
    }

    /**
     * Prepares a (stopped) timer that lasts for {@code delay} and whose action runs when timer <em>ends</em>.
     */
    public static FxTimer create(java.time.Duration delay, Runnable action) {
        return new FxTimer(delay, delay, action, 1);
    }

    /**
     * Equivalent to {@code create(delay, action).restart()}.
     */
    public static FxTimer runLater(java.time.Duration delay, Runnable action) {
        FxTimer timer = create(delay, action);
        timer.restart();
        return timer;
    }

    public void restart() {
        stop();
        long expected = seq;
        timeline.getKeyFrames().set(0, new KeyFrame(actionTime, ae -> {
            if (seq == expected) {
                action.run();
            }
        }));
        timeline.play();
    }

    public void stop() {
        timeline.stop();
        ++seq;
    }
}
