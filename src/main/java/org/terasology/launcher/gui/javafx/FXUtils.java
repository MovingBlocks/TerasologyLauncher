/*
 * Copyright 2019 MovingBlocks
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

package org.terasology.launcher.gui.javafx;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

final class FXUtils {

    private FXUtils() { }

    /**
     * Creates a {@link javafx.animation.ScaleTransition} with the given factor for the specified node element.
     *
     * @param factor the scaling factor
     * @param node   the target node
     * @return a transition object
     */
    static ScaleTransition createScaleTransition(final double factor, final Node node) {
        final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), node);
        scaleTransition.setFromX(node.getScaleX());
        scaleTransition.setFromY(node.getScaleY());
        scaleTransition.setToX(factor);
        scaleTransition.setToY(factor);
        return scaleTransition;
    }

    /**
     * Provides factory methods for timers that are manipulated from and execute
     * their action on the JavaFX application thread.
     *
     * Adopted from https://github.com/TomasMikula/ReactFX/blob/v2.0-M5/reactfx/src/main/java/org/reactfx/util/FxTimer.java
     * by BSD license.
     */
    public static class FxTimer {

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

        /**
         * Prepares a (stopped) timer that lasts for {@code interval} and that executes the given action periodically
         * when the timer <em>ends</em>.
         */
        public static FxTimer createPeriodic(java.time.Duration interval, Runnable action) {
            return new FxTimer(interval, interval, action, Animation.INDEFINITE);
        }

        /**
         * Equivalent to {@code createPeriodic(interval, action).restart()}.
         */
        public static FxTimer runPeriodically(java.time.Duration interval, Runnable action) {
            FxTimer timer = createPeriodic(interval, action);
            timer.restart();
            return timer;
        }

        /**
         * Prepares a (stopped) timer that lasts for {@code interval} and that executes the given action periodically
         * when the timer <em>starts</em>.
         */
        public static FxTimer createPeriodic0(java.time.Duration interval, Runnable action) {
            return new FxTimer(java.time.Duration.ZERO, interval, action, Animation.INDEFINITE);
        }

        /**
         * Equivalent to {@code createPeriodic0(interval, action).restart()}.
         */
        public static FxTimer runPeriodically0(java.time.Duration interval, Runnable action) {
            FxTimer timer = createPeriodic0(interval, action);
            timer.restart();
            return timer;
        }

        private final Duration actionTime;
        private final Timeline timeline;
        private final Runnable action;

        private long seq = 0;

        private FxTimer(java.time.Duration actionTime, java.time.Duration period, Runnable action, int cycles) {
            this.actionTime = Duration.millis(actionTime.toMillis());
            this.timeline = new Timeline();
            this.action = action;

            timeline.getKeyFrames().add(new KeyFrame(this.actionTime)); // used as placeholder
            if (period != actionTime) {
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(period.toMillis())));
            }

            timeline.setCycleCount(cycles);
        }

        public void restart() {
            stop();
            long expected = seq;
            timeline.getKeyFrames().set(0, new KeyFrame(actionTime, ae -> {
                if(seq == expected) {
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
}
