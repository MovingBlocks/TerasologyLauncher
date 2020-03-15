Contributing to Terasology
==================================

If you would like to contribute code, documentation, or other assets, you can do so through GitHub by forking the repository and sending a pull request (PR). You can also simply report issues (bugs), but we aks you to search for any previous reports first.

*When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.*

Please follow our [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). For questions please contact our [community](/README.md#Community).

File an Issue
-------------
You can report bugs and feature requests to [GitHub Issues](https://github.com/MovingBlocks/TerasologyLauncher/issues). As mentioned earlier, please look for a similar existing issue before submitting a new one.

__Please provide as much information as possible to help us solve problems and answer questions better!__

Please don't use GitHub Issues for questions and support requests, but contact our [community](/README.md#Community) instead.

For finding easy to do issues to start contributing, look at the [Bite-size](https://github.com/MovingBlocks/TerasologyLauncher/labels/Bite-size) issues.

Commits and Commit Messages
---------------------------
Follow these guidelines when creating public commits and writing commit messages.

1. If your work spans multiple local commits make sure they can be merged directly into the target branch. If you have some commit and merge noise in the history have a look at [git-rebase](https://git-scm.com/docs/git-rebase). Ideally, every commit should be able to be used in isolation -- that is, each commit must build and pass all tests.
1. The first line of the commit message should be a descriptive sentence about what the commit is doing. It should be possible to fully understand what the commit does by just reading this single line. 
1. **Optional:** Please try to use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/)

License
-------
Submissions must be licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

If you are adding a new file it should have a header like this (automatically available as a template in IntelliJ):
~~~
/*
 * Copyright 2016 MovingBlocks
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

How to Submit a Pull Request
----------------------------
Pull requests are highly appreciated! Please follow the simple guidelines below. 

1. Fork the repository to your personal GitHub account.
1. Create a topic branch for every separate change you make. The branch should have a short but explanatory name, such as "makes-grass-grow".
1. Apply your changes, committing at logical breaks. Make sure your changes are well-tested. 
1. Check any unit tests that cover the affected code and if possible include new ones for new code.
1. Update the [README](/README.md) for noteworthy changes, but please __do not change__ the version number.
1. Push your branch to your personal account and [create a pull request](https://help.github.com/articles/using-pull-requests/). 
1. **Optional:** Add `Fix/Fixing/Fixes/Close/Closing/Refs #issue` to the PR description if you want to mark the issue mentioned as complete in the issue tracker when the change is merged.
1. Watch for comments or acceptance on your PR. The PR can be updated by just pushing to the original branch.

__Please note:__ if you want to change multiple things that don't depend on each other, make sure you create a new branch off the `master` branch before making more changes - this way we can take in each change separately.

How to Merge a Pull Request
---------------------------
For trusted contributors with push access to our root repos you are welcome to help merge pull requests.

1. Consider what scope is suitable for the change being PRed.
  * **Trivial**: If it is a single typo fix in a comment or other *inactive text* it does not require further review.
  * **Patch**: Small bug fixes. Be careful: even a string value changed can have unexpected impact elsewhere if it gets used as a key somewhere, involved in our internationalization, etc. Consider any possible integrations (like our translation server).
  * **Minor**: If substantial code is being changed/added please review as able and ping relevant contributors with `@username` to bring their attention to the PR for review. Probably one additional person should review. 
  * **Major**: If any involved code is likely to cause a backwards incompatibility of some sort, the PR needs the most possible review as it would require a major release. Consider if it is possible to deprecate the old version of the feature and provide a bridge to the new version to restore backwards compatibility. Alternatively should the change be parked in a longer term branch waiting for the next major release?
1. React appropriately to the scope and perform local tests accordingly.
1. Report any issues or concerns as comments in the PR and ping appropriate staff or other contributors with a `@username` mention.
1. Assign the PR and any completed issues to an appropriate milestone if available for later release handling.
1. If sufficient review for the scope of PR has been done and no issues have been noted, perform a squash-merge (please try to use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/)) to GitHub to auto-close the PR as merged.

Related documentation
---------------------
- [How to contribute translations](Add-New-Translation.md)
