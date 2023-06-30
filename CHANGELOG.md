# Terasology Launcher - ChangeLog

## 4.8.0 (2023-06-30)

Special thanks to everyone that contributed to this release:
@anshukrs, @jdrueckert, @skaldarnar.

# Changelog

## 🐛 Bug Fixes

- #699 fix: show locally installed games in drop-down when offline (@skaldarnar)
- #691 fix: updated the download URL (@anshukrs)

## 🧰 Maintenance

- #698 chore: handle offline mode more graceful (@skaldarnar)
- #697 chore: Remove unused code (@skaldarnar)

## 🧪 Tests

- #688 test: add test to ensure that all supported languages have language flag icons (@skaldarnar)

## 📚 Documentation

- #689 doc: add troubleshooting section and download timeout workaround (@jdrueckert)

## ⚙️ Logistics

- #687 build(pmd): update PMD rules and fix issues (@skaldarnar)

## 4.7.0 (2022-11-09)

### 🚀 Features

- #684 feat: remove game profile selection (fixed on OMEGA) (@skaldarnar)
- #682 feat(i18n): add missing german translations (@jdrueckert)
- #679 feat(i18n): use properties and bindings for i18n of resources bundles (@skaldarnar)

### 🐛 Bug Fixes

- #681 fix(i18n): reduce log spam for missing labels (@skaldarnar)

### 🧰 Maintenance

- #685 chore: update Gradle (7.1.1 >>> 7.5.1) and dependencies (@skaldarnar)

### 📚 Documentation

- #683 doc: udpate documentation (@skaldarnar)
- #680 doc: add release guide (@skaldarnar)

## 4.6.0 (2022-09-03)

### 🚀 Features

- #670 feat(i18n): Update Ukrainian language strings & fill in missing i18n (@rzats)
- #668 feat: add request caching with OkHttp (@skaldarnar)
- #667 feat: 1:1 migration from LauncherSettings >>> Settings (@skaldarnar)
- #665 feat: prepare for JavaFX-property-based launcher settings (@skaldarnar)
- #658 feat: hide pre-releases by default (@skaldarnar)

### 🐛 Bug Fixes

- #659 fix: pass JVM heap settings in correct order to GameStarter (@skaldarnar)

### 🧰 Maintenance

- #666 refactor: change API of Settings#load/store to use folder path (@skaldarnar)
- #664 chore: pass LauncherConfiguration to ApplicationController#update (@skaldarnar)
- #662 chore: merge BaseLauncherSettings into LauncherSettings (@skaldarnar)

### 📚 Documentation

- #676 doc: update documentation (@skaldarnar)

### ⚙️ Logistics

- #674 build(github): update actions (@skaldarnar)
- #673 build!: remove 32-bit Windows build (@keturn)
- #672 build: runtime upgrade to Java 11.0.16.1 (from 11.0.7) (@keturn)

## 4.5.0 (2021-08-29)

### 🚀 Features

- #658 feat: hide pre-releases by default (@skaldarnar)
- #638 feat: resolve Terasology.jar either from 'libs' or 'lib' (@skaldarnar)
- #654 fix(GameStarter): do not depend on GameRelease to know the engine version (@keturn)
- #647 feat(GameIdentifier): include the engine version (@keturn)
- #646 feat: update command line options for Terasology (@keturn)

### 🐛 Bug Fixes

- #659 fix: pass JVM heap settings in correct order to GameStarter (@skaldarnar)
- #657 fix(LauncherSettings): do not send a zero-length argument (@keturn)
- #654 fix(GameStarter): do not depend on GameRelease to know the engine version (@keturn)

### 🧰 Maintenance

- #656 chore(JenkinsClient): use new HTTP client with better diagnostics (@keturn)
- #655 chore(GameIdentifier): remove engineVersion; rename String version to displayVersion (@keturn)
- #645 chore: update to jdk 11.0.12 from 11.0.8 (@keturn)
- #644 build: update Gradle 6.8.2 >>> 7.1.1 (@skaldarnar)

## 4.4.0 (2021-06-24)

### 🚀 Features

- #616 feat: add Github repository adapter to fetch releases from Github (@skaldarnar)

## 4.3.4 (2021-05-06)

### 🐛 Bug Fixes

- #641 fix: add connection timeout; remove legacy jenkins adapter (@skaldarnar)

## 4.3.3 (2021-05-02)

### 🐛 Bug Fixes

- #639 fix: use custom start scripts instead of brittle patch files (@skaldarnar)

### 🧰 Maintenance

- #637 build: remove jcenter (@jdrueckert)
- #636 build: remove jcenter dependency (@skaldarnar)

## 4.3.2 (2021-02-21)

### 🐛 Bug Fixes

- #631 fix: append build number to version in JenkinsRepositoryAdapter (@skaldarnar)

### 🧰 Maintenance

- #633 chore: address checkstyle warnings (add doc, optimize imports) (@skaldarnar)
- #632 chore: sort test payloads into V1 and V2 (addresses checkstyle warnings) (@skaldarnar)
- #629 test(JenkinsClient): add tests for exceptional cases on JenkinsClient (@skaldarnar)

## 4.3.1 (2021-02-16)

### 🐛 Bug Fixes

- #628 fix(controller): prevent NumberFormatException on starting game (@skaldarnar)
- #624 fix(repositories): make JenkinsRepositoryAdapter more resilient (@skaldarnar)

### 🧰 Maintenance

- #627 refactor(model): add ReleaseMetadata container (@skaldarnar)
- #626 test(repositories): add tests for LegacyJenkinsRepositoryAdapter (@skaldarnar)
- #625 test(repositories): prepare legacy adapter for testing (@skaldarnar)

## 4.3.0 (2021-01-30)

### 🚀 Features

- #621 feat(repositories): add adapter for new jenkins.terasology.io (@skaldarnar)

## 4.2.0 (2020-12-04)

### 🚀 Features

- #610 feature(lwjgl3): Add BaseJavaParameters for game, implement macos-specific lwgl3-related params. (@DarkWeird)
- #600 Game Runner improvements (take 2) (@keturn)

### 🐛 Bug Fixes

- #608 fix(i18n): update Ukrainian locale strings (@rzats)
- #614 fix(tests): fix flaky TestRunGameTask tests (@keturn)
- #613 fix: various issues (@jdrueckert)
- #574 fix: Download button tooltip error fixed (@TheShubham99)
- #598 test: Fix Windows tests (@Malanius)

### 🧰 Maintenance

- #619 epic: continue launcher clean-up (@skaldarnar)
- #618 build: update Gradle (6.4.1 >>> 6.7.1) (@skaldarnar)
- #617 chore: update license headers (@jdrueckert)
- #615 chore(tests): remove extraneous waitForFxEvents calls (@keturn)
- #607 chore: fix check run annotations, use new codemetrics version (@jdrueckert)
- #612 epic: Rework package management and UI (@skaldarnar)
- #605 chore: remove self updater (@skaldarnar)
- #604 chore: remove deprecations on LauncherConfig and LauncherSettings (@skaldarnar)
- #603 chore: Remove dead code (config package) (@skaldarnar)
- #602 chore: Remove unnecessary access modifiers on JUnit5 tests (@Malanius)

## 4.1.2 (2020-10-18)

### 🐛 Bug Fixes

- #599 fix: remove doubled files when bundling the JRE (@skaldarnar)
- #583 build: update JDK 11.0.7 >>> 11.0.8 and fix download task (@skaldarnar)
- #580 fix: typo in german translation of `logLevel_default` (@Seotte)

### 🧰 Maintenance

- #590 refactoring: Streamline LauncherSettings to ease replacement (@skaldarnar)
- #586 chore: organize GuiUtils and FXUtils into Effects and Dialogs (@skaldarnar)
- #587 chore[build]: add TestFX dependency (@keturn)
- #585 chore: replace custom LogLevel by slf4j.event.Level (@skaldarnar)
- #562 test: use spf4j-slf4j-test to unit test logging (@keturn)
- #569 chore: Remove Swagger configuration (@skaldarnar)
- #570 chore: Use new parser for markdown rendering (@skaldarnar)
- #567 chore: Replace jcabi Github client by github-api (@skaldarnar)
- #566 chore: Prepare for Java 14 (@skaldarnar)
- #564 chore: Remove usage of JNA to determine Windows path (@skaldarnar)

## 4.1.1 (2020-05-02)

### 🐛 Bug Fixes

- #560 fix: Remove Java 8 JVM arguments from settings (@skaldarnar)
- #559 fix: Remove unitialized variable in LauncherUpdater (@keturn)

## 4.1.0 (2020-05-01)

### 🚀 Features

- #548 feat: Upgrade to Java 11 (@keturn)
- #543 feat: replace `OperatingSystem` enum with `Platform` object (@jdrueckert)

### 🐛 Bug Fixes

- #552 fix: Don't block the application thread on message dialog (@keturn)

### 🧰 Maintenance

- #558 fix: Bundle the JRE, not the JDK. (@keturn)
- #556 test: Migrate to Junit5 (Jupiter) (@keturn)
- #555 chore: remove unused GameDownloader (@keturn)
- #540 chore: Extract private inner classes from ApplicationController (@skaldarnar)

## 4.0.0 (2020-03-29)

### 🚀 Features

- #532 feat: Validate schema of 'sources.json' (@praj-foss)
- #535 feat(usability): select last played or installed game (@jdrueckert)
- #531 feat: Show dialog for available updates (again) (@skaldarnar)
- #516 feat: Platform-specific distribution packages (@skaldarnar)
- #512 feat: Infer version from Git (@skaldarnar)
- #498 docs: move wiki into launcher repo (@jdrueckert)
- #501 feat(packages)!: better names for game versions (@skaldarnar)
- #503 feat: cleanup and clarify settings (@jdrueckert)
- #488 feature(jre): Automatically bundle JRE with distributions (@skaldarnar, @praj-foss, @jdrueckert)
- #486 Initializes combo boxes and buttons as soon as FXML is loaded (@praj-foss)
- #485 Indicate installed packages (@praj-foss)
- #484 Shows changelog of selected game package (@praj-foss)
- #472 Logging TextArea instead of TableView (@skaldarnar)
- #452 Provides updated PackageManager architecture (@praj-foss)
- #448 Basic package manager implementation (@praj-foss)
- #442 Adds gradle task to generate Web API client (@praj-foss)
- #444 Makes the game run using the bundled JRE (@praj-foss)
- #441 Bundles a JRE with the Launcher package (@praj-foss)
- #431 Modify the Settings UI (@praj-foss)

### 🐛 Bug Fixes

- #538 fix: Remove (failing) CrashReporter (@skaldarnar)
- #537 fix: small adjustments in settings view (@jdrueckert)
- #523 fix(buildres): linux run script (@jdrueckert)
- #511 fix(settings): typo in game arguments label (@jdrueckert)
- #502 fix: reset version box scroll bar when switching jobs (@skaldarnar)
- #500 fix(footer): Use java.awt.Desktop as fallback for HostServices (@skaldarnar)
- #494 fix: launcher logo (@jdrueckert)
- #457 Prevent NPEs when choosing a directories and init the Launcher (@skaldarnar)
- #434 Check availability of Jenkins (@praj-foss)

### 🧰 Maintenance

- #536 feat: merge 'games' directory into launcher installation directory (@skaldarnar)
- #534 chore: Resovle Checkstyle warnings (@skaldarnar)
- #529 fix: remove HostServices (only working with Oracle JRE) (@skaldarnar)
- #524 chore(settings): clean up properties (@jdrueckert)
- #527 chore: simplify FileUtils and fix "Unknown Windows" (@skaldarnar)
- #491 refactor(config): Dependency Inversion on `ConfigManager` and `ConfigReader` (@skaldarnar)
- #481 Provides new Configuration API (@praj-foss, @skaldarnar)
- #483 Fix Checkstyle errors (@skaldarnar)
- #477 Extract ChangelogView from main application controller (@skaldarnar, @jdrueckert)
- #478 Extract Footer from main application controller (@skaldarnar, @jdrueckert)
- #476 Remove duplicate game (type and version) selection (@skaldarnar, @jdrueckert)
- #480 Refactor AboutViewController (@jdrueckert, @skaldarnar)
- #475 Extract About tab controller (@skaldarnar)
- #458 Update Ukrainian locale with current strings (@rzats)
- #459 Remove G+ icon :( and add Discord icon instead :) (@rzats)
- #454 Clean-up repository (@skaldarnar)
- #453 Remove remnants of Java Webstart (@jdrueckert)
- #437 Simplify DownloadUtils::isJenkinsAvailable (@praj-foss)

## 3.3.0 (2019-06-08)

- Added missing try-catch for NIO operations (#412)
- Fixed UI layouts (#417)
- Combined _start_ and _download_ button (#418)
- Validate launcher settings for 32-bit JVM (#425)

## 3.2.0 (2017-07-08)

- Assorted translations (Polish, Hungarian, Ukrainian, German, Russian)
- Added tooltips to buttons (#376)
- Various code cleanup and refactoring (Checkstyle, PMD, Findbugs, Sonar)
- Added CREDITS and updated icons
- Added more Unit tests (#393, #394, #396)
- Replaced 'gradle install' with 'gradle installApp'
- 4K background image (#402)
- Switch file handling to Java NIO (#407)
- Removed guava dependency (#411)
- Fixed file and zip handling (#395, #403)

## 3.1.0 (2016-07-21)

- Fix for Jenkins upgrade causing available game version scanning to fail
- Sorts languages in drop-down menu for launcher settings
- Assorted translations
- Tiny flag icons for everybody woo!
- Better UI alignment
- Various code cleanup and refactoring
- Allow setting the game log level from the launcher
- Fix launcher hanging when the game data directory was edited on the settings page

## 3.0.0 (2015-12-16)

- More JavaFX improvements. Increases minimum Java 8 version to update 40
- Small freeze fix on some OSes
- More translations

## 2.0.2 (2015-05-09)

- Fixes Japanese
- Fixes loading bar stalling while processing available game versions

## 2.0.1 (2015-05-05)

- Reverted to old exe file until JRE bundling is entirely ready
- Marked Java 8 as _required_ now - no more Java 7 (makes sense anyway since Terasology won't run on 7)
- Fixed the JavaFX bug responsible for causing a stall before loading a file chooser dialog
- Launcher should exit more gracefully if the user cancels out of choosing initial directories to use

## 2.0.0 (2015-05-03

- First new major release. _Not_ backwards compatible.
- Shiny new look and feel using JavaFX
- Retired old "Legacy" and "Multi" version lines - Legacy still available in Jenkins
- Launcher now retrieves the "Omega" zip which is base Terasology + the modules in the standard lineup
- Structure of Terasology.zip changed, Terasology.jar is now inside the "libs" directory (no user action needed)
- More options for customizing the game launch (command line parameters)
- Changelogs are aggregated for older builds
- Substantial rework of how game and engine jars are detected and used
- Renamed "Nightly" to "Develop" and "Stable" to "Release" in various places
- Lots of translation work including several new languages
- Experimental support for JRE bundling and Java Webstart (more info later)
- Support for using the OS proxy server config
- Using Gradle 2.1 now and Java 8 for compiling (but not required at runtime yet)
- Probably more stuff we forgot about :-)

## 1.4.2 (2014-10-11)

- Fix for OS X when selecting game and launcher directory at first start
- Small GUI change (removed "Mods" link)

## 1.4.1 (skipped)

- Rebuild of v1.4.0 with new URLs after server crash

## 1.4.0 (2014-05-29)

- Translation into Polish
- Fallback for missing language translations
- New setting "Save downloaded files" and new "download" directory
- Update "TerasologyLauncher.exe"
- Better handling for failed or change-less game builds
- Delete old and unused cache files
- Note: Old cached information about game versions are not compatible. These are automatically reloaded.
- Internal changes
  - Restructure and refactor code
  - Change Checkstyle and FindBugs configurations
  - Load and log game engine version
  - More and better logging
  - Some small bug fixes
  - Update compile and runtime dependencies
  - Update gradle wrapper (1.10 -> 1.12)
  - Update launch4j (3.0.2 -> 3.4)

## 1.3.1 (2014-03-25)

- Allow all memory (heap size) values (512 MB - 16 GB)
- Optimize zip file download (checks, logging, timeout, file name, ...)
- Delete content of game installation sub directory before downloading and extracting a game version

## 1.3.0 (2014-02-23)

- Translation into Russian
- Optimize language selection
- Optimize launcher self update
- Note: Old cached information about game versions are not compatible. These are automatically reloaded.
- Internal changes
  - Update libraries (logback, slf4j)
  - Restructure and refactor code
  - Some small bug fixes

## 1.2.0 (2014-01-17)

- Choose, edit, show and use game data directory
- Translation into Spanish
- Fix for finding game version information
- Show outdated nightly multiplayer job only if it is installed
- Show old nightly versions only if they are installed
- Internal changes
  - Gradle update (1.10)
  - Java source code check tool 'FindBugs'
  - License header supports year 2014

## 1.1.2 (2013-11-22)

- Fix for a wrong game version info (git branch)

## 1.1.1 (2013-11-08)

- Fix wrong horizontal scroll bar
- Add progress indicators
  - Downloading and updating the launcher
  - Loading game versions (with change log)
- Change min java version for 'TerasologyLauncher.exe' (1.7.0)
- Avoid unnecessary downloading game version info
- Some minor internal code improvements (checkstyle, warnings, logging)

## 1.1.0 (2013-10-27)

- Show launcher _change log_
- Cancel game download
- Add new game type "legacy"
  - STABLE : TerasologyLegacy (legacy, pre-multiplayer)
- Supports new structure ('The Great Convergence')
- Internal changes
  - Gradle update (1.8)
  - Java source code check tool 'PMD'
  - Better support for 'IntelliJ-IDEA'

## 1.0.0 (2013-07-22)

- Download, install and start the game _Terasology_
  - Supports setting a custom game installation directory and managing multiple installations / versions
  - Supports three game types
    - STABLE : TerasologyStable (master)
    - NIGHTLY : Terasology (develop)
    - NIGHTLY : TerasologyMulti (multiplayer)
  - Supports memory settings (Java heap size)
  - Displays comments for each version (git comment)
- Download, install and start the launcher (self updatable)
- Supports multiple languages (english, german)
- Displays many "Terasology"-related links
