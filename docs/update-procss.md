# Terasology Launcher Update Process

## steps
1. check for new version
1. download & unpack new version into tmp dir
1. start self-updater
1. copy/replace old files
1. start new launcher
1. delete tmp files

## places
1. -> GitHub API
1. -> /tmp/terasologylauncher/newVersion.zip
   -> /tmp/terasologylauncher/newVersion-linux64/bin
   -> /tmp/terasologylauncher/newVersion-linux64/lib
   ...
1. -> /tmp/terasologylauncher/newVersion-linux64/lib/self-updater.jar
1. -> /tmp/terasologylauncher/newVersion-linux64/ -> /home/user/.terasologylauncher/
1. -> /home/user/.terasologylauncher/bin/terasologylauncher
1. -> /tmp
