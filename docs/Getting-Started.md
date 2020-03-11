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
