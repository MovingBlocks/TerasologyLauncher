# Launcher Architecture
This is an overview of the launcher's architecture from
a high level. Following are the main components of the
launcher. All the application data is stored inside
the following directories:

|OS      |Path      |
|--------|----------|
|Windows |`C:\Users\[USER]\AppData\Roaming\TerasologyLauncher`|
|OS X    |`/Users/[USER]/Library/Application Support/TerasologyLauncher`|
|Linux   |`/home/[USER]/.terasologylauncher`|

### Init Service
Present inside `org.terasology.launcher`. (Top level)<br/>
Used to properly initialize the components and
start other necessary services. Important functions are:
- Displaying Splash screen
- Starting Update Manager
- Starting Package Manager
- Starting Config Manager

### Views
Present under `resources` directory, inside
`org.terasology.launcher.views`.<br/>
It describes the UI layout using FXML files.

### Controllers
Present inside `org.terasology.launcher.gui.javafx`.<br/>
It connects the views to the underlying services
of the launcher.

### Package Manager
Present inside `org.terasology.launcher.packages`.<br/>
This service is used to manage the game packages.
The `sources.json` file lists all the online
repositories to download the packages from. There
is a Package Database that keeps track of all
packages available in the mentioned repositories.
It saves their information in the `packages.db` 
file. The `cache` folder stores all previously 
downloaded packages, and can later be used for
offline installation.

### Configuration Manager
Present inside `org.terasology.launcher.config`
and `org.terasology.launcher.settings`.<br/>
This service is used to manage configuration data
and other app settings. Currently, it saves the
settings in `TerasologyLauncherSettings.properties`
file. 

### Update Manager
Present inside `org.terasology.launcher.updater`.<br/>
This is used to check for updates and start one
if available.

### Game Manager
Present inside `org.terasology.launcher.game`.<br/>
It's used to launch the games using packages
installed by the Package Manager and settings
saved by the Config Manager. In the future, it
should be broken down into modules that can
would be handled by the Package Manager.

### Utilities
Present inside `org.terasology.launcher.util`.<br/>
Contains various utility classes shared by all
other modules to do common operations like
connecting to Jenkins or downloading files.
