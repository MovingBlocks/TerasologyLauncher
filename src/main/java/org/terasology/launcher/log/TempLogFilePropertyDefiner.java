// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.log;

import ch.qos.logback.core.PropertyDefinerBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Define a temporary log file name that can be used by Logback properties.
 */
public class TempLogFilePropertyDefiner extends PropertyDefinerBase {

    private static TempLogFilePropertyDefiner instance;

    private Path logFile;

    private String prefix;
    private String suffix;

    private boolean failed;

    /**
     * Default constructor (necessary) - called by Logback.
     */
    public TempLogFilePropertyDefiner() {
        if (instance != null) {
            throw new IllegalStateException("This class must not be instantiated twice");
        }

        instance = this;    //NOPMD(AssignmentToNonFinalStatic)
    }

    public static TempLogFilePropertyDefiner getInstance() {
        return instance;
    }

    @Override
    public String getPropertyValue() {

        // Don't try again, if it failed before
        if (failed) {
            return null;
        }

        return getLogFile().toString();
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix string for generating the file's name.
     *
     * @param prefix the prefix string to be used in generating the file's name; may be null
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Set the suffix string for generating the file's name.
     *
     * @param suffix the suffix string to be used in generating the file's name; may be null, in which case ".tmp" is used
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Returns the temporary log file.
     *
     * @return the log file
     */
    public Path getLogFile() {
        if (logFile == null) {
            try {
                logFile = Files.createTempFile(prefix, suffix);
                System.out.println("Using log file " + logFile);
            } catch (IOException e) {
                failed = true;
                // cannot use logger
                e.printStackTrace();
            }
        }

        return logFile;
    }
}
