
# Contributing

First off, thanks for taking the time to contribute!
The following is a set of guidelines for contributing to MICO.

When contributing to MICO, please first discuss the change you wish to make via issue, Email, Slack, or any other method with the owners of this project before making a change.

#### Table Of Contents

[Code of Conduct](#code-of-conduct)

[What should I know before I get started](#what-should-i-know-before-i-get-started)

  * [GitHub Flow](#we-use-github-flow-for-development)
  * [Design Decisions](#design-decisions)
  * [Versioning](#versioning)

[How Can I Contribute](#how-can-i-contribute)

  * [Reporting Bugs](#reporting-bugs)
  * [Proposing new Features](#proposing-new-features)
  * [Working on Features and Patches](#working-on-features-and-patches)

[Guidelines](#guidelines)

  * [Git Commit Messages](#git-commit-messages)
  * [Branch Naming Guidelines](#branch-naming-guidelines)
  * [Source File Headers](#source-file-headers)

---

## Code of Conduct

Please note we have a [Code of Conduct](CODE_OF_CONDUCT.md), please follow it in all your interactions with the project to make make contributing to this project as easy and transparent as possible, whether it's:

* Reporting a bug
* Discussing the current state of the code
* Submitting a fix
* Proposing new features
* Becoming a maintainer

By participating, you are expected to uphold this code. Please report unacceptable behavior to [michael.wurster@iaas.uni-stuttgart.de](mailto:michael.wurster@iaas.uni-stuttgart.de).

---

## What should I know before I get started

We use GitHub to host our source code, to track issues and feature requests, as well as accept pull requests.

In short, when you submit code changes, your submissions are understood to be under the corresponding open source license that covers the project.
Feel free to contact the maintainers if that's a concern.
By contributing, you agree that your contributions will be licensed under the corresponding open source license of the respective component.

### We use GitHub Flow for Development

All our component repositories usually consists of a _master_ and several branches.
The _master_ branch always contains a stable version of the component.

Code changes have to be implemented and tested on a **branch** and proposed to be added to the `master` branch through a [pull request](#pull-requests).
The name of your **branch** should closely describe the added changes and comply with our [naming guidelines](#branch-naming-guidelines).

### Design Decisions

When we make a significant decision in how we maintain the project and what we can or cannot support, we will document it as a [MADR](https://adr.github.io/madr/).
The architectural decisions records are maintained under `adr` inside our [docs](https://github.com/UST-MICO/docs) repository.
If you have a question around how we do things, check to see if it is documented there.
If it is *not* documented there, please raise your question in our Slack channel.

### Versioning

This project uses [Semantic Versioning](https://semver.org) or _SemVer_ for short.
If you do not know what SemVer is, a detailed explanation is available on their website.
Any new releases must adhere to this versioning scheme.

---

## How Can I Contribute

### Reporting Bugs

Bugs are tracked as GitHub [issues](https://github.com/UST-MICO/mico/issues).

Before creating new issues, please check the following as you might find out that you don't need to create one:

* **Perform a [cursory search](https://github.com/search?q=+is:issue+user:UST-MICO)** to see if the bug has already been reported.
  If it has, add a comment to the existing issue instead of opening a new one.

Afterwards, provide the following information by filling in the [template](.github/ISSUE_TEMPLATE.md).
Explain the problem and include additional details (configuration and environment) to help maintainers reproduce the problem:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Which version are you using?** Latest build or a released version?
* **Describe the environment you are using MICO**.
* **Describe the exact steps which reproduce the problem** in as many details as possible.
* **Provide specific examples to demonstrate the steps**.
  Include links to files or copy/pasteable snippets, which you use in those examples.
  If you're providing snippets in the issue, use Markdown code blocks.
* **Include log files or exceptions logs** where meaningful and if it's required to understand and reproduce the problem.
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why**.
* **Include screenshots and animated GIFs** which show you following the described steps and clearly demonstrate the problem.
* **If the problem wasn't triggered by a specific action**, describe what you were doing before the problem happened and share more information using the guidelines below.
* **Can you reliably reproduce the issue?** If not, provide details about how often the problem happens and under which conditions it normally happens.

### Proposing new Features

This section guides you through submitting a new feature request, including completely new features and minor improvements to existing functionality.

Before creating feature suggestions, please check the following as you might find out that you don't need to create one:

* **Perform a [cursory search](https://github.com/search?q=+is:issue+user:UST-MICO)** to see if the feature has already been suggested.
  If it has, add a comment to the existing issue instead of opening a new one.

When you are creating a feature request, please [include as many details as possible](#how-do-i-submit-a-good-feature-request).
Fill in the [template](ISSUE_TEMPLATE.md), including the steps that you imagine you would take if the feature you're requesting existed.

#### How Do I Submit A (Good) Feature Request?

Feature requests are tracked as GitHub [issues](https://github.com/UST-MICO/mico/issues).

Create a new issue on this repository and provide the following information:

* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps**.
  Include copy/pasteable snippets which you use in those examples, as [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the current behavior** and **explain which behavior you expected to see instead** and why.
* **Include screenshots and animated GIFs** which help you demonstrate the steps or point out the part the suggestion is related to.
* **Explain why this feature would be useful**.

### Working on Features and Patches

As we use [GitHub Flow](https://guides.github.com/introduction/flow/index.html) for development, code changes have to be implemented and tested on a **branch** and proposed to be added to the `master` branch through a [pull request](#pull-requests).
The name of your **branch** should closely describe the added changes and comply with our [naming guidelines](#branch-naming-guidelines).

> **Hotfixes:** Commits with small, insignificant changes can be done without creating an issue.
> In such cases, it's allowed to directly open a pull request.

#### Pull Requests

Pull requests are the best way to propose changes to our codebase (we use [Github Flow](https://guides.github.com/introduction/flow/index.html)).

* Create your [branch](#branch-naming-guidelines) from `master`
* Fill in [the required template](.github/PULL_REQUEST_TEMPLATE.md)
* Use a clear and descriptive title related to your issue
* Commit your changes with a [meaningful title and description](#git-commit-messages)
* Include screenshots and animated GIFs in your pull request whenever possible
* If you've added code that should be tested, add unit/integration tests
* Make sure your code applies the coding guidelines of the respective component
* Keep your pull request as small as possible (break up your feature into multiple pull requests)
* Create it early, update it often, and sync it regularly with `master`
* Don't clutter your branch history with merge commits, try to [rebase](https://www.atlassian.com/git/tutorials/merging-vs-rebasing): `git pull --rebase origin/upstream master`
* Make use of `git rebase -i master` to structure your commits in meaningful an logical parts

> **Work in Progress:** Use `[WIP]` as title prefix for unfinished work.
> This shows that the pull request is still in progress and not ready to be merged.

#### Reviewing a Pull Request

Before a pull request can be merged, it must fulfill the criteria specified in the [Definition of Done](#definition-of-done).

When a feature is considered finished it is necessary to get a **review**.
To get a review, remove the `[WIP]` prefix and add the `needs review` label.

Pull Requests are usually reviewed by one or two committers, and checked on a regular basis.
If the reviewers submitted their review react to their comments and update your feature.
A pull request is ready to merge if the reviewers approved it.

#### Definition of Done

An issue, feature, or bug-fix is considered done, if **all** of the following elements are checked:

- [ ] Implementation
    - [ ] is completed
    - [ ] works as expected
    - [ ] meets all functional & non-functional requirements
- [ ] Tests are implemented with an appropriate diff coverage
- [ ] Pull request
    - [ ] CI build finalizes successfully
    - [ ] Contains detailed description of your changes
    - [ ] Approved by at least one reviewer
    - [ ] Ensure that the commit messages are [good commit messages](https://chris.beams.io/posts/git-commit/)
- [ ] Contribution guidelines must be followed
    - [ ] Source files are properly formatted
    - [ ] Documentation is updated
    - [ ] License header is applied
- [ ] Design decisions are discussed in an ADR

---

## Guidelines

### Git Commit Messages

> [How to Write a Git Commit Message](https://github.com/joelparkerhenderson/git_commit_message)

#### TL;DR

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood in the subject line ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Capitalize the subject line
* Do not end the subject line with a period
* Separate subject from body with a blank line
* Use the body to explain what and why vs. how
* Reference issues and pull requests liberally after the first line

### Branch Naming Guidelines

We have a naming convention for our branches.
Each branch should start with a `tag` to indicate a category:

* Bugfixes: `fix/issue-<nnn>` or `fix/<short-title>` if closely related to an issue or give it a short concise title
* Features: `feature/issue-<nnn>` or `feature/<short-title>` if closely related to an issue or give it a short concise title
* Documentation: `docs/<short-title>` if the branch covers only the documentation of something
* Tests: `test/<short-title>` if a branch mainly covers the addition or modification of tests or testing related resources
* WIP: `wip/<short-title>` for work in progress without issues or containing something thats not covered in the above categories

### Source File Headers

Each source file should include the following license header.

```plain
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
```
