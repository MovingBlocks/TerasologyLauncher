// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import org.jspecify.annotations.Nullable;
import org.terasology.launcher.remote.RemoteResource;

import java.net.URL;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

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
public class GameRelease implements RemoteResource<GameIdentifier> {
    final GameIdentifier id;
    final ReleaseMetadata releaseMetadata;
    // Null for a dummy release synthesized from a local install not in any online listing - no remote asset.
    final @Nullable URL url;

    public GameRelease(GameIdentifier id, @Nullable URL url, ReleaseMetadata releaseMetadata) {
        this.id = id;
        this.url = url;
        this.releaseMetadata = releaseMetadata;
    }

    public GameIdentifier getId() {
        return id;
    }

    @Override
    public @Nullable URL getUrl() {
        return url;
    }

    @Override
    public String getFilename() {
        String profileString = id.getProfile().toString().toLowerCase(Locale.ROOT);
        String versionString = id.getDisplayVersion();
        String buildString = id.getBuild().toString().toLowerCase(Locale.ROOT);
        return "terasology-" + profileString + "-" + versionString + "-" + buildString + ".zip";
    }

    @Override
    public GameIdentifier getInfo() {
        return id;
    }

    /**
     * The changelog associated with the game release
     */
    public String getChangelog() {
        return releaseMetadata.getChangelog();
    }

    public Instant getTimestamp() {
        return releaseMetadata.getTimestamp();
    }

    @Override
    public String toString() {
        return id.getDisplayVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GameRelease other)) {
            return false;
        }

        boolean sameId = this.id.equals(other.id);
        boolean sameTimestamp = (this.releaseMetadata == null && other.releaseMetadata == null) 
            || (this.releaseMetadata != null && other.releaseMetadata != null 
                && this.releaseMetadata.getTimestamp().equals(other.releaseMetadata.getTimestamp()));

        return  sameId && sameTimestamp;
    }

        @Override
    public int hashCode() {
        return Objects.hash(id, releaseMetadata.getTimestamp());
    }
}
