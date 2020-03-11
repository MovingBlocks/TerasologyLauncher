<p align="center"><img src="./src/main/resources/org/terasology/launcher/images/logo.png"/></>
<div align="center">
    <img src="https://github.com/MovingBlocks/TerasologyLauncher/workflows/Push%20Validation/badge.svg" alt="Build Status"/>
</div>

[_Terasology Launcher_][github terasologylauncher] is the official launcher for the open source game [Terasology][github terasology]. It provides easy access to the different game versions and build types.

Terasology and related projects are developed by a group of software enthusiast volunteers under the organization name [MovingBlocks][github movingblocks].

## Installation

1. Download the package for your platform
   - [official releases][download github releases] (recommended)
1. Extract the archive to the path where the launcher should be installed
1. Start _Terasology Launcher_
   - `TerasologyLauncher.exe` (Windows)
   - `bin/TerasologyLauncher` (Unix, Linux, Mac OS X)

## Documentation

More information can be found in our [Documentation](docs/Home.md)

## Building/Developing

_Terasology Launcher_ uses a [Gradle][gradle]-based build system and provides a [wrapper][gradle wrapper].
It is a script which is called from the root of the source tree. It downloads and installs [Gradle][gradle] automatically.
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

## Contributing

Please use [GitHub][github terasologylauncher issues] or the forums ([english][english forum] or [german][german forum]) for contact, questions, feature suggestions, contributions and bug reports.

We welcome contributions, especially through pull requests on GitHub.
Submissions must be licensed under the [Apache License, Version 2.0][apache license].

See also [CONTRIBUTING.md](CONTRIBUTING.md).

## Contributors

See [:octocat: contributor overview][github terasologylauncher contributors].

## License

_Terasology Launcher_ is licensed under the [Apache License, Version 2.0][apache license].

[github movingblocks]: https://github.com/MovingBlocks/ "MovingBlocks"
[github terasology]: https://github.com/MovingBlocks/Terasology/ "Terasology"
[github terasologylauncher]: https://github.com/MovingBlocks/TerasologyLauncher/ "TerasologyLauncher"
[github terasologylauncher issues]: https://github.com/MovingBlocks/TerasologyLauncher/issues/ "TerasologyLauncher issues"
[github terasologylauncher contributors]: https://github.com/MovingBlocks/TerasologyLauncher/graphs/contributors/ "TerasologyLauncher contributors"
[download github releases]: https://github.com/MovingBlocks/TerasologyLauncher/releases/ "TerasologyLauncher download (official releases)"
[english forum]: http://forum.terasology.org/threads/terasologylauncher-mrbarsack.708/ "TerasologyLauncher forum thread"
[apache license]: http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0"
[gradle]: http://gradle.org "Gradle"
[gradle wrapper]: http://gradle.org/docs/current/userguide/gradle_wrapper.html "Gradle Wrapper"
