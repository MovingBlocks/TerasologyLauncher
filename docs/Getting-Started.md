[[HOME|Home]] > [[GETTING STARTED|Getting-started]]

<img align="left" width="96px" src="images/download.png"/> 
Getting started with __TerasologyLauncher__ is really easy - the only pre-requisite is that you have installed *Java 7* 
(Update 6 or later) on your system. This page will give a brief guide on how to download, install, and use the launcher.

# Download &amp; Install

## Step 1
Download the *ZIP file* - we recommend using the latest GitHub release.
<p align="center">
<a href="https://github.com/MovingBlocks/TerasologyLauncher/releases/latest">Latest Official Release</a>
</p>

A list of all releases can be found [here](https://github.com/MovingBlocks/TerasologyLauncher/releases/). 
Nightly builds are available through our Jenkins server. 

__Note:__ *The nightly builds are for testing purposes only. We cannot guarantee their stability, as they are often
work-in-progress versions for the next release. Use them at your own risk!*

<p align="center">
<a href="jenkins.terasology.org/job/TerasologyLauncherNightly/lastStableBuild/artifact/build/distributions/TerasologyLauncher.zip">Latest Nightly Build</a>
</p>

## Step 2
Extract the downloaded *ZIP file* to any directory. You can select the installation directory of the game separately.

## Step 3
Start __TerasologyLauncher__ using one of the following methods:

| Operating System          | Executable |
|-------------------------| ---------- |
| __Windows__               | `TerasologyLauncher.exe` or `bin/TerasologyLauncher.bat` |
| __Unix, Linux, Mac OS X__ | `bin/TerasologyLauncher` |
| __all__                   | `java -jar lib/TerasologyLauncher.jar` |

For Linux, you can use the following `.desktop` file to start the launcher (provided by [@BenjaminAmos](https://github.com/BenjaminAmos)). Just save the content as `TerasologyLauncher.desktop` in the launcher directory. Some distributions, e.g., Ubuntu, now let you start the launcher by double-clicking the file.

```
[Desktop Entry]
Version=3.10
Type=Application
Terminal=true
Exec=sh -c 'bash "$(dirname "%k")"/bin/TerasologyLauncher'
Path=
Icon=
Name=Terasology Launcher
Comment=This is the Terasology Launcher
StartupNotify=true
GenericName[en_GB]=Game Launcher
Categories=Game;Java
```
