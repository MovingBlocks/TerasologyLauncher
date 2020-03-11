## Initial Setup
To be able to run **TerasologyLauncher** from source follow these steps. This guide is designed for [IntelliJ IDEA](http://www.jetbrains.com/idea/) (you can use the free community edition), but alternative setups are possible.
Furthermore, you will need [Git](https://git-scm.com) for source control and sign up for [GitHub](https://github.com/signup/free).

### Step 1 - Fork the project
To get ready to tinker with the source code, *fork* the project by opening the [**TerasologyLauncher**](https://github.com/MovingBlocks/TerasologyLauncher) GitHub page and clicking on the little fork button in the upper right. 

### Step 2 - Clone the repo to your hard drive 
There are several ways to get the source code on your local workspace:
- Use the command line by executing `git clone https://github.com/MovingBlocks/TerasologyLauncher.git` (replace `MovingBlocks` with your GitHub username to use your own fork)
- Use IntelliJ's feature "Check out from version control" and follow the prompts
- Use [GitHub for Windows](https://windows.github.com/) or [GitHub for Mac](https://mac.github.com/) to clone the repo

If you cloned your own fork, you might want to set the original repository as a remote. To do so, run

~~~
git remote add movingblocks https://github.com/MovingBlocks/TerasologyLauncher.git
~~~

### Step 3 - Generate project files and open in IntelliJ
To generate the project files for IntelliJ, open a command prompt in the directory you checked out into and execute `gradlew idea` - this fetches the right version of Gradle and all project dependencies automatically, as well as generates the project config. Afterwards, you can simply open the project file in IntelliJ. 

There is also a version for Eclipse - `gradlew eclipse` - but we encourage you to use IntelliJ.

## Development and Contribution
To get the latest changes from your remote repositories (e.g. `movingblocks`) you need to *fetch* all remote data via `git fetch --all`. This does not change your workspace, it just loads up your local Git database.

Familiarise yourself with Git's concept of repositories, branches, and commits. You can find some resources on how to use Git at the bottom of this page. 
 
Assume you have pushed some changes to your fork into a branch `myFeature`. In order to let us know about your work and give us the possibility to incorporate your changes you should send us a *pull request*. You can do this by selecting the `myFeature` branch on you GitHub repo and click the button which says "Open pull request". More information on how to contribute can be found in [CONTRIBUTING.md](https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/CONTRIBUTING.md).

## Related Resources
Tutorials and further information on Git:
- http://www.vogella.de/articles/Git/article.html
- http://gitref.org/
- http://progit.org/

Developer setup tutorials for our main project, [**Terasology**](https://github.com/MovingBlocks/Terasology):
- [Dev Setup](https://github.com/MovingBlocks/Terasology/wiki/Dev-Setup)
- [Dealing with Forks](https://github.com/MovingBlocks/Terasology/wiki/Dealing-with-Forks)
