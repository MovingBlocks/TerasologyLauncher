// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import java.util.Date;

/**
 * Data container for metadata associated with a game release.
 *
 * The metadata in this class is either relevant for displaying more information to the user (e.g., {@code changelog},
 * {@code timestamp}) or for managing and starting the game itself (e.g., {@code isLwjgl3}).
 */
public class ReleaseMetadata {
    private final String changelog;
    private final Date timestamp;
    private final boolean isLwjgl3;

    public ReleaseMetadata(String changelog, Date timestamp, boolean isLwjgl3) {
        this.changelog = changelog;
        this.timestamp = timestamp;
        this.isLwjgl3 = isLwjgl3;
    }

    /**
     * The change log of this release as a single markdown string.
     */
    public String getChangelog() {
        return changelog;
    }

    /**
     * The timestamp of the CI run that built this release.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Whether this release uses LWJGL v3 or not.
     */
    public boolean isLwjgl3() {
        return isLwjgl3;
    }
}
