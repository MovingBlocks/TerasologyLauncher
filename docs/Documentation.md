[[HOME|Home]] > [[TERASOLOGY LAUNCHER DOCUMENTATION|Documentation]]

<img align="left" width="96px" src="images/documentation.png"/>
The technical documentation of **TerasologyLauncher** aims to explain the used technologies and underlying concepts. We 
are trying to keep the hurdle for new contributors as low as possible.

Versioning
----------
This project uses "[Semantic Versioning][SemVer]" for specifying versions and build numbers. The version number is 
composed of three parts:

~~~
MAJOR.MINOR.PATCH
~~~
The `MAJOR` version is changed on breaking changes, e.g. when migrating from a Swing UI to JavaFX. The `MINOR` version
is increased when small feature enhancements or other non-breaking changes are introduced. The `PATCH` version indicates 
 patches and bug fixes that don't add functionality to the launcher. 
 
The current version is stored in the file `version.txt`.

We have a detailed guide on how to [[release a new version|create-new-release]]. 

Planned features for future versions and concrete milestone goals can be found under our [GitHub Milestones](https://github.com/MovingBlocks/TerasologyLauncher/milestones).

Building [![Build Status](http://jenkins.terasology.org/view/Launcher/job/TerasologyLauncherStable/badge/icon)](http://jenkins.terasology.org/view/Launcher/job/TerasologyLauncherStable/)
--------------------------------------
This section covers the build process of **TerasologyLauncher**. In general, we use [Gradle](gradle.org) as build 
tool and [Jenkins](jenkins-ci.org) for CI.

### Gradle

See [[Gradle Overview|setup-guide#overview]].

### Jenkins
All Jenkins jobs regarding the launcher can be found under the [Jenkins Launcher View](http://jenkins.terasology.org/view/Launcher/).
We have set up automatic builds for [Nightly Builds](http://jenkins.terasology.org/view/Launcher/job/TerasologyLauncherNightly/), triggered by commits on GitHub.

Translations [![Translation status](http://translate.terasology.org/widgets/terasologylauncher-shields-badge.svg)](http://translate.terasology.org/engage/terasologylauncher/?utm_source=widget)
--------------------------------------
We attach great importance on the usabiilty of the launcher - all over the world. Therefore, internationalization is a main goal for the development of **TerasologyLauncher**. We currently support 5 languages:
 
 - English
 - German
 - Spanish
 - Polish
 - Russian

Translation is very easy, you just have to translate a bunch of text snippets (labels, error messages, and so on).

The easiest way of helping with translations is to use our [Weblate](http://weblate.org/) web interface at [translate.terasology.org](http://translate.terasology.org/). For more information, go to our guide on [[How to Add a New Translation|add-new-translation]].
Don't worry if you don't want to sign for GitHub or deal with source code technicalities - just drop by on the [forum](forum.terasology.org) or on IRC `#terasology` and contact us!

More in-depth technical information about the Weblate instance is documented in the main project's wiki under [Weblate Setup](https://github.com/MovingBlocks/Terasology/wiki/Weblate-Setup).

Java Web Start
--------------
We use [Java Web Start (JWS)](http://www.oracle.com/technetwork/java/javase/javawebstart/index.html) to deploy the launcher. It ensures the most current versions of the application, as well as the correct version of the Java Runtime Environment (JRE), will be deployed.

### Run webstart

There are [different possibilities](http://www.java.com/en/download/faq/java_webstart.xml) to run JWS files:

* Click the link to the `webstart.jnlp` file (Java must be activated in the browser to work)

* On the first run, JWS will install a shortcut on the Desktop which can be used for later runs

* From the command prompt: **Start** -> **Run** -> `javaws <LINK>` 

### Technical Background

The application is described in a file `webstart.jnlp` as a set of jars (main application plus dependencies) which will be downloaded and run by JWS when executed.

The file can be manually created by the gradle task `webstart`. Our Continuous Integration (CI) server does that on a regular basis. The latest automated build (see Jenkins section) is archived there.

The webstart process requires all jars to be signed. We use a root-certificate by [certum.pl](http://certum.pl) to do that.

[SemVer]: http://semver.org/ "SemVer"