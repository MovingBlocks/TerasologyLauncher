// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.launcher.util;

public enum GameDataDirectoryNames {
    SAVED_WORLDS("SAVED_WORLDS"),
    WORLDS("worlds"),
    SAVES("saves"),
    SCREENS("screens"),
    SCREENSHOTS("screenshots");

    private final String name;

    GameDataDirectoryNames(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
