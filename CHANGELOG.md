Terasology Launcher - ChangeLog
===============================

## x.y.z (unreleased)
* Translation into Russian

## 1.2.0 (2014-01-17)
* Choose, edit, show and use game data directory
* Translation into Spanish
* Fix for finding game version information
* Show outdated nightly multiplayer job only if it is installed
* Show old nightly versions only if they are installed
* Internal changes
    * Gradle update (1.10)
    * Java source code check tool 'FindBugs'
    * License header supports year 2014

## 1.1.2 (2013-11-22)
* Fix for a wrong game version info (git branch)

## 1.1.1 (2013-11-08)
* Fix wrong horizontal scroll bar
* Add progress indicators
    * Downloading and updating the launcher
    * Loading game versions (with change log)
* Change min java version for 'TerasologyLauncher.exe' (1.7.0)
* Avoid unnecessary downloading game version info
* Some minor internal code improvements (checkstyle, warnings, logging)

## 1.1.0 (2013-10-27)
* Show launcher *change log*
* Cancel game download
* Add new game type "legacy"
    * STABLE : TerasologyLegacy (legacy, pre-multiplayer)
* Supports new structure ('The Great Convergence')
* Internal changes
    * Gradle update (1.8)
    * Java source code check tool 'PMD'
    * Better support for 'IntelliJ-IDEA'

## 1.0.0 (2013-07-22)

* Download, install and start the game *Terasology*
    * Supports setting a custom game installation directory and managing multiple installations / versions
    * Supports three game types
        * STABLE : TerasologyStable (master)
        * NIGHTLY : Terasology (develop)
        * NIGHTLY : TerasologyMulti (multiplayer)
    * Supports memory settings (Java heap size)
    * Displays comments for each version (git comment)
* Download, install and start the launcher (self updatable)
* Supports multiple languages (english, german)
* Displays many "Terasology"-related links
