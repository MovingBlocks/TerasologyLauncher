Terasology Launcher - ReadMe
============================
[*Terasology Launcher*][GitHub TerasologyLauncher] is the official launcher for the open source game [Terasology][GitHub Terasology].

The *Terasology Launcher* provides easy access to the different game versions and build types.

Terasology and related projects are developed by a group of software enthusiast volunteers under the organization name [MovingBlocks][GitHub MovingBlocks].

Usage
-----
*Terasology Launcher* requires Java 7.

1. Download and extract the ZIP file
   * [official releases][Download GitHub Releases] (recommended)
   * [latest stable ZIP][Download Jenkins STABLE]
   * [latest nightly ZIP][Download Jenkins NIGHTLY] (only for testing)
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

    gradlew install

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
* Many users of the [german forum][German forum] (ideas, testing, suggestions, bug reports)

[GitHub MovingBlocks]: https://github.com/MovingBlocks/ "MovingBlocks"
[GitHub Terasology]: https://github.com/MovingBlocks/Terasology/ "Terasology"
[GitHub TerasologyLauncher]: https://github.com/MovingBlocks/TerasologyLauncher/ "TerasologyLauncher"
[GitHub TerasologyLauncher Issues]: https://github.com/MovingBlocks/TerasologyLauncher/issues/ "TerasologyLauncher issues"
[GitHub TerasologyLauncher Contributors]: https://github.com/MovingBlocks/TerasologyLauncher/graphs/contributors/ "TerasologyLauncher contributors"
[Download GitHub Releases]: https://github.com/MovingBlocks/TerasologyLauncher/releases/ "TerasologyLauncher download (official releases)"
[Download Jenkins STABLE]: http://jenkins.movingblocks.net/job/TerasologyLauncherStable/lastStableBuild/artifact/build/distributions/TerasologyLauncher.zip "TerasologyLauncher STABLE download"
[Download Jenkins NIGHTLY]: http://jenkins.movingblocks.net/job/TerasologyLauncherNightly/lastStableBuild/artifact/build/distributions/TerasologyLauncher.zip "TerasologyLauncher NIGHTLY download"
[English forum]: http://forum.movingblocks.net/threads/terasologylauncher-mrbarsack.708/ "TerasologyLauncher forum thread"
[German forum]: http://terasologyforum.de/board49-entwicklung/board53-sonstiges/578-terasology-launcher-v3-mrbarsack/ "TerasologyLauncher forum thread"
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0"
[Gradle]: http://gradle.org "Gradle"
[Gradle Wrapper]: http://gradle.org/docs/current/userguide/gradle_wrapper.html "Gradle Wrapper"
