/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.util;

/**
 * A simplified representation of a computer platform as `os` and `arch`
 */
public final class Platform {
    
    private static final Platform PLATFORM = new Platform();

    private String os;
    private String arch;

    /**
     * Constructs platform information for the current host system.
     * Simplifies operating system name to one of `linux`, `mac`, `windows` if applicable.
     * Simplifies operating system architecture to one of `32` and `64` if applicable.
     */
    private Platform() {
        final String platformOs = System.getProperty("os.name").toLowerCase();
        // TODO: consider using regex
        if (platformOs.startsWith("linux")) {
            this.os = "linux";
        } else if (platformOs.startsWith("mac os")) {
            this.os = "mac";
        } else if (platformOs.startsWith("windows")) {
            this.os = "windows";
        } else {
            this.os = platformOs;
        }

        final String platformArch = System.getProperty("os.arch");
        if (platformArch.equals("x86_64") || platformArch.equals("amd64")) {
            this.arch = "64";
        } else if (platformArch.equals("x86")) {
            this.arch = "32";
        } else {
            this.arch = platformArch;
        }
    }

    /**
     * @return the simplified operating system name as platform os
     */
    public String getOs() {
        return this.os;
    }

    /**
     * @return the simplified operating system architecture as platform arch
     */
    public String getArch() {
        return this.arch;
    }

    public boolean isLinux() {
        return this.os.equals("linux");
    }

    public boolean isMac() {
        return this.os.equals("mac");
    }

    public boolean isWindows() {
        return this.os.equals("windows");
    }

    public String toString() {
        return "OS '" + this.os + "', arch '" + this.arch + "'";
    }

    /**
     * Get information on the host platform the launcher is currently running on.
     *
     * @return the platform
     */
    public static Platform getPlatform() {
        return PLATFORM;
    }
}
