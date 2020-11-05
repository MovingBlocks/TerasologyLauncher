// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import java.net.URL;

/**
 * A game release describes a game artefact (asset) that can be downloaded and installed by the launcher.
 * <p>
 * Each game release is uniquely identified by the {@link GameIdentifier} {@code id} and provides a URL from which the
 * artefact can be retrieved.
 * </p>
 */
public class GameRelease {
    final GameIdentifier id;
    final URL url;

    public GameRelease(GameIdentifier id, URL url) {
        this.id = id;
        this.url = url;
    }
}
