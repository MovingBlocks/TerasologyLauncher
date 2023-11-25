// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.tasks;

public interface ProgressListener {

    default void update() {}

    default void update(int progress) {}

    default boolean isCancelled() {
        return false;
    }
}
