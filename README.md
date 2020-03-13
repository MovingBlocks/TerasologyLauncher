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

<h3 align="center"><b>
    <a href="#installation-">Installation</a> | 
    <a href="#community">Community</a> | 
    <a href="#features">Features</a>  | 
    <a href="#development">Development</a>  | 
    <a href="#acknowledgements">Acknowledgements</a>  | 
    <a href="#license-">License</a> 
</b></h3>

[_Terasology Launcher_][github terasologylauncher] is the official launcher for the open source game [Terasology][github terasology]. It provides easy access to the different game versions and build types. Terasology and related projects are developed by a group of software enthusiast volunteers under the organization name [MovingBlocks][github movingblocks].

## Installation [![](https://img.shields.io/github/v/release/MovingBlocks/TerasologyLauncher)][latest-release]

1. Download the corresponding archive for your platform from the [latest release][latest-release]
1. Extract the archive to the path where the launcher should be installed
1. Start _Terasology Launcher_

<table align="center">
  <thead align="left"><tr>
    <th width="40%">Operating System</th>
    <th width="60%">Executable</th>
  </tr></thead>
  <tr>
    <td width="40%"><b>Windows</b></td>
    <td width="60%"><code>TerasologyLauncher.exe</code> or <code>bin/TerasologyLauncher.bat</code></td>
  </tr>
  <tr>
    <td width="40%"><b>Unix, Linux, Mac OS X</b></td>
    <td width="60%"><code>bin/TerasologyLauncher</code></td>
  </tr>
</table>


## Community

If you want to get in contact with the **Terasology** community and the whole **MovingBlocks** team, you can easily connect with us, share your ideas, report and solve problems.
We are present in nearly the complete round-up of social networks. Follow/friend us wherever you want, chat with us and tell the world.

&nbsp;

<p align="center">
    <a title="Terasology Forum" href="https://forum.terasology.org">
        <img src="./src/main/resources/org/terasology/launcher/images/forum.png" width="48px"/>
    </a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a title="Discord" href="https://discord.gg/terasology">
        <img src="./src/main/resources/org/terasology/launcher/images/discord.png" width="48px"/>
    </a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a title="IRC Webchat" href="http://webchat.freenode.net/?channels=terasology&uio=d4?channels=%23terasology&nick=Terasologist...&prompt=1&useUserListIcons=true">
        <img src="./src/main/resources/org/terasology/launcher/images/webchat.png" width="48px"/>
    </a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a title="Twitter" href="https://twitter.com/Terasology">
    <img src="./src/main/resources/org/terasology/launcher/images/twitter.png" width="48px"/>
    </a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a title="Facebook" href="https://www.facebook.com/Terasology">
        <img src="./src/main/resources/org/terasology/launcher/images/facebook.png" width="48px"/>
    </a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a title="Reddit" href="http://www.reddit.com/r/Terasology">
        <img src="./src/main/resources/org/terasology/launcher/images/reddit.png" width="48px"/>
    </a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a title="Youtube" href="https://www.youtube.com/user/blockmaniaTV">
        <img src="./src/main/resources/org/terasology/launcher/images/youtube.png" width="48px"/>
    </a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a title="Patreon" href="https://www.patreon.com/Terasology">
        <img src="./src/main/resources/org/terasology/launcher/images/patreon.jpg" width="48px"/>
    </a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a title="GitHub Issues" href="https://github.com/MovingBlocks/TerasologyLauncher/issues">
        <img src="./src/main/resources/org/terasology/launcher/images/github.png" width="48px"/>
    </a>
</p>

## Features

## Development

To be able to run **TerasologyLauncher** from source follow these steps. This guide is designed for [IntelliJ IDEA](http://www.jetbrains.com/idea/) (you can use the free community edition), but alternative setups are possible.

<details closed>
<summary>:octocat: Get Ready</summary>
<br>

To contribute to this repo, please open a [pull request](https://guides.github.com/activities/forking/) from your fork of this repository.

For more information on building and developing the Terasology Launcher, make sure to read the rest of this README and [CONTRIBUTING.md](docs/CONTRIBUTING.md).

### Requirements

You'll need the following tools:

- Java SE Development Kit (JDK) 8 with JavaFX. It may be possible to use a later version of the JDK for local development, but the CI will verify against this baseline version.
- Git to clone the repo and commit changes.

Development is possible on all common platforms (Windows, Linux, MacOS) as long as the JDK with JavaFX is properly set up.
  
To generate the project files for IntelliJ, open a command prompt in the directory you checked out into and execute `gradlew idea` - this fetches the right version of Gradle and all project dependencies automatically, as well as generates the project config. Afterwards, you can simply open the project file in IntelliJ.

There is also a version for Eclipse - `gradlew eclipse` - but we encourage you to use IntelliJ.

</details>

<details closed>
<summary>:nerd_face: Start developing</summary>
<br>
    
Familiarise yourself with Git's concept of repositories, branches, and commits. To get the latest changes from your remote repositories (e.g. `movingblocks`) you need to *fetch* all remote data via `git fetch --all`. This does not change your workspace, it just loads up your local Git database.

Apart from Git, basically everything can be done using the [Gradle](http://gradle.org) [wrapper](http://gradle.org/docs/current/userguide/gradle_wrapper.html). The following list is an excerpt of some commonly used tasks.

<table align="center">
  <thead align="left"><tr>
    <th width="50%">Command</th>
    <th width="50%"><i>Description</i></th>
  </tr></thead>
  <tr>
      <td width="50%"><code>gradlew build</code></td>
      <td width="50%"><i>Compile the source code, run tests and build a JAR.</i></td>
  </tr>
  <tr>
      <td width="50%"><code>gradlew install</code></td>
      <td width="50%"><i>Create a local runnable installation (placed in <code>./build/install/TerasologyLauncher</code>).</i></td>
  </tr>
  <tr>
      <td width="50%"><code>gradlew run</code></td>
      <td width="50%"><i>Build and run the launcher.</i></td>
  </tr>
  <tr>
      <td width="50%"><code>gradlew createRelease</code></td>
      <td width="50%"><i>Create a local development release (located in <code>./build/distributions</code>).</i></td>
  </tr>
  <tr>
      <td width="50%"><code>gradlew tasks</code></td>
      <td width="50%"><i>Display other available build script tasks.</i></td>
  </tr>
  <tr>
      <td width="50%"><code>gradlew idea`</td>
      <td width="50%"><i>Generate IntelliJ IDEA project files.</i></td>
  </tr>
</table>

Assume you have pushed some changes to your fork into a branch `myFeature`. In order to let us know about your work and give us the possibility to incorporate your changes you should send us a _pull request_. You can do this by selecting the `myFeature` branch on you GitHub repo and click the button which says "Open pull request".

More information on how to contribute can be found in [CONTRIBUTING.md](https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/CONTRIBUTING.md). Remember, that all submissions must be licensed under [Apache License, Version 2.0][license].

</details>

<details closed>
<summary>:books: Related Resources</summary>
<br>

Tutorials and further information on Git:

- http://www.vogella.de/articles/Git/article.html
- http://gitref.org/
- http://progit.org/

Developer setup tutorials for our main project, [**Terasology**](https://github.com/MovingBlocks/Terasology):

- [Dev Setup](https://github.com/MovingBlocks/Terasology/wiki/Dev-Setup)
- [Dealing with Forks](https://github.com/MovingBlocks/Terasology/wiki/Dealing-with-Forks)

</details>

## Acknowledgements

_Terasology Launcher_ is driven by its [contributors][github terasologylauncher contributors]!

This project uses

- Font Awesome Icon for Webchat (`fa-comments`), CC BY 4.0 License, [Font Awesome Free License](https://fontawesome.com/license/free)
- [Bellsoft Liberica JRE 8](https://bell-sw.com/pages/java-8u232/) is bundled with the launcher

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
