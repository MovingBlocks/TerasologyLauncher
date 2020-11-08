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
    final String changelog;
    final String sha256Checksum;

    public GameRelease(GameIdentifier id, URL url, String changelog, String sha256Checksum) {
        this.id = id;
        this.url = url;
        this.changelog = changelog;
        this.sha256Checksum = sha256Checksum;
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
    public String getChangelog() {
        return changelog;
    }

    /**
     * The SHA256 checksum for the game release asset.
     */
    public String getSha256Checksum() {
        return sha256Checksum;
    }

}
