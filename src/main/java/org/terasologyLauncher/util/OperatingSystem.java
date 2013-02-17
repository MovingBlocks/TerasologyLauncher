/*
 * This file is part of Spoutcraft.
 *
 * Copyright (c) 2011-2012, Spout LLC <http://www.spout.org/>
 * Spoutcraft is licensed under the Spout License Version 1.
 *
 * Spoutcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spoutcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.terasologylauncher.util;

public enum OperatingSystem {
    UNIX("Unix"),
    LINUX("Linux"),
    SOLARIS("Solaris"),
    WINDOWS_XP("Windows XP"),
    WINDOWS_VISTA("Windows Vista"),
    WINDOWS_7("Windows 7"),
    WINDOWS_8("Windows 8"),
    WINDOWS_UNKNOWN("Windows"),
    MAC_OSX("Mac OS X"),
    MAC("Mac"),
    UNKNOWN("");

    private final String identifier;
    OperatingSystem(String system) {
        this.identifier = system.toLowerCase();
    }

    public boolean isUnix() {
        return this == UNIX || this == LINUX || this == SOLARIS;
    }

    public boolean isMac() {
        return this == MAC_OSX || this == MAC;
    }

    public boolean isWindows() {
        return this == WINDOWS_XP ||  this == WINDOWS_VISTA ||  this == WINDOWS_7 ||  this == WINDOWS_8 ||  this == WINDOWS_UNKNOWN;
    }

    public static OperatingSystem getOS() {
        OperatingSystem best = UNKNOWN;
        final String os = System.getProperty("os.name").toLowerCase();
        for (OperatingSystem system : values()) {
            if (os.contains(system.identifier)) {
                if (system.identifier.length() > best.identifier.length()) {
                    best = system;
                }
            }
        }
        return best;
    }
}
