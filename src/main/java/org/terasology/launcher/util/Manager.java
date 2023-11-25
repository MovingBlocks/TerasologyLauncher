// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import javafx.collections.ObservableSet;
import org.terasology.launcher.tasks.ProgressListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface Manager<T> {
    default CompletableFuture<Installation<T>> install(Downloadable<T> downloadable) throws IOException {
        return install(downloadable, new ProgressListener() {});
    }

    CompletableFuture<Installation<T>> install(Downloadable<T> downloadable, ProgressListener listener) throws IOException;

    void uninstall(T info) throws IOException;

    ObservableSet<Installation<T>> getInstalled();

    Installation<T> get(T info) throws FileNotFoundException;
}
