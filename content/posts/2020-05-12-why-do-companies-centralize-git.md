---
title: Why do companies centralize Git?
publishdate: 2020-05-12
tags: [git]
canonicalUrl: https://medium.com/polarsquad/why-do-companies-centralize-git-a068e6c666a3
---

Every now and then, popular Git hosting services such as GitHub, Gitlab, and Bitbucket have outages. Since many organisations, both private and public, rely on these services, a single outage can cause a world wide drop in productivity and a familiar question to pop up.

> If Git is distributed, why do we keep having these outages?

At face value, it’s happening because of the centralization of Git repositories in a handful of providers, but that just raises more questions: Given that Git is distributed, why do we centralize Git repositories?

<!-- more -->

Last month, Yair talked about [how Git is an important part of our toolset](https://medium.com/polarsquad/devops-whats-it-all-about-part-2-tooling-git-the-master-of-version-control-systems-59e976c1881e), so I thought I’d expand on it by taking a look into what it means for Git to be distributed, why companies keep centralising Git, and why it’s generally not thought out to be that big of a deal.

## What does it mean for Git to be distributed?

Before we can understand why Git is being centralized, we must understand the ways in which Git is distributed.

Git is classified as a [distributed version control](https://en.wikipedia.org/wiki/Distributed_version_control) system (DVCS): Your local copy of a Git repository contains the full history of the repository instead of just the latest revision. This means that your copy of the repository can act as a standalone repository.

In Git, you can create changes to the repository offline, and publish them in various ways:

  - Use a popular provider like GitHub, Gitlab, or Bitbucket.
  - Host your own Git service using a tool like Gogs, gitea, gitlist, GitWeb, or Gitlab.
  - Share the repo as a directory sitting on a computer via SSH or HTTPS.
  - Copy the Git directory to a USB drive and throw it around between your pals.

You can also combine these different approaches together: For example, you could have your changes published in multiple providers simultaneously. Alternatively, you can also decide to not publish at all, and hoard the code to yourself.

Additionally, since each copy of a repository can be used as a standalone repository, you automatically have a full backup of the repository on every user’s machine.

With so many hosting options available, surely we shouldn’t have any problems with centralisation, right?

## Beyond source code hosting

You’d be wrong to assume that services like GitHub and Gitlab offer only code hosting and a pretty user interface to browse the code. They also include features such as bug trackers, project management tools, and repositories for software releases.

These features are of course something you can always host elsewhere, but there’s one feature most teams want when they go for a Git host: a process to facilitate contributions. GitHub provides their almighty [pull request](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/about-pull-requests) for this purpose: a workflow where users can share their changes using the platform, request feedback, and integrate the changes. This same workflow is available in other Git hosting platforms as well, and it has become a de facto standard.

The pull request feature is not only useful as a way to collaborate between team mates, but also automation. Bots such as [Dependabot](https://dependabot.com/) automatically create pull requests to keep your source dependencies in sync.

When it comes to building your workflow around Git, you have to pick between convenience and improved availability. The limitation of the pull request model is that it works only within the service. For example, it’s not possible to create a pull request from a GitHub repository to a Gitlab repository or vice versa. This means that, if you were to distribute changes between Git repositories in a decentralized way, you’d have to pick between the pull request model or hosting everything under the same centralised service.

## Building on a single source of truth

Even with all of the cloned repositories out there, you most likely will still have a single source of truth: that one blessed repository that everyone follows, and towards which contributions flow.

Around this repository, you typically have artefacts and effects generated from the source code. For example:

  - Release binaries
  - API documentation pages
  - Deployments to various environments

To automate this, you’d typically have a CI/CD solution to perform these actions when a change is detected in repository branches or tags. While the CI/CD may run automated tests and other verification processes on multiple clones of the same repository, the artefacts and effects are typically generated from from a single repository (the single source of truth).

We limit ourselves to a single central repository because it’s convenient to build around that model. Knowing that all contributions are based on a relatively recent copy of a single repository means that you’re less likely to be dealing with huge merge issues when attempting to synchronize changes from multiple contributors.

The release binaries and deployments follow only one repository because you only need one lineage of releases or deployments. If you were to release/deploy from any clone, you’d have to have a process in place to decide which clone to use, and then ensure that it contains the relevant changes. It’s much easier to decide that all releases/deployments are done from a single repository.

## Do companies care?

Centralising Git repositories is convenient, but it trades off some of the availability. When the service for our repositories goes down, we lose our ability to deliver changes. Since software companies are all about delivering new things, then surely this is a huge issue, right?

In this article, I’ve presented what would need to happen, if you were to move from a hosting solution such as GitHub to a model where Git is truly decentralized. In summary, here’s what you’d have to do:

  - Find multiple alternative solutions for where to host your Git sources.
  - Scrap convenient out-of-the-box features such as pull requests, and perhaps create a replacement solution.
  - Create a process for keeping different sources in sync.
  - Create a process for deciding which of the sources to use for releases/deployments.

That’s a lot of engineering effort, but in theory it should let us achieve better availability than GitHub. But how much better availability would it really be?

Let’s say that we’re hosting all of our code on GitHub. Fortunately for us, GitHub has their [incident records](https://www.githubstatus.com/history) open to the public. According to them, the month with the most outages during the past year has been April 2020. The minimum availability for February was 99.51% with outages spread across 5 days. That’s roughly 3.5 hours of outages for the whole month.

With outages like that the chances are that you didn’t even notice the outage in the first place because you were working with your local copy of the Git repo, you spent the time on doing other business such as meetings or reading Slack, or you were out of the office. Therefore, for a lot of companies, it doesn’t make sense to spend resources on re-engineering Git hosting for higher availability to mitigate issues that will most likely not affect their business.

What do you think? Is decentralization of Git hosting worth the effort for you?
