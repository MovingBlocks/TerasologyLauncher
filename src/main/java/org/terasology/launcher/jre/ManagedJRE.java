// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.jre;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.launcher.platform.Arch;
import org.terasology.launcher.platform.OS;
import org.terasology.launcher.platform.Platform;

import java.util.HashSet;
import java.util.Set;

public final class ManagedJRE {

    public static final Set<Integer> SUPPORTED_JAVA_VERSIONS = Sets.newHashSet(8, 11, 17);

    private static final Set<JREArtefact> supportedJavaVersions;

    static {
        supportedJavaVersions = new HashSet<>();


        supportedJavaVersions.addAll(
                Lists.newArrayList(
                        // Linux
                        new JREArtefact(8,
                                new Platform(OS.LINUX, Arch.X64),
                                "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u392-b08/OpenJDK8U-jre_x64_linux_hotspot_8u392b08.tar.gz",
                                "91d31027da0d985be3549714389593d9e0da3da5057d87e3831c7c538b9a2a0f"
                        ),
                        new JREArtefact(11,
                                new Platform(OS.LINUX, Arch.X64),
                                "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.21%2B9/OpenJDK11U-jre_x64_linux_hotspot_11.0.21_9.tar.gz",
                                "156861bb901ef18759e05f6f008595220c7d1318a46758531b957b0c950ef2c3"
                        ),
                        new JREArtefact(17,
                                new Platform(OS.LINUX, Arch.X64),
                                "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jre_x64_linux_hotspot_17.0.9_9.tar.gz",
                                "c37f729200b572884b8f8e157852c739be728d61d9a1da0f920104876d324733"
                        ),
                        // Mac
                        new JREArtefact(8,
                                new Platform(OS.MAC, Arch.X64),
                                "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u392-b08/OpenJDK8U-jre_x64_mac_hotspot_8u392b08.tar.gz",
                                "f1f15920ed299e10c789aef6274d88d45eb21b72f9a7b0d246a352107e344e6a"
                        ),
                        new JREArtefact(11,
                                new Platform(OS.MAC, Arch.X64),
                                "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.21%2B9/OpenJDK11U-jre_x64_mac_hotspot_11.0.21_9.tar.gz",
                                "43d29affe994a09de31bf2fb6f8ab6d6792ba4267b9a2feacaa1f6e042481b9b"
                        ),
                        new JREArtefact(17,
                                new Platform(OS.MAC, Arch.X64),
                                "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jre_x64_mac_hotspot_17.0.9_9.tar.gz",
                                "c69b37ea72136df49ce54972408803584b49b2c91b0fbc876d7125e963c7db37"
                        ),
                        // Window
                        new JREArtefact(8,
                                new Platform(OS.WINDOWS, Arch.X64),
                                "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u392-b08/OpenJDK8U-jre_x64_mac_hotspot_8u392b08.tar.gz",
                                "a6b7e671cc12f9fc16db59419bda8be00da037e14aaf5d5afb78042c145b76ed"
                        ),
                        new JREArtefact(11,
                                new Platform(OS.WINDOWS, Arch.X64),
                                "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.21%2B9/OpenJDK11U-jre_x64_windows_hotspot_11.0.21_9.zip",
                                "a93d8334a85f6cbb228694346aad0353a8cb9ff3c84b5dc3221daf2c54a11e54"
                        ),
                        new JREArtefact(17,
                                new Platform(OS.WINDOWS, Arch.X64),
                                "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jre_x64_windows_hotspot_17.0.9_9.zip",
                                "6c491d6f8c28c6f451f08110a30348696a04b009f8c58592191046e0fab1477b"
                        )
                ));

        for (var v : supportedJavaVersions) {
            System.out.println(v.url);
        }
    }

    public static JREArtefact getJreFor(Platform platform, int version) throws UnsupportedJREException {
        return supportedJavaVersions.stream()
                .filter(jre -> jre.platform.equals(platform) && jre.version == version)
                .findFirst()
                .orElseThrow(() -> new UnsupportedJREException(platform, version));
    }
}
