---
title: How should I answer a health check?
publishdate: 2020-04-22
tags: [distributed-systems]
canonicalUrl: https://medium.com/polarsquad/how-should-i-answer-a-health-check-aa1fcf6e858e
---

In my work, I often meet teams that are not confident with their health checks endpoints. Usually, there’s no health checking implemented at all or there is just enough to pay lip service to the idea. This, of course, makes total sense because there’s often a lot more important components to work on, and there’s little shared knowledge around them beyond the description.

I’ve found that health checks can do wonders for your application’s ability to self-heal, so I thought I’ll talk about what the pattern is and what it’s used for, and how you can approach writing health check code for your application.

<!--more-->

![Health check your loved ones](images/health-check.png)

## What are health checks anyway?

One of the fundamental problems in distributed computing is that it’s really hard to tell whether a remote component is still available or not. It may have crashed or it might be unreachable from the network.

To detect these problems, we can use health checks: Continuously checking whether the remote component is available or not by asking it, and automating actions based on the perceived availability.

There’s many ways to implement health checks: e.g. executing a command successfully on the host, checking that a TCP socket can be opened to the server, checking that we get the right HTTP response from a web server. Probably the most common way to do health checking is by using a HTTP GET check where the response codes between 200-399 indicate a successful check and everything else is interpreted as a failed check.

## How do services use health checks?

Health checks are typically performed by these types of services. There may be other types of services, but these are the most common ones I’ve encountered.

### Service discovery

A service that maintains a knowledge of available services. A service discovery service may choose to broadcast the availability of an application based on the health check responses it receives from the application.

Examples: Consul, AWS Route 53

### Load balancers

A service that distributes incoming traffic between multiple instances of an application. A load balancer may choose to cut traffic to an instance, if the health check fails.

Examples: Traefik, Envoy, AWS ELB

### Container and VM orchestration

A service that runs containers or virtual machines. An orchestration platform may reboot a container/VM, if the container/VM health check fails.

Examples: AWS EC2, Kubernetes, DC/OS

## Types of health checks

Based on the service types, we can categorize health checks based on the **actions** they take.

**Reboot:** When the target is unhealthy, the target should be restarted to recover to a working state. Container and VM orchestration platforms typically perform reboots.

**Cut traffic:** When the target is unhealthy, no traffic should be sent to the target. Service discovery services and load balancers typically cut traffic from targets in one way or another.

The difference between these is that rebooting attempts to actively repair the target, while cutting traffic leaves room for the target to repair itself.

Most services only include one or the other type of health checks, but [Kubernetes has both](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/). This is because Kubernetes includes service discovery and load balancer features on top of being a container orchestration platform.

In Kubernetes, the terminology is slightly different:

  - Health checks are called **probes**.
  - The health check for reboots is called a **liveness probe**: “Check if the container is alive”.
  - The health check for cutting traffic is called a **readiness probe**: “Check if the container is ready to receive traffic”.

Readiness checks are also used during application upgrades to track the upgrade progress. When an app is upgraded, Kubernetes will wait until the readiness check passes for new containers before shutting down old containers. In order to guarantee zero-downtime upgrades for your app in Kubernetes, the readiness endpoint in your app should respond with a healthy status only when it knows it can handle traffic.

Additionally, Kubernetes includes a third probe called the [startup probe](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#define-startup-probes). It’s used as a way to give time for the application to start up properly before the liveness probe is started. It could be seen as one stage of the liveness health check.

## Responding to a health check

Different types of health checks perform different actions, so ideally your app should include separate logic for them. Let’s take a look at how you could respond to both the liveness and readiness checks.

### Liveness check

The liveness check should be very, very simple. It should only respond with a failure when the only way to recover the application is to reboot it. Here’s a few examples of those situations:

  - **The resources were exhausted:** Application has ran out of resources such as memory, and can’t continue further.
  - **The underlying system failed:** The platform (e.g. JVM or Ruby VM) that the app runs on has failed.

In these situations, it’s usually enough to just respond with a healthy response always. If one of the above failure scenarios occurs, it will most likely automatically take out the application and the health check with it. This is especially the case when the liveness health endpoint is integrated as part of the application as an API.

**WARNING\!** If the health check fails when it shouldn’t, it can cause a lot of unnecessary restarts. For this reason, [you may wish to be really conservative when it comes to the liveness check](https://srcco.de/posts/kubernetes-liveness-probes-are-dangerous.html). You could either use a high threshold for the check (high timeout and high number of consecutive failures before the action is triggered) or choose not to use liveness probes at all.

### Readiness check

The readiness check is more complicated compared to the liveness check. It should only respond with a healthy status when the application is reasonably confident it’s ready to consume incoming traffic. Here’s a few example situations your application should keep track of for the liveness check.

**Dependency availability:** If your application has dependencies to external resources such as databases, your application may need to first ensure those are available before it can consider itself ready. For example, if your application requires access to a database to do anything useful, your application should check that it can connect to the database as part of the readiness check.

**Resource saturation:** If your application uses internal resources that can get saturated (e.g. queues, network sockets), your application may need to keep track of those. When one of the resources has run out, the app can signal that it’s no longer ready to receive more traffic. This allows the application to cool down before accepting more traffic.

**Down for maintenance:** If you need to set your application to a maintenance mode, you can signal it through the health check. An example of this could be a scheduled resource garbage collection cycle which may disturb the application’s ability to serve incoming requests.

## Tips

Here’s a collection of general guidelines to keep in mind when implementing health checks for your app.

### Don’t use the same endpoint for different checks

Even though liveness and readiness use the same API style, their semantics are completely different. You can’t infer liveness status from a readiness check or vice versa. Don’t configure liveness and readiness checks to use the same endpoint\! What might happen is that your application gets rebooted when it could very well recover from the situation on its own.

If you’re integrating health checks to an application that only provides a single health endpoint, check that the type of the health check matches the behaviour of the health endpoint before combining them together. For example, if you have an app that provides a readiness type health endpoint, you can attach it to load balancer health check or Kubernetes readiness check, but not a Kubernetes liveness check.

If you want to use both types of health check endpoints for an app, but you only have one, you can do one of the following:

  - If the app is maintained in-house, talk to the team that maintains the app, and work with them on implementing the missing health check.
  - If the app is a 3rd party app, you could create a [sidecar process](https://samirbehara.com/2018/07/23/sidecar-design-pattern-in-your-microservices-ecosystem/) that analyses the app healthiness and reports it to the health check.

### Start small and iterate

It’s OK, if your health checks don’t check everything they should from day one. It’s good to start with something basic and implement more as you go. After all, we’re not in the business of writing health checks. Tailor checks for your app

There are many common ways to implement health checks, but there’s no one true health check for every application. Make sure to tune the health checks for your app to match what you need.

### Do long checks in the background

When a service polls your application’s health endpoint, the response should be fast. This can be difficult to guarantee when you need to check external dependencies.

Instead of checking each dependency when the readiness health endpoint is called, you could instead have the dependencies continuously checked in the background (e.g. in another thread) and stored in memory. Your app can then respond to the readiness check based on the last known dependency statuses. This way, your application health checks remain fast and reasonably accurate.

## Improving reliability

Once you have a really elaborate readiness health check in place, you might feel like your application is constantly going unavailable. Don’t worry, this is expected\! It might not be the fault of the readiness check, but the application structure. Use the opportunity to improve the reliability of the application. Here’s a few tips you might find useful.

### Only track dependencies that are essential

If your application uses multiple dependencies (e.g. databases, other services), you should review which of those are essential for the application to function.

You might find that your app can work around some of the dependencies. For example, your app could serve cached or placeholder data when a dependency is down (i.e. perform [circuit breaking](https://www.martinfowler.com/bliki/CircuitBreaker.html)). This means that you don’t need to check those dependencies in your app. Alternatively, you could serve the data from another service, which means you only need to check that either the original or alternative service is available.

### Split the app, if needed

You might notice that you have multiple essential dependencies, but that the data from those dependencies are never shared within the application. This could indicate that you have two logical applications packaged within one application. You may want to consider splitting the app into two or more apps where each app has their distinct dependencies and health checks.

### Tune thresholds for your dependencies

It’s a good idea to check external dependencies multiple times before deciding that they’re unavailable. This is so that the temporary hiccups such as network latency spikes don’t directly affect the healthiness status of your application.

## Conclusions

In this article, I’ve talked about what health checks are and how they’re used. I categorised the health checks into two types based on the actions associated with them, and described how your app could respond to the different health checks. I listed a few general tips when it comes to writing health check code.

Thanks for reading\!
