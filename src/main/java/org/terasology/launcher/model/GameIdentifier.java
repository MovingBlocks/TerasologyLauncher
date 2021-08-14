// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Uniquely identify a game release.
 *
 * <p>
 * A game release is not only identified by its version, but also by the {@link Profile}, denoting what is bundled with
 * the release (i.e., only the bare-bone engine, the full-fledged Omega line-up, ...).
 * The {@code version} string and {@link Build} information identify a release of the respective profile similar to a
 * semantic version.
 * </p>
 *
 * @see <a href="https://semver.org">https://semver.org</a>
 */
public class GameIdentifier {

    private static final Logger logger = LoggerFactory.getLogger(GameIdentifier.class);

    final String displayVersion;
    final Build build;
    final Profile profile;

    public GameIdentifier(String displayVersion, Build build, Profile profile) {
        this.displayVersion = displayVersion;
        this.build = build;
        this.profile = profile;
    }

    public String getDisplayVersion() {
        return displayVersion;
    }

    public Build getBuild() {
        return build;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameIdentifier that = (GameIdentifier) o;
        return displayVersion.equals(that.displayVersion)
                && build == that.build
                && profile == that.profile;
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayVersion, build, profile);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("version", displayVersion)
                .add("build", build)
                .add("profile", profile)
                .toString();
    }
}
