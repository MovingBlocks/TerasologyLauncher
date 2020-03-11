<p align="center"><img src="./docs/images/logo.png" height=400px/></>
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

[_Terasology Launcher_][github terasologylauncher] is the official launcher for the open source game [Terasology][github terasology]. It provides easy access to the different game versions and build types. Terasology and related projects are developed by a group of software enthusiast volunteers under the organization name [MovingBlocks][github movingblocks].

## Installation [![](https://img.shields.io/github/v/release/MovingBlocks/TerasologyLauncher)][latest-release]

1. Download the corresponding archive for your platform from the [latest release][latest-release]:
1. Extract the archive to the path where the launcher should be installed
1. Start _Terasology Launcher_
   - `TerasologyLauncher.exe` (Windows)
   - `bin/TerasologyLauncher` (Unix, Linux, Mac OS X)

## Community

If you want to get in contact with the **Terasology** community and the whole __MovingBlocks__ team, you can easily connect with us, share your ideas, report and solve problems.

<table style="margin-left:auto;margin-right:auto">
  <thead><tr>
    <th width="33%">Forum &amp; Chat</th>
    <th width="33%">Social Media</th>
    <th width="33%">Other</th>
  </tr></thead>
  <tr>
    <td width="33%" align="center">
      <a title="Terasology Forum" href="https://forum.terasology.org"><img src="./src/main/resources/org/terasology/launcher/images/forum.png" width="48px"/></a>
      <a title="Discord" href="https://discord.gg/terasology"><img src="./src/main/resources/org/terasology/launcher/images/discord.png" width="48px"/></a>
      <a title="IRC Webchat" href="http://webchat.freenode.net/?channels=terasology&uio=d4?channels=%23terasology&nick=Terasologist...&prompt=1&useUserListIcons=true"><img src="./src/main/resources/org/terasology/launcher/images/webchat.png" width="48px"/></a></td>
    <td width="33%" align="center">
      <a title="Twitter" href="https://twitter.com/Terasology"><img src="./src/main/resources/org/terasology/launcher/images/twitter.png" width="48px"/></a>
      <a title="Facebook" href="https://www.facebook.com/Terasology"><img src="./src/main/resources/org/terasology/launcher/images/facebook.png" width="48px"/></a>
      <a title="Reddit" href="http://www.reddit.com/r/Terasology"><img src="./src/main/resources/org/terasology/launcher/images/reddit.png" width="48px"/></a>
      <a title="Youtube" href="https://www.youtube.com/user/blockmaniaTV"><img src="./src/main/resources/org/terasology/launcher/images/youtube.png" width="48px"/></a></td>
    <td width="33%" align="center">
      <a title="Patreon" href="https://www.patreon.com/Terasology"><img src="./src/main/resources/org/terasology/launcher/images/patreon.jpg" width="48px"/></a></td>
  </tr>
</table>

We are present in nearly the complete round-up of social networks. Follow/friend us wherever you want, chat with us and tell the world.

## Features

## Development

Basically everything can be done using the [Gradle](http://gradle.org) [wrapper](http://gradle.org/docs/current/userguide/gradle_wrapper.html). The following list is an excerpt of some commonly used tasks.

| Command               | *Description* |
|:----------------------|:--------------|
|`gradlew build`        | *Compile the source code, run tests and build a JAR.* |
|`gradlew install`      | *Create a local runnable installation (placed in `./build/install/TerasologyLauncher`).* |
|`gradlew run`          | *Build and run the launcher.* |
|`gradlew createRelease`| *Create a local development release (located in `./build/distributions`).* |
|`gradlew tasks`        | *Display other available build script tasks.* |
|`gradlew idea`         | *Generate IntelliJ IDEA project files.*|

Please use [GitHub][github terasologylauncher issues] or the forums ([english][english forum] or [german][german forum]) for contact, questions, feature suggestions, contributions and bug reports.

We welcome contributions, especially through pull requests on GitHub.
Submissions must be licensed under the [Apache License, Version 2.0][apache license].

See also [CONTRIBUTING.md](CONTRIBUTING.md).

See [:octocat: contributor overview][github terasologylauncher contributors].

## Acknowledgements

This project uses
- Font Awesome Icon for Webchat ("fa comment")
- Bellsoft JRE

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
