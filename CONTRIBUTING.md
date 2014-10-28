Terasology Launcher - Contributing
==================================
*If you would like to contribute code you can do so through GitHub by forking the repository and sending a pull request.*

*When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.*

License
-------
Submissions must be licensed under the [Apache License, Version 2.0][Apache License].

If you are adding a new file it should have a header like this:
~~~
/*
 * Copyright 2014 MovingBlocks
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

File an Issue
-------------
You can report bugs and feature requests to [GitHub Issues](https://github.com/MovingBlocks/TerasologyLauncher/issues).

Please don't ask question in the issue tracker, instead use the [forums](forum.terasology.org).

__It's most likely that your bug gets resolved faster if you provide as much information as possible!__

How to Submit a Pull Request
----------------------------
You are welcome to contribute to the project with pull requests. Please follow the simple guidelines below and refer to the [documentation in the wiki](https://github.com/MovingBlocks/TerasologyLauncher/wiki/Documentation). 

1. Fork the repository to your personal GitHub account.
2. Create a topic branch for every separate change you make. The branch should have a short but explanatory name, such as "feature/i18n-de".
3. Apply your changes, committing at logical breaks. Make sure your changes are well-tested. 
4. Update the [README](/README.md) and [CHANGELOG](CHANGELOG.md) for noteworthy changes, but please __do not change__ the version number.
5. Push your branch to your personal account and [create a pull request](https://help.github.com/articles/using-pull-requests/).
6. Watch for comments or acceptance on your PR. The PR can be updated by just pushing to the same branch.

__Please note:__ if you want to change multiple things that don't depend on each other, make sure you check the master branch back out before making more changes - that way we can take in each change seperately.

Commits and Commit Messages
---------------------------
Follow these guidelines when creating public commits and writing commit messages.

1. If your work spans multiple local commits make sure they can be merged directly into the target branch. If you have some commit and merge noise in the history have a look at git-rebase. Ideally, every commit should be able to be used in isolation -- that is, each commit must build and pass all tests.
2. The first line of the commit message should be a descriptive sentence about what the commit is doing. It should be possible to fully understand what the commit does by just reading this single line. 
3. **Optional:** Following the single line description (ideally no more than 70 characters long) should be a blank line followed by a list with the details of the commit.
4. Add keywords for your commit (depending on the degree of automation we reach, the list may change over time):
    - `Review by @githubuser` - will notify the reviewer via GitHub. Everyone is encouraged to give feedback, however. (Remember that @-mentions will result in notifications also when pushing to a WIP branch, so please only include this in your commit message when you're ready for your pull request to be reviewed. Alternatively, you may request a review in the pull request's description.)
    - `Fix/Fixing/Fixes/Close/Closing/Refs #ticket` - if you want to mark the ticket as fixed in the issue tracker. 

For questions please join us in our [forum](forum.terasology.org) or on `#terasology` (irc.freenode.net).

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0"
[SemVer]: http://semver.org/ "SemVer"
