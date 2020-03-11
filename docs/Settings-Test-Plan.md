[[HOME|Home]] > [[TERASOLOGY LAUNCHER DOCUMENTATION|Documentation]] > [[SETTINGS TEST PLAN|settings-test-plan]]

This page will cover a test plan for the settings menu in the Terasology launcher.
# Set-up
Before beginning, ensure that you have downloaded the latest version of the Terasology Launcher and that it can run without any errors, following this page: https://github.com/MovingBlocks/TerasologyLauncher/wiki/Getting-Started

Open up the Terasology launcher, click the "Settings" icon in the top left of the launcher, and you should be presented with this screen:

![](http://i.imgur.com/Qb3IQo1.png)

# Launcher Settings Tests

Before performing each test, please ensure that you currently do not have any Terasology installed by clicking the "Trash" icon in the toolbar for each version that you have installed. 

Take note that there are different language settings available for the launcher and that these tests are based on the English version of the launcher. After changing each setting, remember to click the Save button.

(Tests with a '*' indicate that they require something to be downloaded)

1. [Settings: Save and Cancel](#save)
2. [Game: Type *](#type)
3. [Game: Version *](#version)
4. [Game: Installation Directory *](#install_dir)
5. [Game: Data Directory](#data_dir)
6. [Game: Maximum Memory](#maxram)
7. [Game: Initial Memory](#initialram)
7. [Launcher: Language](#language)
8. [Launcher: Close launcher after game start](#close)

<a name="save"></a>
## Settings: Save and Cancel (Currently bugged for game Type and Version)

- Change the Terasology type and the maximum memory using the dropdown menu in the Settings menu
- Click the "Save" button 
- [ ] Reopen the Settings menu and verify that the Terasology type remains changed

---

- Change the Terasology type and the maximum memory using the dropdown menu in the Settings menu
- Click the "Cancel" button
- [ ] Reopen the Settings menu and verify that the settings did not change


<a name="type"></a>
## Game: Type *
- Ensure that you are downloading the Stable release (normal) of Terasology using the dropdown menu in the Settings menu
- Download the Stable release by clicking on the "Download" icon in the toolbar
- After it has downloaded, run Terasology by clicking the "Play" icon in the toolbar
- [ ] Verify that the Stable version of Terasology is running as the version information contains the word 'Stable'


![](http://i.imgur.com/GU8uXmS.png)


 Close the game. Change the Game: Type setting in the Settings menu to Development build (normal)
- Download the Development release by clicking on the "Download" icon to the right of the dropdown menu
- After it has downloaded, run Terasology by clicking the "Play" icon
- [ ] Verify that the Development release of Terasology is running as the version information does not contain the word 'Stable'

![](http://i.imgur.com/6mIt9TT.png)

<a name="version"></a>
## Game: Version *
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

<a name="install_dir"></a>
## Game: Installation Directory *

- Take note the default installation directory - for instance in the first image above, you can see that it is at "C:\Users\Desktop\Isaac"
- Ensure that you are installing the latest version of the Stable release using the dropdown menus in the Settings menu
- Download Terasology by clicking on the "Download" icon in the toolbar
- [ ] Verify that a new folder "RELEASE" in created in your default installation directory, containing a "Terasology Stable" folder

---

- Change the default installation directory to a directory of your choice using the "Edit" button below the setting
- Verify that a file selection window pops up
- Repeat steps 2 and 3
- [ ] Verify that a new folder "RELEASE" in created in the new directory, containing a "Terasology Stable" folder

<a name="data_dir"></a>
## Game: Data Directory

- Take note the default data directory - for instance in the first image above, you can see that it is at "C:\Users\Desktop\Isaac"
- Ensure that you are installing the latest version of the Stable release using the dropdown menus in the Settings menu.
- Download Terasology by clicking on the "Download" icon in the toolbar
- Run Terasology after it has been downloaded
- [ ] Verify that new data directories "saves", "logs" and "module" have been created in your default data directory

---

- Change the default data directory to a directory of your choice using the "Edit" button below the setting
- Verify that a file selection window pops up
- Repeat steps 2 to 4
- [ ] erify that new data directories "saves", "logs" and "module" have been created in new data directory

<a name="maxram"></a>
## Game: Maximum Memory (Currently bugged)
- Set the maximum memory to 2 GB in the Settings menu
- Run Terasology using the Play button in the toolbar
- Create a new save and leave the game running
- [ ] Using the Task Manager (or its equivalent on other platforms), verify that the memory used by the Terasology process does not exceed 2 GB. You can also make use of the in-game debug overlay (F3) to monitor the game's memory usage


![](http://i.imgur.com/Ixs9Zdn.png)


- Set the maximum memory to 3 GB in the Settings menu
- Run Terasology again using the Play button in the toolbar
- Create a new save and leave the game running
- [ ] Using the Task Manager (or its equivalent on other platforms), verify that the memory used by the Terasology process exceeds 2 GB but not 3 GB. You can also make use of the in-game debug overlay (F3) to monitor the game's memory usage

<a name="initialram"></a>
## Game: Initial Memory (Currently bugged)
- Set the minimum memory to 2 GB in the Settings menu
- Open Task Manager and get ready to look out the initial memory of Terasology
- Run Terasology using the Play button in the toolbar
- [ ] Using the Task Manager (or its equivalent on other platforms), verify that the initial memory used by the Terasology process starts from 2 GB

---

- Set the minimum memory to 3 GB in the Settings menu
- Open Task Manager and get ready to look out the initial memory of Terasology
- Run Terasology using the Play button in the toolbar
- [ ] Using the Task Manager (or its equivalent on other platforms), verify that the initial memory used by the Terasology process starts from 3 GB



<a name="language"></a>
## Launcher: Language

- Verify that the menus in the launcher are in English


![](http://i.imgur.com/YDbivc0.png)


- Change the current language to Spanish using the dropdown menu in the Settings menu
- [ ] Verify that the menus in the launcher are now in Spanish!


![](http://i.imgur.com/sggK8Fk.png) 


<a name="close"></a>
## Launcher: Close launcher after game start
- Ensure that the "Close launcher after game start" checkbox is not ticked
- Ensure that you are installing the latest version of the Stable release using the dropdown menus in the Settings menu
- Download Terasology by clicking on the "Download" icon in the toolbar
- After Terasology has been installed, start it by clicking the Play button in the toolbar
- [ ] Verify that the launcher remains open  

---

- Close Terasology
- Ensure that the "Close launcher after game start" checkbox is now ticked
- Restart Terasology by clicking the Play button in the toolbar
- [ ] Verify that the launcher now closes when Terasology is started