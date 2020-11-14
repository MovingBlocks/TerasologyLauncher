// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import java.net.URL;
import java.util.Date;
import java.util.List;

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
    final List<String> changelog;
    final Date timestamp;

    public GameRelease(GameIdentifier id, URL url, List<String> changelog, Date timestamp) {
        this.id = id;
        this.url = url;
        this.changelog = changelog;
        this.timestamp = timestamp;
    }

    public GameIdentifier getId() {
        return id;
    }

    public URL getUrl() {
        return url;
    }

    /**
     * The changelog associated with the game release
     */
    public List<String> getChangelog() {
        return changelog;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
