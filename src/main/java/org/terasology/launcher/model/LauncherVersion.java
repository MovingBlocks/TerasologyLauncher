// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import com.vdurmont.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class LauncherVersion {

    private static final Logger logger = LoggerFactory.getLogger(LauncherVersion.class);

    private static final String VERSION_INFO_FILE = "/org/terasology/launcher/versionInfo.properties";

    private static final String VERSION = "version";

    private static final String DEFAULT_VALUE = "";

    private static LauncherVersion instance;

    private final String version;
    private final Semver semver;

    private LauncherVersion(Properties properties) {
        version = properties.getProperty(VERSION, DEFAULT_VALUE);

        semver = new Semver(version);
    }

    //TODO: Should this be instantiated once at startup and then passed to respective classes? Prepare for dependency injection
    public static synchronized LauncherVersion getInstance() {
        if (instance == null) {
            final Properties properties = new Properties();
            try (InputStream input = LauncherVersion.class.getResourceAsStream(VERSION_INFO_FILE)) {
                properties.load(input);
            } catch (IOException e) {
                logger.error("Loading launcher version info from '{}' failed.", VERSION_INFO_FILE, e);
            }
            instance = new LauncherVersion(properties);
        }
        return instance;
    }

    public String getDisplayName() {
        return version;
    }

    public Semver getSemver() {
        return semver;
    }

    @Override
    public String toString() {
        return version;
    }
}
