[[HOME|Home]] > [[TERASOLOGY LAUNCHER DOCUMENTATION|Documentation]] > [[CREATE NEW RELEASE|create-new-release]]

## Release candidate (branch "develop")

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

## Release (branch "master")

1. Push local "master" branch

   git push --tags movingblocks master

2. Build jenkins job [TerasologyLauncherStable](http://jenkins.movingblocks.net/view/Launcher/job/TerasologyLauncherStable/)

   View test and check results

   Download and test created version

3. Close GitHub Milestone

## Prepare next release (branch "develop")

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
