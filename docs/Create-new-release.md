[[HOME|Home]] > [[TERASOLOGY LAUNCHER DOCUMENTATION|Documentation]] > [[CREATE NEW RELEASE|create-new-release]]

Release candidate (branch "develop")
------------------------------------
1. Build a local development release

    gradlew clean createRelease

    gradlew run

2. Update file [version.txt](/MovingBlocks/TerasologyLauncher/blob/develop/version.txt)

    Example: 1.1.0-rc

3. Update file [CHANGELOG.md](/MovingBlocks/TerasologyLauncher/blob/develop/CHANGELOG.md)

    Example: 1.1.0 (2013-10-27, unreleased)

4. Commit both files

    Example: "Version 1.1.0-rc (develop)"

5. Push local "develop" branch

    git push movingblocks develop

6. Build jenkins job [TerasologyLauncherNightly](http://jenkins.movingblocks.net/view/Launcher/job/TerasologyLauncherNightly/)

    View test and check results

    Download and test created version

Prepare release (branch "develop", local git workspace)
-------------------------------------------------------
1. Update file [version.txt](/MovingBlocks/TerasologyLauncher/blob/develop/version.txt)

    Example: 1.1.0

2. Update file [CHANGELOG.md](/MovingBlocks/TerasologyLauncher/blob/develop/CHANGELOG.md). Furthermore, check for new contributors and add them to README.

    Example: 1.1.0 (2013-10-27)

3. Commit both files

    Example: "Release 1.1.0"

4. Create a tag

    Example: git tag -a 'v1.1.0' -m "Version 1.1.0"

Merge "develop" into "master" (local git workspace)
---------------------------------------------------
1. git checkout master

2. git merge --ff-only develop

3. Gradle test

    gradlew clean createRelease

    gradlew run

Release (branch "master")
-------------------------
1. Push local "master" branch

    git push --tags movingblocks master

2. Build jenkins job [TerasologyLauncherStable](http://jenkins.movingblocks.net/view/Launcher/job/TerasologyLauncherStable/)

    View test and check results

    Download and test created version

3. Close GitHub Milestone

Prepare next release (branch "develop")
---------------------------------------
1. git checkout develop

2. Update file [version.txt](/MovingBlocks/TerasologyLauncher/blob/develop/version.txt)

    Example: 1.1.1-0

3. Update file [CHANGELOG.md](/MovingBlocks/TerasologyLauncher/blob/develop/CHANGELOG.md)

    Example: x.y.z (unreleased)

4. Commit both files

    Example: "Version 1.1.1-0 (develop)"

5. Push local "develop" branch

    git push --tags movingblocks develop

    git push --tags origin develop

6. Create new GitHub Milestone

Announcements
-------------
1. [GitHub Releases](https://github.com/MovingBlocks/TerasologyLauncher/releases)

    Draft a new release

    Choose "Target: master"

    Enter/Choose existing tag version (Example v1.1.0)

    Set Release title (Example: Release 1.1.0)

    Copy Changelog

    Attach binary "TerasologyLauncher.zip"

    Publish release    

2. [Jenkins TerasologyLauncherStable](http://jenkins.movingblocks.net/view/Launcher/job/TerasologyLauncherStable/lastBuild/)

    Change displayname and description

3. Forum (english, german), Twitter, Facebook
