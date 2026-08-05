// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import edu.sc.seis.launch4j.tasks.Launch4jLibraryTask
import org.apache.tools.ant.filters.FixCrLfFilter
import org.gradle.jvm.tasks.Jar
import org.jetbrains.gradle.ext.delegateActions
import org.jetbrains.gradle.ext.settings
import org.terasology.gradlegoo.GradleGooExtension
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    configurations.classpath {
        resolutionStrategy.activateDependencyLocking()
    }
}

plugins {
    application
    checkstyle
    id("de.undercouch.download") version "5.3.0"
    id("edu.sc.seis.launch4j") version "4.0.0"
    id("net.ltgt.errorprone") version "5.1.0"
    java
    id("nebula.release") version "21.0.0"
    pmd
    `project-report`
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.5"
}

apply(plugin = "org.terasology.gradlegoo")

// A second, independent exe: launches the game directly (org.terasology.launcher.Terasology),
// skipping the GUI entirely - see DirectPlay's javadoc for why it avoids javafx.graphics. The
// launch4j{} extension below only wires up one exe (the plugin's fixed "createExe" task), so this
// is a second task of the same underlying type with its own config. Deliberately no bundledJrePath:
// unlike TerasologyLauncher.exe, this one has no JavaFX dependency, so it only needs a plain Java
// 21 runtime - callers are expected to provide their own (e.g. a winget PackageDependencies entry).
// Registered here, before jre.gradle.kts is applied, since that script references this task by name
// and (unlike the plugin's own "createExe") its registration doesn't exist until this line runs.
tasks.register<Launch4jLibraryTask>("createTerasologyExe") {
    outfile.set("Terasology.exe")
    mainClassName.set("org.terasology.launcher.Terasology")
    headerType.set("console")
    setJarTask(tasks.named("jar"))
    dontWrapJar.set(true)
    classpath.set(listOf("lib/*"))
    jreMinVersion.set("21")
    requires64Bit.set(true)
}

apply(from = "./config/gradle/jre.gradle.kts")

// Test for right version of Java in use for running this script - compiling for
// sourceCompatibility 21 below requires the JDK actually running Gradle to be >= 21 too.
// Pinned to 21 (not the newer 25) so the launcher's own JRE and the game's bundled JRE can be
// the same one - the game still needs Java 21 to install a SecurityManager for its module
// sandbox, which JEP 486 removed entirely starting with JDK 24.
assert(JavaVersion.current() >= JavaVersion.VERSION_21)

val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
dateTimeFormat.timeZone = TimeZone.getTimeZone("UTC")

configurations {
    compileClasspath {
        resolutionStrategy.activateDependencyLocking()
    }
    create("codeMetrics")
}

// Read environment variables, including variables passed by jenkins continuous integration server
val env: Map<String, String> = System.getenv()

// Stuff for our automatic version file setup
val startDateTimeString: String = dateTimeFormat.format(Date())

// Splash image for the JAR
val splashImage = "org/terasology/launcher/images/splash.jpg"

// Shared code analytics configurations via retrieved config zip
val metricsConfigDir = layout.projectDirectory.dir("config/metrics")

// Declare remote repositories we're interested in - library files will be fetched from here
repositories {
    // Main Maven repo
    mavenCentral()
    maven {
        // MovingBlocks Artifactory instance(s) for libs not readily available elsewhere plus our own libs
        name = "Terasology Artifactory"
        url = uri("https://artifactory.terasology.io/artifactory/virtual-repo-live")
    }
    maven {
        name = "JitPack" // used by org.everit.json.schema
        url = uri("https://jitpack.io")
    }

    maven {
        // required for markdown-javafx-renderer
        url = uri("https://sandec.jfrog.io/artifactory/repo")
    }
}

// Primary dependencies definition
dependencies {
    implementation("org.slf4j:slf4j-api:[1.7.+, 2.0.0-alpha7]") {
        because("influenced by app or test loggers as needed")
    }
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha16") {
        because("1.3 series uses ServiceLoader (more packaging friendly?)")
    }

    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.github.everit-org.json-schema:org.everit.json.schema:1.14.1")

    implementation("org.kohsuke:github-api:1.318")
    implementation("org.semver4j:semver4j:5.2.2")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.0")

    implementation("org.hildan.fxgson:fx-gson:5.0.0") {
        because("de-/serialization of launcher properties to JSON")
    }

    implementation("com.squareup.okhttp3:okhttp:4.12.0") {
        because("built-in caching of HTTP requests")
    }

    // These dependencies are only needed for running tests

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    // Gradle 9 no longer resolves this transitively - without it, `test` fails before running
    // anything: "Failed to load JUnit Platform... including the JUnit Platform launcher."
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.18.0") {
        because("mockito-inline (used previously) was discontinued after 5.2.0 - inline mock making " +
                "(mocking final classes) is the default in mockito-core since Mockito 5. Also, 5.2.0's " +
                "bundled Byte Buddy predates Java 25/26 class file support.")
    }
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")

    testImplementation("org.spf4j:spf4j-slf4j-test:8.10.0") {
        because("testable logging")
    }
    testImplementation("org.slf4j:slf4j-api:2.0.13")

    testImplementation("org.testfx:testfx-core:4.0.18") {
        because("to test JavaFX Application")
        // -alpha because that's the only kind of release they have?
    }
    testImplementation("org.testfx:testfx-junit5:4.0.18")

    testImplementation("org.testfx:openjfx-monocle:17.0.10") {
        // nobody's uploaded a jdk-14 build yet. does the jdk-12 one work?
        because("CI builders are headless environments")
    }

    testImplementation("com.github.gmazzo.okhttp.mock:mock-client:2.0.0") {
        because("to easily write OkHttpClient interceptors for testing")
    }
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0") {
        because("to control server responses for testing")
    }

    // Config for our code analytics from: https://github.com/MovingBlocks/TeraConfig
    "codeMetrics"("org.terasology.config:codemetrics:1.7.1@zip")

    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    compileOnly("com.google.errorprone:error_prone_annotations:2.50.0")
}

val testClasspathNamePattern = Regex("test(Runtime|Compile|Implementation|PmdAux)Classpath")
configurations.matching { testClasspathNamePattern.containsMatchIn(it.name) }.all {
    GradleGooExtension.prefers(resolutionStrategy, "logging", "jcl-api-capability", "jcl-over-slf4j",
            "jcl should prefer slf4j when available")
    GradleGooExtension.prefers(resolutionStrategy, "logging", "slf4j-impl-capability",
            "spf4j-slf4j-test", "tests use slf4j-test")

    // SPF4J has a dependency on "avro-logical-types-gen:1.3", which does not exist. However, version "1.3p" does.
    resolutionStrategy.force("org.spf4j:avro-logical-types-gen:1.3p")
    // old hamcrest versions exist only to spite us
    // http://hamcrest.org/JavaHamcrest/distributables#upgrading-from-hamcrest-1x
    exclude(group = "org.hamcrest", module = "hamcrest-core")
    exclude(group = "org.hamcrest", module = "hamcrest-library")
}

// Set the expected module Java level (can use a higher Java to run, but should not use features from a higher Java)
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
}
tasks.named<JavaCompile>("compileTestJava") {
    options.encoding = "UTF-8"
}

configure<org.openjfx.gradle.JavaFXOptions> {
    // Pinned to the 21.x line to match the JDK 21 we compile/bundle for - JavaFX's own artifacts
    // are compiled targeting roughly their own major version's bytecode (24.0.1 needs a JDK 22+
    // runtime to even load the classes), independent of our sourceCompatibility setting.
    version = "21.0.12"
    modules = listOf(
            "javafx.graphics",
            "javafx.fxml",
            "javafx.web"
    )
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    // The SPF4J test dependency relies on some forbidden reflection to improve logging performance.
    // Java 17 will not tolerate this without a bit of encouragement.
    jvmArgs("--add-opens=java.logging/java.util.logging=ALL-UNNAMED")
}

checkstyle {
    // 10.18.0 fixes LocalVariableNameCheck incorrectly flagging the JEP 456 unnamed
    // variable "_" (checkstyle#14688) - needed to accept error-prone's own suggested
    // fix for FutureReturnValueIgnored. Config is copied in under config/checkstyle/
    // (was pulled from the external, gitignored codemetrics zip) so we're not stuck
    // waiting on that shared config to catch up.
    toolVersion = "10.18.2"
    isIgnoreFailures = false
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
}

pmd {
    // 6.39.0's bundled ASM can't read Java 25 class files during type resolution
    // ("Unsupported class file major version 69"), erroring on every source file.
    toolVersion = "7.13.0"
    isIgnoreFailures = false
    isConsoleOutput = true
    threads = 4
    ruleSetFiles = files("${rootDir}/config/pmd.xml")
    ruleSets = listOf()
}

application {
    mainClass.set("org.terasology.launcher.TerasologyLauncher")
}
// Groovy/Kotlin scripts elsewhere in this file (jar manifest, launch4j) read this as a plain
// variable - the application plugin's own top-level property setter was removed in
// modern Gradle, but reading mainClass back out and aliasing it like this still works.
val mainClassName = application.mainClass.get()

// Vestigial: not referenced elsewhere, kept for parity with the pre-conversion Groovy script.
val convertGitBranch: (String?) -> String = { gitBranch ->
    if (gitBranch != null) {
        // Remove "origin/" from "origin/develop"
        gitBranch.substring(gitBranch.lastIndexOf("/") + 1)
    } else {
        ""
    }
}

val createVersionInfoFile = tasks.register("createVersionInfoFile") {
    inputs.property("version", version.toString())

    val versionInfoFileDir = File(sourceSets["main"].output.resourcesDir, "org/terasology/launcher/")
    val versionFile = File(versionInfoFileDir, "version.txt")

    outputs.file(versionFile)

    doLast {
        versionInfoFileDir.mkdirs()
        versionFile.writeText(version.toString())
    }
}

val extractCodeMetricsConfig = tasks.register<Copy>("extractCodeMetricsConfig") {
    description = "Extract code metrics configuration to '$metricsConfigDir'"
    from({ configurations["codeMetrics"].map { zipTree(it) } })
    into(metricsConfigDir)
}

tasks.named("processResources") {
    dependsOn(createVersionInfoFile, extractCodeMetricsConfig)
}

tasks.named<Delete>("clean") {
    delete(createVersionInfoFile.get().outputs.files)
    delete(extractCodeMetricsConfig.get().destinationDir)
}

tasks.named<Jar>("jar") {
    // TODO we only use this name because the `.exe` start scripts require the JAR to be named "TerasologyLauncher.jar"
    archiveFileName.set("${project.name}.jar")
    // replace development "logback.xml" with productive "logback_jar.xml"
    exclude("logback.xml")
    rename("logback_jar.xml", "logback.xml")
    manifest {
        val manifestClasspath = configurations["runtimeClasspath"].joinToString(" ") { it.name }
        attributes(
                "Main-Class" to mainClassName,
                "Class-Path" to manifestClasspath,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "SplashScreen-Image" to splashImage,

                // allow everything
                "Permissions" to "all-permissions",
                "Codebase" to "*",
                "Application-Name" to project.name,
                "Application-Library-Allowable-Codebase" to "*",
                "Caller-Allowable-Codebase" to "*",
                "Trusted-Only" to "false"
        )
    }
}

tasks.register<Copy>("copyExtra") {
    filter(mapOf("eol" to FixCrLfFilter.CrLf.newInstance("crlf")), FixCrLfFilter::class.java)

    from("README.md") {
        rename("README.md", "README.txt")
    }

    from("CHANGELOG.md") {
        rename("CHANGELOG.md", "CHANGELOG.txt")
    }

    from("CONTRIBUTING.md") {
        rename("CONTRIBUTING.md", "CONTRIBUTING.txt")
    }

    from("LICENSE")
    from("NOTICE")

    into(layout.buildDirectory.dir("distributions"))
}

val copyExtraIntoResources = tasks.register<Copy>("copyExtraIntoResources") {
    from("README.md")
    from("CHANGELOG.md")
    from("docs/CONTRIBUTING.md")
    from("LICENSE")

    into("src/main/resources/org/terasology/launcher/about")
}
tasks.named("processResources") {
    dependsOn(copyExtraIntoResources)
}

val copyIconsIntoResources = tasks.register<Copy>("copyIconsIntoResources") {
    from("icons")

    into("src/main/resources/org/terasology/launcher/icons")
}
tasks.named("processResources") {
    dependsOn(copyIconsIntoResources)
}

// Wraps TerasologyLauncher.jar into a real Windows .exe - see jre.gradle.kts, where windows64's
// distribution contents pulls in this task's output (replacing the previously checked-in,
// statically-generated buildres/windows64/TerasologyLauncher.x64.exe).
//
// dontWrapJar = true keeps the jar as an external lib/TerasologyLauncher.jar reference instead of
// embedding a copy inside the exe - embedding broke class loading on real Windows (see the fix
// commit for the exact symptom). classpath uses the bare "*" wildcard, not "*.jar" - Java's
// classpath wildcard syntax only expands a bare *. bundledJrePath doesn't copy anything at
// generation time; it's a search path baked into the exe, relative to itself at runtime, matching
// where unpackJreWindows64 (in jre.gradle.kts) already places the bundled JRE in the same
// distribution.
configure<edu.sc.seis.launch4j.Launch4jPluginExtension> {
    outfile.set("TerasologyLauncher.exe")
    mainClassName.set("org.terasology.launcher.TerasologyLauncher")
    headerType.set("gui")
    setJarTask(tasks.named("jar"))
    copyConfigurable.set(emptyList<Any>())
    dontWrapJar.set(true)
    classpath.set(listOf("lib/*"))
    bundledJrePath.set("jre")
    jreMinVersion.set("21")
    requires64Bit.set(true)
}

// distZip is the plain, OS-agnostic application-plugin zip (unix + windows start scripts, no
// platform split) and was never meant to carry a platform-specific exe - guard against one
// leaking in regardless of how it got named.
tasks.named<Zip>("distZip") {
    exclude("*.x*.exe")
}


idea {
    project.settings {
        delegateActions {
            delegateBuildRunToGradle = false
        }
    }
}

tasks.named<Wrapper>("wrapper") {
    // IDEA likes this distribution to better know things.
    distributionType = Wrapper.DistributionType.ALL
}
