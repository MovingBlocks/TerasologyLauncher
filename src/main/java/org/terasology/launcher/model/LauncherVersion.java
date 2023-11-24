// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import com.google.common.io.Resources;
import org.semver4j.Semver;
import org.semver4j.SemverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class LauncherVersion {

    private static final Logger logger = LoggerFactory.getLogger(LauncherVersion.class);
    private static final String VERSION_INFO_FILE = "/org/terasology/launcher/version.txt";
    private static LauncherVersion instance;

    private final Semver semver;

    private LauncherVersion(Semver semver) {
        this.semver = semver;
    }

    //TODO: Should this be instantiated once at startup and then passed to respective classes? Prepare for dependency injection
    public static synchronized LauncherVersion getInstance() {
        if (instance == null) {
            String version = "";
            Semver semver = null;
            try {
                version = Resources.toString(Resources.getResource(LauncherVersion.class, VERSION_INFO_FILE), StandardCharsets.UTF_8);
                semver = new Semver(version);
            } catch (SemverException e) {
                logger.error("Failed to load launcher version info from '{}': Invalid semver '{}'.", VERSION_INFO_FILE, version, e);
            } catch (IOException e) {
                logger.error("Loading launcher version info from '{}' failed.", VERSION_INFO_FILE, e);
            }
            instance = new LauncherVersion(semver);
        }
        return instance;
    }

    public String getDisplayName() {
        return Optional.ofNullable(semver).map(Semver::getVersion).orElse("n/a");
    }

    public Semver getSemver() {
        return semver;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
