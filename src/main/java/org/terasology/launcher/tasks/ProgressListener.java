// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.tasks;

public interface ProgressListener {

    void update();

    void update(int progress);

    boolean isCancelled();

}
