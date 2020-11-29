// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

public class LauncherStartFailedException extends Exception {

    private static final long serialVersionUID = -6096111978556806948L;

    public LauncherStartFailedException() {
    }

    public LauncherStartFailedException(String message) {
        super(message);
    }
}
