// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;


public final class DownloadException extends RuntimeException {

    private static final long serialVersionUID = -6597132435025903769L;

    public DownloadException() {
        super();
    }

    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownloadException(Throwable cause) {
        super(cause);
    }
}
