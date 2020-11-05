// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

/**
 * Metadata related to game releases.
 * <p>
 * This data class contains all kind of meta information on a game release, e.g., the changelog to inform the user about
 * the content of the release. None of the metadata is essential for the launcher's functionality.
 * </p>
 */
public class ReleaseMetadata {
    final String changelog;
    final String displayName;
    final String sha256Checksum;

    public ReleaseMetadata(String changelog, String displayName, String sha256Checksum) {
        this.changelog = changelog;
        this.displayName = displayName;
        this.sha256Checksum = sha256Checksum;
    }

    /**
     * The changelog associated with the game release
     */
    public String getChangelog() {
        return changelog;
    }

    /**
     * The display name for the game release, e.g., "Terasology" or "Terasology Lite (nightly)".
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * The SHA256 checksum for the game release asset.
     */
    public String getSha256Checksum() {
        return sha256Checksum;
    }
}
