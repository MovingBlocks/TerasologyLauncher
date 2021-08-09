// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import com.google.common.base.MoreObjects;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

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

    /**
     * Attempt to create a game identifier for a specific {@link Build} and {@link Profile} from a version info file.
     * <p>
     * The version info properties
     * <ul>
     *  <li> MUST contain a {@code displayVersion} entry (any string)
     *  <li> MUST contain a {@code engineVersion} entry holding a valid SemVer
     *  <li> MAY contain a {@code buildNumber}
     * </ul>
     * Other properties are ignored.
     * <p>
     * If the version info properties contain a non-empty {@code buildNumber} it is appended to the
     * {@code displayVersion} to form the {@link GameIdentifier#getDisplayVersion()}.
     *
     * @param versionInfo the Terasology distribution version info; must contain a {@code displayVersion} (any string)
     *                    and a valid SemVer in {@code engineVersion}
     * @param build       the build variant of the game release
     * @param profile     the build profile of the game release
     * @return Some identifier if the version info meets the requirements; empty otherwise
     */
    public static Optional<GameIdentifier> fromVersionInfo(Properties versionInfo, Build build, Profile profile) {
        var displayVersion = versionInfo.getProperty("displayVersion");
        if (displayVersion == null) {
            return Optional.empty();
        }
        // Append the build number to the display version to ensure that the version string is unique for two different
        // builds. This is necessary because the display version generated by the CI build is the same of subsequent
        // builds...
        var buildNumber = versionInfo.getProperty("buildNumber");
        if (buildNumber != null && !buildNumber.isEmpty()) {
            displayVersion += "+" + buildNumber;
        }
        return Optional.of(new GameIdentifier(displayVersion, build, profile));
    }

    public Semver getVersion() {
        // compatibility shim until we have GameIdentifier.version
        try {
            return new Semver(displayVersion, Semver.SemverType.LOOSE);
        } catch (SemverException e) {
            return new Semver("0.0.1-" + displayVersion, Semver.SemverType.LOOSE); // FIXME ASAP
        }
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
