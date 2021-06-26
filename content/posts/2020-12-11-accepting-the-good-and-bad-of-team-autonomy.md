---
title: Accepting the good and bad of team autonomy
publishdate: 2020-12-11
tags: [devops]
canonicalUrl: https://polarsquad.com/blog/accepting-the-good-and-bad-of-team-autonomy
---

One of the key ideas of the DevOps culture is to bring autonomy to teams. Teams are responsible for what they produce from start to finish, but they also have the freedom to decide how they work. When autonomous, teams are better equipped to deliver features and fixes on their own rather than having to coordinate changes with other teams, which reduces the time to market and lead time between fixes.

In practice, when DevOps is adopted in an organisation, the responsibilities in the traditional development and operations teams are shifted. The development teams adopt more responsibilities in maintaining their software in production, while the operations teams provide the capabilities to support the dev teams. Personnel might be shifted between the teams to better support the transition or the teams might be renamed entirely (e.g. the ops team is renamed to platform team). There are many ways to go about it, and there’s no one size that fits all.

Like any large change, the shift to DevOps can cause a lot of headaches. In this article, I’ll present what I’ve found to usually go wrong when teams become more autonomous. I’ll also talk about what can be done to remedy the situation.

<!-- more -->

## Changing the mindset

> "The problem isn’t change, per se, because change is going to happen; the problem, rather, is the inability to cope with change when it comes."
>
> — Kent Beck, Extreme Programming Explained: Embrace Change

One of the challenges during a transition to DevOps is to learn a new mindset for how production support is done. A lot of times, the developers need to learn how to run their software in production, deploy it, and respond to any issues that may appear. Along the way, they may also learn techniques that help them create more resilient and maintainable software. While developers are required to pick up new skills, the operations engineers also need to adapt as well.

As an ops person, you often want an application to run the best possible way. A non-optimal solution may be inefficient, less secure, and harder to operate. Therefore, when a development team comes up with a non-optimal solution for their problem, it may be tempting to just say "no" and direct them to a more optimal solution instead.

For example, a development team may wish to run arbitrary commands on their application Pods in Kubernetes to debug their software when the more appropriate method would be to examine logs, metrics, and other diagnostic data. After all, diagnostic data can be queried in rich and unique ways, and it doesn’t require access to the software that can also break it. As the maintainer of the Kubernetes cluster, it may be tempting to just block access to executing commands and instruct the team to use the centralized logging and metrics platforms instead.

The problem with saying "no" in these situations is that it can lead to micro-management. It’s very easy to get tangled in the minutiae when the ops team should be driving broader concepts forward. Of course, you want everyone to use the best solutions out there, but sometimes that just isn’t the ops team’s decision to make. Since the dev teams are responsible for what they do, that will also include taking responsibility for the non-optimal solutions they come up with as well.

## Principle of least privilege

The issue of enforcing best practices is similar to enforcing permissions.

In information security, it’s commonly agreed that you should follow the [principle of least privilege](https://en.wikipedia.org/wiki/Principle_of_least_privilege): grant users and processes only the privileges they require to function and nothing more. Whenever a new use-case appears, its privilege requirements are evaluated, and new privileges are granted by the privileged authority. Traditionally, a central operation team acts as the authority that grants the privileges.

While I agree that the principle of least privilege is generally a great idea, I’ve usually seen it applied in a way that is counter-productive to autonomous teams. A lot of times when a team needs to add a new component or a feature to their product, they also need to grant new access to it as well. However, with the central authority still around, this means that the team is blocked until a new access is granted. In other words, the team can’t act autonomously.

> "Faced with the pressure to move fast and compete on one side, and the weight of old processes on the other, it’s not surprising that respondents from operationally mature companies report it’s common to evade change management procedures. Many changes get rubber-stamped, and teams regularly bypass their change management procedures without consequences."
>
> — State of DevOps report 2020, Puppet Labs

For organisations where the teams rarely need new access, having a central authority for permissions might not be that bad of a blocker for teams, but it can escalate into a problem when the number of teams and services scale up. If more permission change requests are made than what the central authority can handle, the requests start to queue up. It’s also typical for a permission request to be immediately be followed up another request because the team has not figured out all the permission requirements for the first request. This means that it takes longer for teams to deliver changes, which will create frustration between the team and the central authority.

If the responses to the permission requests become too slow, teams will eventually start to find ways to work around it. They might re-use access from previous components, use some user’s credentials, or find another way to bypass the permission boundaries. In other words, a shadow system is created in parallel to the existing one.

The central authority team is also pressured to deliver more permission changes. With the number of use-cases growing, it becomes increasingly difficult for the team to keep track of what permissions are truly required and what can be left out, and thus hampers the team’s ability to make rational decisions for granting the permissions. When other teams start to see the central authority as a blocker, it becomes increasingly tempting for them to just grant whatever is being requested, which eliminates the purpose of the entire process.

## Bulkheading

So how do you decide what practices and permissions to restrict and what to allow? If you allow only the bare minimum, you’ll end up blocking and frustrating the teams. If you allow too much, you risk the stability of your infrastructure.

I’ve found bulkheading to be a pretty good answer for this: If the solutions the team came up with can’t interfere with other teams’ work, they may continue with it. This way you can isolate any issues arising from the non-optimal solutions to the teams’ silos, while still giving them the freedom to built the solution as they see fit. This approach pushes you to come up with ways to make safety boundaries between teams rather than between a team and the solution they need.

For example, if you can prevent teams from running commands on  other teams’ Kubernetes Pods, then you might as well give them the access they need. This could be achieved by limiting the teams’ access to dedicated namespaces or even dedicated clusters.

Moreover, when each team has its silo to work with, they can be granted access to grant further permissions to services and users within the silo. This allows the team to still follow the principle of least privilege within the silo. For example, if a team has full-access to a Kubernetes namespace, the team can create service accounts and roles within the namespace to grant services access to their silo.

Rather than spending time on granting and blocking access to features and permissions, the time could spend promoting better practices instead. The teams can then adopt the new practices at their own pace. The non-optimal solutions can also be a great learning opportunity: as the teams and their products grow, they may realise that the solution they came up with may need a better alternative.

## Conclusions

In this article, I talked about how operations teams need to accept both good and bad decisions that development teams make, and how the principle of least privilege can get into the way. I also presented an idea for how to set up boundaries for teams, so that they can act autonomously without risking the stability of the entire production system.

What do you think? How would you ensure teams can remain autonomous while promoting good operational practices?
