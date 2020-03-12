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

   | Operating System          | Executable                                               |
   | ------------------------- | -------------------------------------------------------- |
   | **Windows**               | `TerasologyLauncher.exe` or `bin/TerasologyLauncher.bat` |
   | **Unix, Linux, Mac OS X** | `bin/TerasologyLauncher`                                 |

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
Furthermore, you will need [Git](https://git-scm.com) for source control and sign up for [GitHub](https://github.com/signup/free).

<details closed>
<summary>:one: Fork the project</summary>
<br>
    
To get ready to tinker with the source code, *fork* the project by opening the [**TerasologyLauncher**](https://github.com/MovingBlocks/TerasologyLauncher) GitHub page and clicking on the little fork button in the upper right. 

</details>

<details closed>
<summary>:two: Clone the repository</summary>
<br>
    
There are several ways to get the source code on your local workspace:
- Use the command line by executing `git clone https://github.com/MovingBlocks/TerasologyLauncher.git` (replace `MovingBlocks` with your GitHub username to use your own fork)
- Use IntelliJ's feature "Check out from version control" and follow the prompts
- Use [GitHub for Windows](https://windows.github.com/) or [GitHub for Mac](https://mac.github.com/) to clone the repo

If you cloned your own fork, you might want to set the original repository as a remote. To do so, run

~~~
git remote add movingblocks https://github.com/MovingBlocks/TerasologyLauncher.git
~~~

</details>

<details closed>
<summary>:three: Generate project files and open in IDE</summary>
<br>
    
To generate the project files for IntelliJ, open a command prompt in the directory you checked out into and execute `gradlew idea` - this fetches the right version of Gradle and all project dependencies automatically, as well as generates the project config. Afterwards, you can simply open the project file in IntelliJ. 

There is also a version for Eclipse - `gradlew eclipse` - but we encourage you to use IntelliJ.

</details>

<details closed>
<summary>:tada: Start developing</summary>
<br>
    
Familiarise yourself with Git's concept of repositories, branches, and commits. To get the latest changes from your remote repositories (e.g. `movingblocks`) you need to *fetch* all remote data via `git fetch --all`. This does not change your workspace, it just loads up your local Git database.

Apart from Git, basically everything can be done using the [Gradle](http://gradle.org) [wrapper](http://gradle.org/docs/current/userguide/gradle_wrapper.html). The following list is an excerpt of some commonly used tasks.

| Command                 | _Description_                                                                            |
| :---------------------- | :--------------------------------------------------------------------------------------- |
| `gradlew build`         | _Compile the source code, run tests and build a JAR._                                    |
| `gradlew install`       | _Create a local runnable installation (placed in `./build/install/TerasologyLauncher`)._ |
| `gradlew run`           | _Build and run the launcher._                                                            |
| `gradlew createRelease` | _Create a local development release (located in `./build/distributions`)._               |
| `gradlew tasks`         | _Display other available build script tasks._                                            |
| `gradlew idea`          | _Generate IntelliJ IDEA project files._                                                  |


Assume you have pushed some changes to your fork into a branch `myFeature`. In order to let us know about your work and give us the possibility to incorporate your changes you should send us a *pull request*. You can do this by selecting the `myFeature` branch on you GitHub repo and click the button which says "Open pull request".

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

_Terasology Launcher_ is driven by its [:octocat: contributors][github terasologylauncher contributors]!

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
