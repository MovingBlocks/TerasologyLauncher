// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.jre;

import org.terasology.launcher.platform.Platform;

//TODO(Java17): make this a 'record'
public final class JREArtefact {

    public final String url;
    public final String checksum;
    public final Platform platform;
    public final int version;

    public JREArtefact(int version, Platform platform,String url, String checksum) {
        this.url = url;
        this.checksum = checksum;
        this.platform = platform;
        this.version = version;
    }
}
