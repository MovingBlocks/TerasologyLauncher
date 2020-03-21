# Launcher Architecture
This is an overview of the launcher's architecture from
a high level. Following are the main components of the
launcher.

### Views
Present under `resources` directory, inside
`org.terasology.launcher.views`.
It describes the UI layout using FXML files.

### Controllers
Present inside `org.terasology.launcher.gui.javafx`.
It connects the views to the underlying services
of the launcher.

### Package Manager
Present inside `org.terasology.launcher.packages`.
This service is used to manage the game packages.

### Configuration Manager
Present inside `org.terasology.launcher.config`
and `org.terasology.launcher.settings`.
This service is used to manage configuration data
and other app settings.

### Update Manager
Present inside `org.terasology.launcher.updater`.
This is used to check for updates and start one
if available.

### Game Manager
Present inside `org.terasology.launcher.game`.
It's used to launch the games using packages
installed by the Package Manager and settings
saved by the Config Manager. In the future, it
should be broken down into modules that can
would be handled by the Package Manager.

### Utilities
Present inside `org.terasology.launcher.util`.
Contains various utility classes shared by all
other modules to do common operations like
connecting to Jenkins or downloading files.
