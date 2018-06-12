Terasology Launcher - ReadMe
============================
![Terasology Logo](https://github.com/MovingBlocks/TerasologyLauncher/wiki/images/logo.png)

[*Terasology Launcher*][GitHub TerasologyLauncher] is the official launcher for the open source game [Terasology][GitHub Terasology].

The *Terasology Launcher* provides easy access to the different game versions and build types.

Terasology and related projects are developed by a group of software enthusiast volunteers under the organization name [MovingBlocks][GitHub MovingBlocks].

[![Build Status](http://jenkins.terasology.org/view/Launcher/job/TerasologyLauncherStable/badge/icon)](http://jenkins.terasology.org/view/Launcher/job/TerasologyLauncherStable/)
[![Translation status](http://translate.terasology.org/widgets/launcher-shields-badge.svg)](http://translate.terasology.org/engage/launcher/?utm_source=widget)

Usage
-----
*Terasology Launcher* requires [Java 8](https://www.java.com) and JavaFX. If you are using OpenJDK make sure you have
the JavaFX runtime libraries installed, e.g., through an extra package often named `openjfx` (Ubuntu) or `java-openjfx` (ArchLinux).


1. Download and extract the ZIP file
   * [official releases][Download GitHub Releases] (recommended)
   * [latest release ZIP][Download Jenkins RELEASE]
   * [latest develop ZIP][Download Jenkins DEVELOP] (only for testing)
2. Start *Terasology Launcher*
   * TerasologyLauncher.exe (Windows)
   * bin/TerasologyLauncher.bat (Windows)
   * bin/TerasologyLauncher (Unix, Linux, Mac OS X)
   * lib/TerasologyLauncher.jar (Java)
3. Use *Terasology Launcher* to download, config and start *Terasology*

License
-------
*Terasology Launcher* is licensed under the [Apache License, Version 2.0][Apache License].

Building/Developing
-------------------
*Terasology Launcher* uses a [Gradle][Gradle]-based build system and provides a [wrapper][Gradle Wrapper].
It is a script which is called from the root of the source tree. It downloads and installs [Gradle][Gradle] automatically.
Depending on the system it may be necessary to call "./gradlew" instead of "gradlew".

    gradlew build

Compile the source code, run tests and build a jar

    gradlew installApp

Create a runnable installation (./build/install/TerasologyLauncher/)

    gradlew run

Build and run

    gradlew createRelease

Create a local development release (./build/distributions/)

    gradlew tasks

Display available tasks

    gradlew idea

Generate IntelliJ IDEA project files

Contributing
------------
Please use [GitHub][GitHub TerasologyLauncher Issues] or the forums ([english][English forum] or [german][German forum]) for contact, questions, feature suggestions, contributions and bug reports.

We welcome contributions, especially through pull requests on GitHub.
Submissions must be licensed under the [Apache License, Version 2.0][Apache License].

See also [CONTRIBUTING.md](CONTRIBUTING.md).

Contributors
------------
[Overview of contributors][GitHub TerasologyLauncher Contributors]

* MrBarsack [@MrBarsack](https://github.com/MrBarsack)
* Tobias Nett [@skaldarnar](https://github.com/skaldarnar)
* Mathias Kalb [@mkalb](https://github.com/mkalb)
* Rasmus Praestholm [@Cervator](https://github.com/Cervator)
* Ian Macalinao [@simplyianm](https://github.com/simplyianm)
* Immortius [@immortius](https://github.com/immortius)
* Aldo Borrero [@aldoborrero](https://github.com/aldoborrero)
* Martin Steiger [@msteiger](https://github.com/msteiger)
* Anthony Kireev [@small-jeeper](https://github.com/small-jeeper)
* Piotr Halama [@Halamix2](https://github.com/Halamix2)
* Martin van Kuik [mtjvankuik](https://github.com/mtjvankuik)
* Aaron Hill [@Aaron1011](https://github.com/Aaron1011)
* Avalancs [@Avalancs](https://github.com/Avalancs)
* Bryan Ngo [@bryngo](https://github.com/bryngo)
* Gianluca Nitti [@gianluca-nitti](https://github.com/gianluca-nitti)
* Malanius Privierre [@Malanius](https://github.com/Malanius)
* Max Borsch [@MaxBorsch](https://github.com/MaxBorsch)
* Rostyslav Zatserkovnyi [@rzats](https://github.com/rzats)
* Scott M Sunarto [@smsunarto](https://github.com/smsunarto)
* Zeeshan Asghar [@zeeshanasghar](https://github.com/zeeshanasghar)
* iojw [@iojw](https://github.com/iojw)
* theobisproject [@theobisproject](https://github.com/theobisproject)
* bpas247 [@bpas247](https://github.com/bpas247)

.. and many users of the German Terasology Forum (ideas, testing, suggestions, bug reports).

[GitHub MovingBlocks]: https://github.com/MovingBlocks/ "MovingBlocks"
[GitHub Terasology]: https://github.com/MovingBlocks/Terasology/ "Terasology"
[GitHub TerasologyLauncher]: https://github.com/MovingBlocks/TerasologyLauncher/ "TerasologyLauncher"
[GitHub TerasologyLauncher Issues]: https://github.com/MovingBlocks/TerasologyLauncher/issues/ "TerasologyLauncher issues"
[GitHub TerasologyLauncher Contributors]: https://github.com/MovingBlocks/TerasologyLauncher/graphs/contributors/ "TerasologyLauncher contributors"
[Download GitHub Releases]: https://github.com/MovingBlocks/TerasologyLauncher/releases/ "TerasologyLauncher download (official releases)"
[Download Jenkins RELEASE]: http://jenkins.terasology.org/job/TerasologyLauncherStable/lastStableBuild/artifact/build/distributions/TerasologyLauncher.zip "TerasologyLauncher RELEASE download"
[Download Jenkins DEVELOP]: http://jenkins.terasology.org/job/TerasologyLauncher/lastStableBuild/artifact/build/distributions/TerasologyLauncher.zip "TerasologyLauncher DEVELOP download"
[English forum]: http://forum.terasology.org/threads/terasologylauncher-mrbarsack.708/ "TerasologyLauncher forum thread"
[German forum]: http://terasologyforum.de/board49-entwicklung/board53-sonstiges/578-terasology-launcher-v3-mrbarsack/ "TerasologyLauncher forum thread"
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0"
[Gradle]: http://gradle.org "Gradle"
[Gradle Wrapper]: http://gradle.org/docs/current/userguide/gradle_wrapper.html "Gradle Wrapper"
