Terasology Launcher - ChangeLog
===============================

## 2.0.0 (2015-04-26
* Launcher now retrieves the "Omega" zip which is base Terasology + the modules in the standard lineup
* Structure of Terasology.zip changed, Terasology.jar is now inside the "libs" directory

## 1.4.2 (2014-10-11)
* Fix for OS X when selecting game and launcher directory at first start
* Small GUI change (removed "Mods" link)

## 1.4.1 (skipped)
* Rebuild of v1.4.0 with new URLs after server crash 

## 1.4.0 (2014-05-29)
* Translation into Polish
* Fallback for missing language translations
* New setting "Save downloaded files" and new "download" directory
* Update "TerasologyLauncher.exe"
* Better handling for failed or change-less game builds
* Delete old and unused cache files
* Note: Old cached information about game versions are not compatible. These are automatically reloaded.
* Internal changes
    * Restructure and refactor code
    * Change Checkstyle and FindBugs configurations
    * Load and log game engine version
    * More and better logging
    * Some small bug fixes
    * Update compile and runtime dependencies
    * Update gradle wrapper (1.10 -> 1.12)
    * Update launch4j (3.0.2 -> 3.4)

## 1.3.1 (2014-03-25)
* Allow all memory (heap size) values (512 MB - 16 GB)
* Optimize zip file download (checks, logging, timeout, file name, ...)
* Delete content of game installation sub directory before downloading and extracting a game version

## 1.3.0 (2014-02-23)
* Translation into Russian
* Optimize language selection
* Optimize launcher self update
* Note: Old cached information about game versions are not compatible. These are automatically reloaded.
* Internal changes
    * Update libraries (logback, slf4j)
    * Restructure and refactor code
    * Some small bug fixes

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
