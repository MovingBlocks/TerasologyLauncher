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

    private static final String DATE_TIME = "dateTime";
    private static final String VERSION = "version";

    private static final String DEFAULT_VALUE = "";

    private static LauncherVersion instance;

    // Indicates whether this version info is 'empty' (usually indicates that the launcher is being run in a development environment)
    private final boolean isEmpty;
    private final String dateTime;
    private final String version;
    private final String stringRepresentation;
    private final Semver semver;

    private LauncherVersion(Properties properties) {
        isEmpty = properties.isEmpty();
        dateTime = properties.getProperty(DATE_TIME, DEFAULT_VALUE);
        version = properties.getProperty(VERSION, DEFAULT_VALUE);

        semver = new Semver(version);

        final StringBuilder stringRepresentationBuilder = new StringBuilder();
        stringRepresentationBuilder.append("[");
        stringRepresentationBuilder.append(DATE_TIME);
        stringRepresentationBuilder.append("=");
        stringRepresentationBuilder.append(dateTime);
        stringRepresentationBuilder.append(", ");
        stringRepresentationBuilder.append(VERSION);
        stringRepresentationBuilder.append("=");
        stringRepresentationBuilder.append(version);
        stringRepresentationBuilder.append(", ");
        stringRepresentationBuilder.append("isEmpty");
        stringRepresentationBuilder.append("=");
        stringRepresentationBuilder.append(isEmpty);
        stringRepresentationBuilder.append("]");

        stringRepresentation = stringRepresentationBuilder.toString();
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

    //TODO: is this used or needed?
    public boolean isEmpty() {
        return isEmpty;
    }

    //TODO: is this used or needed?
    public String getDateTime() {
        return dateTime;
    }

    //TODO: is this used or needed?
    public String getVersion() {
        return version;
    }

    public String getDisplayName() {
        return version;
    }

    public Semver getSemver() {
        return semver;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }
}
