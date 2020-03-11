[[HOME|Home]] > [[TERASOLOGY LAUNCHER DOCUMENTATION|Documentation]]

<img align="left" width="96px" src="images/documentation.png"/>
The technical documentation of **TerasologyLauncher** aims to explain the used technologies and underlying concepts. We 
are trying to keep the hurdle for new contributors as low as possible.

## Versioning

This project uses "[Semantic Versioning][semver]" for specifying versions and build numbers. The version number is
composed of three parts:

```
MAJOR.MINOR.PATCH
```

The `MAJOR` version is changed on breaking changes, e.g. when migrating from a Swing UI to JavaFX. The `MINOR` version
is increased when small feature enhancements or other non-breaking changes are introduced. The `PATCH` version indicates
patches and bug fixes that don't add functionality to the launcher.

The current version is stored in the file `version.txt`.

We have a detailed guide on how to [[release a new version|create-new-release]].

Planned features for future versions and concrete milestone goals can be found under our [GitHub Milestones](https://github.com/MovingBlocks/TerasologyLauncher/milestones).

## CI/CD

### Github Actions
