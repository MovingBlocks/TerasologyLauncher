// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("java-gradle-plugin")
    groovy
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(localGroovy())
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "org.terasology.gradlegoo"
            implementationClass = "org.terasology.gradlegoo.GradleGooPlugin"
        }
    }
}
