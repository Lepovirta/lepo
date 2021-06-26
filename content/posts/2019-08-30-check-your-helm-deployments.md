---
title: Check your Helm deployments!
publishdate: 2019-08-30
tags: [kubernetes, helm]
canonicalUrl: https://polarsquad.com/blog/check-your-helm-deployments
---

The Deployment resource is the de-facto way to handle application deployments in Kubernetes, but there are many tools to manage them. One way to manage them safely is to use kubectl directly as demonstrated in [my previous article](2019-04-24-check-your-kubernetes-deployments.html).

Another popular way to deploy resources to Kubernetes is to use [Helm, a package manager for Kubernetes](https://helm.sh/). In this article, I’ll talk about how to repeat the deployment pattern demonstrated in the previous post using Helm. We’ll be using Helm version 2.14.2 for the demonstration.

<!--more-->

## Example Helm chart

In Helm, Kubernetes resources are distributed as [charts](https://v2.helm.sh/docs/developing_charts/#charts): a collection of templated Kubernetes resources in YAML or JSON format. The charts can be deployed from an external Helm repository, a chart archive file, or a local chart directory. Each chart has their own set of variables that can be used for customizing the deployment. Let’s generate a Helm chart to a local directory that we can use for testing failing and successful deployments.

```
$ helm create demo
```

This creates a simple chart in the directory `demo/`, which contains a deployment for a web server. The template in path `demo/templates/deployment.yaml` generates the deployment manifest. Let’s parametrize the readiness probe, so that we can simulate a failing deployment by changing a Helm chart parameter.

``` yaml
readinessProbe:
  httpGet:
    path: {{ .Values.readinessPath | default "/" }}
    port: http
```

## Unchecked deployment

There are two ways to install Helm charts using the Helm CLI: [`helm install`](https://v2.helm.sh/docs/helm/#helm-install) and [`helm upgrade --install`](https://v2.helm.sh/docs/helm/#helm-upgrade). The install sub-command always installs a brand new chart, while the upgrade sub-command can upgrade an existing chart and install a new one, if the chart hasn’t been installed before. With the upgrade feature, we can use a single command for installs and upgrades, which is handy for automation. Let’s use it to install the demo Helm chart we created earlier.

```
$ helm upgrade --install demo demo/
Release "demo" does not exist. Installing it now.
NAME:   demo
LAST DEPLOYED: Fri Aug 16 13:48:06 2019
NAMESPACE: default
STATUS: DEPLOYED

RESOURCES:
==> v1/Deployment
NAME  READY  UP-TO-DATE  AVAILABLE   AGE
demo  0/1    1           0           1s

==> v1/Pod(related)
NAME                     READY  STATUS               RESTARTS  AGE
demo-69c7467798-84nr9    0/1    ContainerCreating    0         1s

==> v1/Service
NAME  TYPE        CLUSTER-IP    EXTERNAL-IP  PORT(S)  AGE
demo  ClusterIP   10.96.196.73  <none>       80/TCP   1s
```

As we can see from the output, the Helm chart was installed, but the deployment is still in progress. Helm didn’t check that our deployment finished successfully.

When we create a failing deployment, we should see the same result. Let’s break the deployment on purpose by changing the path of the readiness probe to something that we know doesn’t work.

```
$ helm upgrade --install --set readinessPath=/fail demo demo/
Release "demo" does not exist. Installing it now.
NAME:   demo
LAST DEPLOYED: Fri Aug 16 13:53:26 2019
NAMESPACE: default
STATUS: DEPLOYED

RESOURCES:
==> v1/Deployment
NAME  READY  UP-TO-DATE  AVAILABLE   AGE
demo  0/1    1           0           5m

==> v1/Pod(related)
NAME                   READY  STATUS               RESTARTS  AGE
demo-54df8f97bb-ffp4b  0/1    ContainerCreating    0          0s
demo-69c7467798-84nr9  1/1    Running              0          5m
==> v1/Service
NAME  TYPE        CLUSTER-IP    EXTERNAL-IP  PORT(S)  AGE
demo  ClusterIP   10.96.196.73  <none>       80/TCP   5m
```

The output shows that the chart was "deployed", but the updated Pod wasn’t launched successfully. We can verify that the deployment didn’t finish successfully by viewing the deployment rollout status.

```
$ kubectl rollout status deployment demo
Waiting for deployment "demo" rollout to finish: 1 old replicas are pending termination...
```

However, the chart deployment [history](https://v2.helm.sh/docs/helm/#helm-history) will show that the first deployment was superseded by the second one.

```
$ helm history demo
REVISION  STATUS      DESCRIPTION
1         SUPERSEDED  Install complete
2         DEPLOYED    Upgrade complete
```

Let’s delete the chart, and start fresh.

```
$ helm delete --purge demo
```

## Wait and timeout

It seems that the Helm chart deployments work similar to how `kubectl apply` works: the resources are created, but the actual deployment is not verified. With kubectl, we can use `kubectl rollout status` to further check the status of the deployment. So what would be the Helm equivalent in this case?

Helm install and upgrade commands include two CLI options to assist in checking the deployments: `--wait` and `--timeout`. When using `--wait`, Helm will wait until a minimum expected number of Pods in the deployment are launched before marking the release as successful. Helm will wait as long as what is set with `--timeout`. By default, the timeout is set to 300 seconds. Let’s try it out\!

```
$ helm upgrade --install --wait --timeout 20 demo demo/
Release "demo" does not exist. Installing it now.
NAME:   demo
LAST DEPLOYED: Fri Aug 16 15:47:10 2019
NAMESPACE: default
STATUS: DEPLOYED

RESOURCES:
==> v1/Deployment
NAME  READY  UP-TO-DATE  AVAILABLE  AGE
demo  1/1    1           1          8s

==> v1/Pod(related)
NAME                   READY  STATUS   RESTARTS  AGE
demo-69c7467798-4tkqf  1/1    Running  0         8s

==> v1/Service
NAME  TYPE       CLUSTER-IP      EXTERNAL-IP  PORT(S)  AGE
demo  ClusterIP  10.102.127.162  <none>       80/TCP   8s
```

The deployment finished successfully as expected. Let’s see what happens when we try this with a failing deployment.

```
$ helm upgrade --install --wait --timeout 20 --set readinessPath=/fail demo demo/
UPGRADE FAILED
Error: timed out waiting for the condition
Error: UPGRADE FAILED: timed out waiting for the condition
```

Very nice\! We finally got feedback from a failing deployment. We can also see this in the Helm chart history.

```
$ helm history demo
REVISION  STATUS     DESCRIPTION
1         DEPLOYED   Install complete
2         FAILED     Upgrade "demo" failed: timed out waiting for the condition
```

## Manual rollbacks

Now that we have a failed upgrade, you might think that you can just deploy the previous version of the chart and be done with it. Unfortunately, that doesn’t get you back to a working version.

```
$ helm upgrade --install --wait --timeout 20 demo demo/
UPGRADE FAILED
Error: timed out waiting for the condition
Error: UPGRADE FAILED: timed out waiting for the condition
$ helm history demo
REVISION  STATUS     DESCRIPTION
1         DEPLOYED   Install complete
2         FAILED     Upgrade "demo" failed: timed out waiting for the condition
3         FAILED     Upgrade "demo" failed: timed out waiting for the condition
```

I’m not sure why this is the case, but it gets **worse**\! If you try to issue another update for your chart, it will fail as well.

```
$ helm upgrade --install --wait --timeout 20 --set replicaCount=2 demo demo/
UPGRADE FAILED
Error: timed out waiting for the condition
Error: UPGRADE FAILED: timed out waiting for the condition
```

The only way I can think of getting out of this situation is to delete the chart deployment entirely and start fresh.

```
$ helm delete --purge demo
```

Instead of trying to use the upgrade command, we can use [`helm rollback`](https://v2.helm.sh/docs/helm/#helm-rollback). It’s specifically designed for rolling out a version of a chart you’ve deployed before. To use the rollback sub-command, we need to provide it the revision to roll back to. It also accepts the same wait and timeout options as install and upgrade, which we can use to verify that the rollback itself is successful. Note that rollback can’t be used for recovering from the situation mentioned above. Let’s roll back to the first revision.

```
$ helm upgrade --install --wait --timeout 20 demo demo/
$ helm upgrade --install --wait --timeout 20 --set readinessPath=/fail demo demo/
$ helm rollback --wait --timeout 20 demo 1
Rollback was a success.
```

Awesome\! Again, this should be visible in the Helm chart history as well.

```
$ helm history demo
REVISION  STATUS       DESCRIPTION
1         SUPERSEDED   Install complete
2         SUPERSEDED   Upgrade "demo" failed: timed out waiting for the condition
3         DEPLOYED     Rollback to 1
```

## Automated rollbacks with atomic

Automating deployment and rollback this way is a bit cumbersome because you need to figure out how to parse the last successful revision from the Helm history, so that you can issue a rollback. Using the `-o json` option with the history command, you can get the history in JSON format, which should help. However, there is a shortcut to avoid all that.

Helm install and upgrade commands include an `--atomic` CLI option, which will cause the chart deployment to automatically rollback when it fails. Enabling the atomic option will automatically enable wait. Let’s try it\!

```
$ helm upgrade --install --atomic --timeout 20 --set readinessPath=/fail demo demo/
UPGRADE FAILED
Error: timed out waiting for the condition
ROLLING BACKRollback was a success.
Error: UPGRADE FAILED: timed out waiting for the condition
$ helm history demo
REVISION  STATUS       DESCRIPTION
1         SUPERSEDED   Install complete
2         SUPERSEDED   Upgrade "demo" failed: timed out waiting for the condition
3         SUPERSEDED   Rollback to 1
4         SUPERSEDED   Upgrade "demo" failed: timed out waiting for the condition
5         DEPLOYED     Rollback to 3
```

Perfect\! This will also work then failing to install the chart the first time. If there’s no revision to revert to, the chart deployment will be deleted.

```
$ helm delete --purge demo
release "demo" deleted
$ helm upgrade --install --atomic --timeout 20 --set readinessPath=/fail demo demo/
Release "demo" does not exist. Installing it now.
INSTALL FAILED
PURGING CHART
Error: release demo failed: timed out waiting for the condition
Successfully purged a chart!
Error: release demo failed: timed out waiting for the condition
$ helm history demo
Error: release: "demo" not found
```

## Awkwardness in Helm

The fact that Helm supports checking deployments and automated rollbacks out of the box is awesome, but it has a couple caveats compared to traditional kubectl based deployments.

First, there’s no official command to wait for a deployment to finish that’s separate from the install and upgrade procedure similar to `kubectl rollout status`. This feature would be useful to have in situations where you suspect a deployment on the same Helm chart might be ongoing: you could wait for the existing deployment to finish before attempting to apply your changes. However, it is possible to work around this caveat by creating a script that continuously polls the status of the chart deployment using the `helm status` sub-command.

Second, the deployment timeout is global across all the resources within the chart. Compare this to the [`progressDeadlineSeconds` feature in Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#progress-deadline-seconds), which allows estimating the timeout per Pod. In Helm, you need to take into account all the pods within a deployment in a single timeout. This makes it much harder to estimate a correct timeout for the chart deployment. If you estimate it too low, you get deployment that fails too early even when it still could make progress. If you estimate it too high, the chart deployment will have to wait a long time to notice that the deployment is not getting anywhere.

## Conclusion

In this article, I’ve demonstrated how to safely deploy Helm charts containing Kubernetes Deployments with automated rollbacks. I’ve also talked about the inherent caveats in Helm’s approach for monitoring deployment healthiness. One area I haven’t covered is how safe deployments are handled with Helm charts that contain resources not based on just the Kubernetes Deployment resource.

Thanks for reading\!
