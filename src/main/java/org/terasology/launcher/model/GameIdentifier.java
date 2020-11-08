// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import com.vdurmont.semver4j.Semver;

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
    final String version;
    final Build build;
    final Profile profile;
    final Semver semver;

    public GameIdentifier(String version, Build build, Profile profile, Semver semver) {
        this.version = version;
        this.build = build;
        this.profile = profile;
        this.semver = semver;
    }

    public String getVersion() {
        return version;
    }

    public Build getBuild() {
        return build;
    }

    public Profile getProfile() {
        return profile;
    }

    public Semver getSemver() {
        return semver;
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
        return version.equals(that.version)
                && build == that.build
                && profile == that.profile;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, build, profile);
    }

    @Override
    public String toString() {
        return profile + " @ " + version + "+" + build + " (" + semver + ")";
    }

}
