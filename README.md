<p align="center"><img src="./docs/images/logo.svg"/></>
<div align="center">
    <img src="https://github.com/MovingBlocks/TerasologyLauncher/workflows/Push%20Validation/badge.svg" alt="Build Status"/>
    <a href="https://github.com/MovingBlocks/TerasologyLauncher/releases/latest">
        <img src="https://img.shields.io/github/v/release/MovingBlocks/TerasologyLauncher" alt="Release" />
    </a>
    <a href="http://www.apache.org/licenses/LICENSE-2.0.html">
        <img src="https://img.shields.io/github/license/MovingBlocks/TerasologyLauncher" alt="License" />
    </a>
</div>

<h3 align="center"><font size="+1"><b>
    <a href="#installation">Installation</a> | 
    <a href="#community">Community</a> | 
    <a href="#features">Features</a>  | 
    <a href="#development">Development</a>  | 
    <a href="#license">License</a> 
</b></font></h3>

[_Terasology Launcher_][github terasologylauncher] is the official launcher for the open source game [Terasology][github terasology]. It provides easy access to the different game versions and build types.

Terasology and related projects are developed by a group of software enthusiast volunteers under the organization name [MovingBlocks][github movingblocks].

## Installation [![](https://img.shields.io/github/v/release/MovingBlocks/TerasologyLauncher)][latest-release]

1. Download the corresponding archive for your platform from the [latest release][latest-release]:
1. Extract the archive to the path where the launcher should be installed
1. Start _Terasology Launcher_
   - `TerasologyLauncher.exe` (Windows)
   - `bin/TerasologyLauncher` (Unix, Linux, Mac OS X)

## Community

## Features

## Development

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

Please use [GitHub][github terasologylauncher issues] or the forums ([english][english forum] or [german][german forum]) for contact, questions, feature suggestions, contributions and bug reports.

We welcome contributions, especially through pull requests on GitHub.
Submissions must be licensed under the [Apache License, Version 2.0][apache license].

See also [CONTRIBUTING.md](CONTRIBUTING.md).

See [:octocat: contributor overview][github terasologylauncher contributors].

## Acknowledgements

This project uses

## License [![](https://img.shields.io/github/license/MovingBlocks/TerasologyLauncher)][license]

_Terasology Launcher_ is licensed under the [Apache License, Version 2.0][apache license].

<!-- References -->

[latest-release]: https://github.com/MovingBlocks/TerasologyLauncher/releases/ "TerasologyLauncher download (official releases)"
[license]: http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0"

<!-- -->

[github movingblocks]: https://github.com/MovingBlocks/ "MovingBlocks"
[github terasology]: https://github.com/MovingBlocks/Terasology/ "Terasology"
[github terasologylauncher]: https://github.com/MovingBlocks/TerasologyLauncher/ "TerasologyLauncher"
[github terasologylauncher issues]: https://github.com/MovingBlocks/TerasologyLauncher/issues/ "TerasologyLauncher issues"
[github terasologylauncher contributors]: https://github.com/MovingBlocks/TerasologyLauncher/graphs/contributors/ "TerasologyLauncher contributors"
[english forum]: http://forum.terasology.org/threads/terasologylauncher-mrbarsack.708/ "TerasologyLauncher forum thread"
[gradle]: http://gradle.org "Gradle"
[gradle wrapper]: http://gradle.org/docs/current/userguide/gradle_wrapper.html "Gradle Wrapper"
