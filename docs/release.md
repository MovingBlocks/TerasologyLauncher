# Create a Release

This guide is directed at maintainers of the Terasology Launcher. 
It explains how to create a new release, including building the release assets and publishing them as GitHub release.

- Make sure all relevant changes are merged.
  
  Please also check the [milestones](https://github.com/MovingBlocks/TerasologyLauncher/milestones) for any outstanding tasks or issues.

- Check out the main branch and update your local checkout:

  ```sh
  git switch master
  git pull
  ```

- Update the release changelog.

  You might want to use [gh-terasology](https://github.com/skaldarnar/gh-terasology) to generate the changelog:
  
  ```sh 
  gh terasology changelog --pretty \
    --since=$(git show -s -1 --format=%ai v4.5.0) \
    --repo movingblocks/terasologylauncher 
  ```

- commit the changes to the changelog

  ```sh 
  git add CHANGELOG.md 
  git commit -m "release: <version>"
  ```

- tag the release commit:

  ```sh
  git tag -a v<version> -m "Release version <version>"
  ```

- push both release commit and release tag 

  ```sh
  git push
  git push v<version>
  ```

- go to [releases](https://github.com/MovingBlocks/TerasologyLauncher/releases) and publish the respective release via the GitHub UI.

  The [asset-upload](https://github.com/MovingBlocks/TerasologyLauncher/blob/master/.github/workflows/asset-upload.yml) GitHub workflow will build the launcher for all supported platforms and upload the assets automatically.
