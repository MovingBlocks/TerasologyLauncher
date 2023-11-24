// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import java.util.Date;
import com.vdurmont.semver4j.Semver;

/**
 * Data container for metadata associated with a game release.
 *
 * The metadata in this class is relevant for displaying more information to the user, e.g., {@code changelog},
 * {@code timestamp}.
 */
public class ReleaseMetadata {
    private final String changelog;
    private final Date timestamp;
    private final Semver minJavaVersion;

    public ReleaseMetadata(String changelog, Date timestamp, Semver minJavaVersion) {
        this.changelog = changelog;
        this.timestamp = timestamp;
        this.minJavaVersion = minJavaVersion;
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

    public Semver getMinJavaVersion() {
        return minJavaVersion;
    }
}
