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

public final class Platform {

    private static Platform PLATFORM;

    private String os;
    private String arch;

    private Platform() {
        // TODO: consider using regex
        final String platformOs = System.getProperty("os.name").toLowerCase();
        if (platformOs.equals("linux")) {
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

    public String getOs() {
        return this.os;
    }

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
     * @return the operating system singleton
     */
    public static Platform getPlatform() {
        if (PLATFORM == null) {
            PLATFORM = new Platform();
        }

        return PLATFORM;
    }
}
