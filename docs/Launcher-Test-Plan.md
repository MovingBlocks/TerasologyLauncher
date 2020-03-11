[[HOME|Home]] > [[TERASOLOGY LAUNCHER DOCUMENTATION|Documentation]] > [[LAUNCHER TEST PLAN|launcher-test-plan]]

This page will cover a test plan for the Terasology launcher.

# Set-up

Before beginning, ensure that you have downloaded the latest version of the Terasology Launcher and that it can run without any errors following this page: https://github.com/MovingBlocks/TerasologyLauncher/wiki/Getting-Started

Open up the Terasology launcher, and you should be presented with a similar screen to this (the changelog might differ):

![](http://i.imgur.com/bNg2WVa.jpg)

# Launcher Tests

Take note that there are different language settings available for the launcher and that these tests are based on the English version of the launcher.

2. [Installing and Uninstalling](#install)
3. [Tabs](#uninstall)
4. [Social Media](#media)
5. [Miscellaneous](#misc)


<a name="install"></a>
## Installing and Uninstalling
- Ensure that you are downloading the Stable release of Terasology using the dropdown menu in the Settings menu
- Download the Stable release by clicking on the "Download" icon in the toolbar
- After it has downloaded, run Terasology by clicking the "Play" icon in the toolbar
- [ ] Verify that Terasology starts
- [ ] Verify that the Stable version of Terasology is running as the version information contains the word 'Stable'


![](http://i.imgur.com/GU8uXmS.png)


- Now, change the type to the Development build using the dropdown menu 
- Download the Development release by clicking on the "Download" icon to the right of the dropdown menu
- [ ] Verify that a progress bar appears, indicating the progress of the download
- After it has downloaded, run Terasology by clicking the "Play" icon
- [ ] Verify that Terasology starts
- [ ] Verify that the Development release of Terasology is running as the version information does not contain the word 'Stable'


![](http://i.imgur.com/6mIt9TT.png)


- Close the game and uninstall it by clicking the "Trash" icon in the toolbar
- [ ] Verify that a confirmation dialog pops up confirming the uninstallation
 - [ ] Verify that the directory shown in the dialog matches the current release and version of Terasology selected
 - [ ] Verify that clicking "Cancel" does nothing
 - [ ] Verify that clicking "Ok" causes the "Play" icon to dim and the "Download" icon to brightens up, indicating that the game has been uninstalled  

---

- Ensure that you are downloading the one version of Terasology (v1) using the dropdown menus in the Settings menu
- Download the Terasology (v1) by clicking on the "Download" icon in the toolbar
- After it has download, run Terasology by clicking the "Play" icon in the toolbar
- [ ] Verify that the Terasology (v1) is running from the version information in the main menu. For instance, the image below shows the version information for alpha - 70:


![](http://i.imgur.com/GU8uXmS.png)


- Close the game. Change the Game: Version setting in the Settings menu to another version (v2)
- Download Terasology (v2) by clicking on the "Download" icon in the toolbar
- After it has downloaded, run Terasology by clicking the "Play" icon in the toolbar
- [ ] Verify that the Terasology (v2) is running from the version information in the main menu. For instance, the image below shows the version information for alpha - 68:


![](http://i.imgur.com/BgTG2sB.png)


<a name="tabs"></a>
## Tabs
- [ ] Verify that there are 3 tabs shown in the launcher: "Changelog", "About" and "Log"
- [ ] Verify that the Terasology changelog shown under the "Changelog" tab corresponds to the current version selected in the dropdown menus
- Change the current version of Terasology using the dropdown menus
- [ ] Verify that the Terasology changelog shown under the "Changelog" tab changes accordingly to match the new version. For instance, if you are selecting version 70 - alpha of the Stable release, you should see this changelog:


![](http://i.imgur.com/LqLbIwR.png)


- Switch to the "About" tab 
- [ ] Verify that you are presented with 4 different section: README.md, CHANGELOG.md, CONTRIBUTING.md and LICENSE


![](http://i.imgur.com/3M1xdoK.gif)

- Verify that the documents shown under each of these tabs correspond to the documents found in the TerasologyLauncher repository
 - [ ] README.md - https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/README.md
 - [ ] CHANGELOG.md - https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/CHANGELOG.md
 - [ ] CONTRIBUTING.md - https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/CONTRIBUTING.md
 - [ ] LICENSE - https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/LICENSE

---

- Switching to the "Log" tab, you should be presented with a table listing logs from the launcher
- [ ] Verify that there are 3 columns in the table with the appropriate information is each column: "Timestamp", "Level" and "Message" (Note that the logs may not correspond exactly to the image below)


![](http://i.imgur.com/ToLTb1J.png)

<a name="media"></a>
## Social Media
- [ ] Verify that you see 6 different icons at the bottom of the launcher, from left to right: 
Facebook, Github, Google+, Reddit, Twitter and Youtube 


![](http://i.imgur.com/GIRbVQO.png)

- Verify that clicking each of these icons opens the corresponding Terasology page in the web browser
 - [ ] Facebook - Terasology's Facebook profile (https://www.facebook.com/Terasology)
 - [ ] Github - Terasology's Github repository (https://www.github.com/MovingBlocks/Terasology)
 - [ ] Google+ - Terasology's Google+ profile (https://plus.google.com/103835217961917018533)
 - [ ] Reddit - Terasology's subreddit (https://www.reddit.com/r/Terasology/)
 - [ ] Twitter - Terasology's Twitter profile (https://twitter.com/Terasology/)
 - [ ] Youtube - Terasology's Youtube channel (https://www.youtube.com/user/blockmaniaTV/)

<a name="misc"></a>
## Miscellaneous
- [ ] Verify that the version number at the bottom right matches the version number in the "Changelog" tab and the window title
- [ ] Verify that clicking the "Leave" icon in the toolbar closes the Terasology launcher

![](http://i.imgur.com/7gi6jOj.gif)