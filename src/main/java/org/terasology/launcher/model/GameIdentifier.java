// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import com.vdurmont.semver4j.Semver;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    static final Pattern PATTERN = Pattern.compile("GameIdentifier\\{version='(?<version>.*)', build=(?<build>\\w+), profile=(?<profile>\\w+)\\}");

    final String version;
    final Build build;
    final Profile profile;
    final Semver engineVersion;

    public GameIdentifier(String version, Semver engineVersion, Build build, Profile profile) {
        this.version = version;
        this.engineVersion = engineVersion;
        this.build = build;
        this.profile = profile;
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
        //TODO: move serialization somewhere else (probably to LauncherSettings?)
        return "GameIdentifier{" +
                "version='" + version + '\'' +
                ", build=" + build +
                ", profile=" + profile +
                '}';
    }

    public static GameIdentifier fromString(String identifier) {
        //TODO: move deserialization somewhere else (probably to LauncherSettings?)
        Matcher matcher = PATTERN.matcher(identifier);
        if (matcher.find()) {
            final Build build = Build.valueOf(matcher.group("build"));
            final Profile profile = Profile.valueOf(matcher.group("profile"));
            final String version = matcher.group("version");
            Semver engineVersion = new Semver(version, Semver.SemverType.IVY); // FIXME: assumes engineVersion==version!
            return new GameIdentifier(version, engineVersion, build, profile);
        }
        return null;
    }
}
