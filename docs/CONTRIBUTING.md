# Contributing to Terasology

If you would like to contribute code, documentation, or other assets, you can do so through GitHub by forking the repository and sending a pull request (PR). You can also simply report issues (bugs), but we ask you to search for any previous reports first.

*When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.*

Please follow our [Contributor Covenant Code of Conduct][code of conduct]. For questions please contact our [community][community].

## File an Issue

You can report bugs and feature requests to [GitHub Issues][github issues]. As mentioned earlier, please look for a similar existing issue before submitting a new one.

__Please provide as much information as possible to help us solve problems and answer questions better!__

Please don't use GitHub Issues for questions and support requests, but contact our [community][community] instead.

For finding easy to do issues to start contributing, look at the ["Bite-size" labeled issues][github issues bitesize].

## Commits and Commit Messages

Follow these guidelines when creating public commits and writing commit messages.

1. If your work spans multiple local commits, make sure they can be merged directly into the target branch. If you have commit and merge noise in the history have a look at [git-rebase][git rebase]. Ideally, every commit should be able to be used in isolation -- that is, each commit must build and pass all tests.
1. The first line of the commit message should be a descriptive sentence about what the commit is doing. It should be possible to fully understand what the commit does by just reading this single line. 
1. **Optional:** Please try to use [conventional commits][conventional commits]

## License

Submissions must be licensed under the [Apache License, Version 2.0][license].

If you are adding a new file it should have a header like this (automatically available as a template in IntelliJ):
~~~
/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
~~~

## How to Submit a Pull Request

Pull requests are highly appreciated! Please follow the guidelines below. 

1. [Fork the repository][github forking] and make your changes. We advise to use [feature branches][feature branch] for every separate change you make. 
1. Run the tests and do additional manual checks to be sure everything is working as expected. 
1. [Create a pull request][github pull request]. Where applicable, you can [reference issues][github pr link issue] in the PR description.
1. Watch for comments or acceptance on your PR. The PR can be updated by just pushing to the original branch.

__Please note:__ if you want to change additional things that don't relate to what you have been working on, make sure you create a new branch off the `master` branch before making more changes - this way we can take in each change separately.

## How We Merge Pull Requests

For trusted contributors with push access to our root repos you are welcome to help merge pull requests.

1. Our review process roughly follows these categories:
    - **Trivial**: A single typo fix in a comment or other *inactive text*. Does not require further review.
    - **Patch**: Small bug fixes. Needs careful review to prevent unexpected impact.
    - **Minor**: Substantial code changes / additions. Needs thorough testing. May require reviews from multiple contributors.
    - **Major**: Breaking changes! Need consideration concerning feature deprecation and backwards compatibility.
1. We will review the changes and report any issues or concerns as comments in the PR and ping appropriate contributors with a `@username` mention to join the discussion or do an additional review of the changes.
1. If sufficient review for the PR category has been done and no issues have been noted, the PR will be _squash-merged_.

## Related documentation

- [Conventional Commits][conventional commits]
- [How to contribute translations](Add-New-Translation.md)

<!-- References -->
[license]: http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0"
[community]: /README.md#Community "Terasology Community"
[readme]: /README.md "TerasologyLauncher Documentation"
[code of conduct]: /docs/CODE_OF_CONDUCT.md "Contributor Covenant Code of Conduct"
[github]: https://github.com/MovingBlocks/TerasologyLauncher "GitHub"
[github issues]: https://github.com/MovingBlocks/TerasologyLauncher/issues "GitHub Issues"
[github issues bitesize]: https://github.com/MovingBlocks/TerasologyLauncher/labels/Bite-size "Bite-size GitHub Issues"

[conventional commits]: https://www.conventionalcommits.org/en/v1.0.0/ "Conventional Commits"
[feature branch]: https://www.atlassian.com/git/tutorials/comparing-workflows/feature-branch-workflow "Feature Branch Workflow"
[github forking]: https://guides.github.com/activities/forking/ "GitHub Forking"
[git rebase]: https://git-scm.com/docs/git-rebase "Git Rebase"
[github pull request]: https://help.github.com/articles/using-pull-requests/ "GitHub Pull Requests"
[github pr link issue]: https://help.github.com/en/github/managing-your-work-on-github/linking-a-pull-request-to-an-issue "Github Linking a Pull Request to an Issue"

