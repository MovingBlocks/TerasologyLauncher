// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// Scripts applied via apply(from = ...) don't inherit the root script's plugins{}-resolved
// classpath for their own compilation (a known Kotlin DSL limitation) - this script needs its
// own buildscript{} to resolve the Download task type and launch4j's task types by name.
buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("de.undercouch:gradle-download-task:5.3.0")
    }
}

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withGroovyBuilder

// Applied scripts don't get the main script's plugins{}-generated type-safe accessors (like the
// top-level "distributions {}" function build.gradle.kts gets for free) - look the container up
// explicitly instead.
val distributions = the<DistributionContainer>()

// Uses Bellsoft Liberica JRE - the "full" edition specifically, since it's the only free,
// redistributable build that bundles JavaFX in (JavaFX was split out of the JDK since Java 11).
// https://bell-sw.com/pages/downloads/
// Pinned to 17, matching what Terasology (the engine) itself targets, so the launcher can be
// developed with the same JDK - see review discussion on PR #719. This also means the game's
// module sandbox SecurityManager (see terasology#5357) works with no opt-in flag needed; that's
// only disabled-by-default starting with JDK 18, and removed entirely (JEP 486) on 24+.
val jdkVersion = "17.0.20+10"

// Each target is a fixed (OS, arch) pair - the resulting zip/tar always bundles the same
// JRE regardless of which machine runs Gradle, so a single host can build every dist.
// distBase groups Linux64/LinuxArm64 (etc.) under the same buildres/ resources and the
// same eachFile handling, since the family - not the arch - determines those.
data class JrePlatform(val distBase: String, val urlFile: String)

val jrePlatforms = mapOf(
        "Linux64" to JrePlatform("linux", "linux-amd64-full.tar.gz"),
        "LinuxArm64" to JrePlatform("linux", "linux-aarch64-full.tar.gz"),
        "Windows64" to JrePlatform("windows", "windows-amd64-full.zip"),
        "WindowsArm64" to JrePlatform("windows", "windows-aarch64-full.zip"),
        // Bundling the amd64 JRE unconditionally used to make the game fail to launch on
        // Apple Silicon: running under Rosetta, the launcher's JVM intermittently can't
        // posix_spawn the game's child JVM (posix_spawn failed, error: 0).
        "Mac" to JrePlatform("mac", "macos-amd64-full.zip"),
        "MacArm64" to JrePlatform("mac", "macos-aarch64-full.zip")
)

val createRelease = tasks.register("createRelease") {
    group = "Distribution"
    description = "Bundles the project with a JRE for each platform"
    dependsOn(tasks.named("distZip"))

    doLast {
        println("Created release: $version")
    }
}

val downloadJreAll = tasks.register("downloadJreAll") {
    group = "JRE"
    description = "Downloads Launcher JREs for all platforms"
}

val unpackJreAll = tasks.register("unpackJreAll") {
    group = "JRE"
    description = "Unpack JREs for all platforms"
}

fun createJreTasks(
        taskNameBase: String,
        downloadUrl: String,
        downloadFile: String,
        unpackDir: String
): TaskProvider<Copy> {

    tasks.register<Download>("download$taskNameBase") {
        group = "JRE"
        src(downloadUrl)
        dest(downloadFile)
        overwrite(false)
    }

    val unpackTask = tasks.register<Copy>("unpack$taskNameBase") {
        group = "JRE"
        from(if (downloadFile.endsWith("zip")) zipTree(downloadFile) else tarTree(downloadFile)) {
            eachFile {
                relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                // filePermissions{} below overrides every copied entry's mode with a
                // fresh default, discarding the executable bit the zip/tar recorded for
                // bin/* (java, javaw, ...) and jspawnhelper - restore it explicitly or
                // the bundled JRE can't be executed after unpacking. jspawnhelper lives
                // under lib/, not bin/: without exec permission on it, ProcessBuilder
                // (used to launch the game) fails with "posix_spawn failed, error: 0"
                // since JDK uses this helper binary for process spawning on macOS/Linux.
                if ((relativePath.segments.isNotEmpty() && relativePath.segments[0] == "bin")
                        || relativePath.lastName == "jspawnhelper") {
                    permissions { unix("755") }
                }
            }
            includeEmptyDirs = false
        }
        into(unpackDir)
        filePermissions {
            user.write = true
        }
        dependsOn("download$taskNameBase")
    }

    return unpackTask
}

jrePlatforms.forEach { (os, platform) ->
    val launcherTaskBase = "Jre$os"
    val unpackTask = createJreTasks(
            launcherTaskBase,
            "https://download.bell-sw.com/java/$jdkVersion/bellsoft-jre$jdkVersion-${platform.urlFile}",
            "$projectDir/jre/$os-$jdkVersion-${platform.urlFile}",
            "$projectDir/jre/$os")

    val distName = os.lowercase()
    val distBase = platform.distBase

    distributions.create(distName) {
        contents {
            with(distributions["main"].contents)

            into("jre") {
                from(unpackTask)
                // The dist Zip/Tar tasks don't carry over the executable bit
                // unpackTask's own eachFile restored on disk - re-apply it here so
                // jre/bin/java is actually executable inside the packaged archive.
                eachFile {
                    val segs = relativePath.segments
                    // relativePath here is rooted at the whole distribution, not the
                    // "jre" subtree, so match on the file's immediate parent dir
                    // rather than assuming "bin" is segments[0]. See the matching
                    // comment on unpackTask above for why jspawnhelper needs this too.
                    if ((segs.size >= 2 && segs[segs.size - 2] == "bin") || relativePath.lastName == "jspawnhelper") {
                        permissions { unix("755") }
                    }
                }
            }

            from("$projectDir/buildres/$distBase")
            from("$projectDir/buildres/$distName")

            if (os == "Windows64") {
                // Dynamically generated by launch4j (see the launch4j {} block in
                // build.gradle.kts) instead of the checked-in static binary this replaces.
                from(tasks.named("createExe"))
                // Second, independent exe that launches the game directly instead of the GUI
                // (see createTerasologyExe in build.gradle.kts) - rides along in the same zip
                // since it shares the same jar/lib set, even though it doesn't use the bundled jre/.
                from(tasks.named("createTerasologyExe"))
            }
        }
    }

    downloadJreAll.configure { dependsOn("download$launcherTaskBase") }
    unpackJreAll.configure { dependsOn("unpack$launcherTaskBase") }

    // Gradle's distribution plugin names the assemble task after distName (all-lowercase, e.g.
    // "macarm64") with only its first letter capitalized - not after the mixed-case "os" map key.
    createRelease.configure { dependsOn("assemble${distName.replaceFirstChar { it.uppercase() }}Dist") }
}

jrePlatforms.filterValues { it.distBase == "mac" }.keys.forEach { os ->
    distributions.named(os.lowercase()) {
        contents {
            into("TerasologyLauncher.app/Contents")
            exclude("**/*.bat")
            eachFile {
                path = Regex("(Contents)/bin/(.+)").replace(path) { m ->
                    "${m.groupValues[1]}/MacOS/${m.groupValues[2]}"
                }
            }
        }
    }
}

tasks.withType<CreateStartScripts>().configureEach {
    val unixTemplate = file("$projectDir/buildres/scripts/unixStartScript.txt")
    val windowsTemplate = file("$projectDir/buildres/scripts/windowsStartScript.txt")

    assert(project.file(unixTemplate).exists())
    assert(project.file(windowsTemplate).exists())

    unixStartScriptGenerator.withGroovyBuilder {
        setProperty("template", resources.text.fromFile(unixTemplate))
    }
    windowsStartScriptGenerator.withGroovyBuilder {
        setProperty("template", resources.text.fromFile(windowsTemplate))
    }
}
