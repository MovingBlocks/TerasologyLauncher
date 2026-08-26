// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.remote;

import org.jspecify.annotations.Nullable;

public final class DownloadException extends RuntimeException {

    private static final long serialVersionUID = -6597132435025903769L;

    public DownloadException() {
        super();
    }

    public DownloadException(String message) {
        super(message);
    }

    // cause is @Nullable since Throwable.getCause() is - Throwable(String, Throwable) treats null as "none" anyway.
    public DownloadException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public DownloadException(@Nullable Throwable cause) {
        super(cause);
    }
}
