// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * A game release describes a (remote) game artifact (asset) that can be downloaded and installed by the launcher.
 * <p>
 * Each game release is uniquely identified by the {@link GameIdentifier} {@code id} and provides a URL from which the
 * artifact can be retrieved.
 * </p>
 * <ul>
 *     <li>TODO: define what the <b>artifact</b> is, and what requirements/restrictions there are</li>
 * </ul>
 */
public class GameRelease {
    final GameIdentifier id;
    final ReleaseMetadata releaseMetadata;
    final URL url;

    public GameRelease(GameIdentifier id, URL url, ReleaseMetadata releaseMetadata) {
        this.id = id;
        this.url = url;
        this.releaseMetadata = releaseMetadata;
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
        return releaseMetadata.getChangelog();
    }

    public Date getTimestamp() {
        return releaseMetadata.getTimestamp();
    }

    public boolean isLwjgl3() {
        return releaseMetadata.isLwjgl3();
    }
}
