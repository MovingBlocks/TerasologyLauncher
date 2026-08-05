// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

rootProject.name = "TerasologyLauncher"

if (File("web-api-client").exists()) {
    include("web-api-client")
}
