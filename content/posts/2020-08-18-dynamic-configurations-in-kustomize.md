---
title: Loading dynamic configurations in Kubernetes Kustomize
publishdate: 2020-08-18
tags: [kubernetes, kustomize]
canonicalUrl: https://polarsquad.com/blog/loading-dynamic-configurations-in-kubernetes-kustomize
---

[Kustomize for Kubernetes](https://kustomize.io/) is cool! It provides a structured approach to generating Kubernetes resource manifests instead of relying on text-based templating. This eliminates a whole class of bugs: syntax errors coming from generated templates. It’s also included as part of `kubectl` since version 1.14, so as long as you have been keeping your Kubernetes up-to-date, you can enjoy its benefits without installing additional tools.

While learning Kustomize, I was surprised how [committed Kustomize was to loading everything from files](https://kubernetes-sigs.github.io/kustomize/faq/eschewedfeatures/#build-time-side-effects-from-cli-args-or-env-variables). Kustomize will build the manifests from files exclusively, and no information would come from runtime. In essence, no information can be loaded from the command-line arguments or environment variables during the manifest build phase. This is done to support the practice of storing all of the configurations in a version control system like Git.

However, there are times when it’s useful to be able to load configurations dynamically in Kustomize. In this article, I’ll present a use-case for dynamic configurations and methods for loading them.

<!-- more -->

## A use-case for dynamic configurations

A lot of times, I work with projects that use push-based Continuous Deployment with Kubernetes. Once a pull request is accepted, it’s merged to master, and a pipeline job is triggered for it automatically. Here’s what typically happens in the pipeline:

1. A Docker image is built for the project.
2. The image is tagged with the Git commit hash and pushed to a registry.
3. For each environment, the project’s Kubernetes resources are updated from the manifests (or manifest templates) in the repository with the image tag pointing to the new Docker image tag.

The Git commit hash provides a simple, convenient, and effective versioning strategy. It’s unique enough to distinguish each version from each other, and we can easily trace each version back to the source code.

Most importantly, the Git commit hash is something we can derive from the source repository. Once changes have been accepted, the version number is auto-generated without any additional manual steps. Even though changing a version number is a relatively small task compared to the rest of the development process, it adds up quickly in projects where there are frequent changes. I’ve found this to be an important factor in promoting rapid releases in teams.

The trade-off for using the hashes as versions is that they lack semantics for ordering and compatibility. In other words, you can’t tell which version comes after which just by looking at the version strings, and you can’t tell whether the changes between two versions broke compatibility or not. However, I’ve found these qualities to be something I can work without in practice when working with service-based software development.

From Kustomize’s perspective, the hashes from Git repositories work like dynamic configurations. While they’re read from the Git repository, they’re not stored in a format that Kustomize can use directly. Typically, we read the hash either from environment variables provided by the CI platform (e.g. `CI_COMMIT_SHA` in GitLab) or using the Git CLI tool (e.g. `git rev-parse HEAD`). Therefore, we need to build a custom solution for feeding the Git commit hash to the Kustomize via files.

## Example project

Before we can try loading dynamic configurations in Kustomize, we need a project to try it out on. For this purpose, I’ve created a [Git repository](https://gitlab.com/jkpl/kustomize-demo) that contains the following components:

* a “Hello world” HTTP service written in Go
* a Dockerfile for building a Docker image for the service
* a Kustomize project for deploying the service
* a GitLab CI pipeline for building the Docker image and publishing it

Additionally, there’s a step in the pipeline to generate the final Kubernetes manifests that can be deployed to Kubernetes using `kubectl`. However, since this is only a demo, the actual deployment part is left out as an exercise for the reader.

First, let’s have a look at the Kustomize project. To keep the demo simple, we’ll only create a Service and a Deployment for the app in the `k8s-base` directory. Here’s what the Service looks like.

```yaml
# path: k8s-base/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp
spec:
  type: ClusterIP
  ports:
  - port: 80
    protocol: TCP
    targetPort: 8080
  selector:
    app: myapp
```

The Deployment manifest uses `myapp` as the Docker image, which we can later point to the actual Docker image using the [`images`](https://kubectl.docs.kubernetes.io/references/kustomize/images/) field.

```yaml
# path: k8s-base/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  progressDeadlineSeconds: 30
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: myapp
        image: myapp
        ports:
        - name: http
          containerPort: 8080
        readinessProbe:
          httpGet:
            path: /
            port: 8080
```

These are then bound to the Kustomization resource, which also sets some of the common settings such as labels. It’s placed next to the manifests in the file named `kustomization.yaml`.

```yaml
# path: k8s-base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
- service.yaml
- deployment.yaml
namespace: default
commonLabels:
  app: myapp
```

With the files in place, we should be able to generate all the manifests using command `kubectl kustomize k8s-base`.

## Templating configurations in place

Now that we have a Kustomize project to work with, we need a way to feed the extra configurations to it via files.

One of the classic ways to inject configurations to files is to use a search and replace tools such as `sed`. For example, we could include placeholders in the `kustomization.yaml` file, but then replace the placeholders with the actual configurations. Let’s try this out by adding a placeholder for the Docker image tag in the images field.

```yaml
# path: k8s-base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
- service.yaml
- deployment.yaml
namespace: default
commonLabels:
  app: myapp
images:
- name: myapp
  newName: registry.gitlab.com/jkpl/kustomize-demo
  newTag: IMAGE_TAG
```

We can now replace the placeholder `IMAGE_TAG` using `sed` with the content from the Git commit hash. The `-i` flag in `sed` means that the contents will be replaced in the file itself.


```bash
IMAGE_TAG=$(git rev-parse HEAD)
sed -i "s/IMAGE_TAG/${IMAGE_TAG}/g" k8s-base/kustomization.yaml
```

If you look at the `kustomization.yaml` file, you should see the Git commit hash where the placeholder was.

The above script works when it’s run only once but it doesn’t work if you try to run it again with a different Git commit. This is because the placeholder no longer exists in the file.

We can get around this limitation by creating a dedicated template file that contains the placeholders.

```yaml
# path: k8s-base/kustomization.template.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
- service.yaml
- deployment.yaml
namespace: default
commonLabels:
  app: myapp
images:
- name: myapp
  newName: registry.gitlab.com/jkpl/kustomize-demo
  newTag: IMAGE_TAG
```

We can now safely generate the final `kustomization.yaml` file as many times as we like from the template by forwarding the `sed` output to a separate file instead of replacing the contents in place.

```bash
IMAGE_TAG=$(git rev-parse HEAD)
sed "s/IMAGE_TAG/${IMAGE_TAG}/g" \
    k8s-base/kustomization.template.yaml \
    > k8s-base/kustomization.yaml
```

Since we’re feeding the data via environment variables, we could use a templating tool that’s specifically made for that purpose: [`envsubst`](https://linux.die.net/man/1/envsubst). It substitutes any environment variables (i.e. strings in format `$VARIABLE` or `${VARIABLE}`) in the given template with the contents for those environment variables. The `envsubst` command is typically provided via [GNU gettext](https://www.gnu.org/software/gettext/).

Let’s try it out by creating a new template file. Pay attention to how the placeholder will now use the environment variable syntax.

```yaml
# path: k8s-base/kustomization.template.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
- service.yaml
- deployment.yaml
namespace: default
commonLabels:
  app: myapp
images:
- name: myapp
  newName: registry.gitlab.com/jkpl/kustomize-demo
  newTag: ${IMAGE_TAG}
```

We can provide this template to the `envsubst` command. Note that all of the shell variables we want to use in the template must be exported as environment variables.

```bash
export IMAGE_TAG=$(git rev-parse HEAD)
envsubst \
    < k8s-base/kustomization.template.yaml \
    > k8s-base/kustomization.yaml
```

Again, if you look at the `kustomization.yaml` file, you should see the Git commit hash where the `${IMAGE_TAG}` was located in the template.

## A temporary Kustomize project

Kustomize provides a mechanism to [use an existing Kustomize project as a base project to extend on](https://kubectl.docs.kubernetes.io/guides/config_management/bespoke/). It’s generally intended for customising deployments per target environment.

By using the bases feature, we can host our dynamic configurations in a temporary project that extends the manifests in the Git repository. We can automate this using a shell script.

We can automate this using a shell script.

```bash
#!/usr/bin/env bash
IMAGE_TAG=$(git rev-parse HEAD)

TEMPDIR=$(mktemp -d tmp.k8s.XXXXX)
delete_temp_dir() {
    if [ -d "${TEMPDIR}" ]; then
        rm -r "${TEMPDIR}"
    fi
}
trap delete_temp_dir EXIT

cat <<EOF > "${TEMPDIR}/kustomization.yaml"
bases:
- ../k8s-base
images:
- name: myapp
  newName: registry.gitlab.com/jkpl/kustomize-demo
  newTag: "${IMAGE_TAG}"
EOF

kubectl kustomize "${TEMPDIR}"
```

Let’s go through the script step by step by starting with reading the Git commit hash as we’ve done before.

```bash
IMAGE_TAG=$(git rev-parse HEAD)
```

Next, we’ll create a temporary directory next to the `k8s-base` directory to host our dynamic configurations and set it up for deletion after the script ends.

```bash
TEMPDIR=$(mktemp -d tmp.k8s.XXXXX)
delete_temp_dir() {
    if [ -d "${TEMPDIR}" ]; then
        rm -r "${TEMPDIR}"
    fi
}
trap delete_temp_dir EXIT
```

The `mktemp` command creates a directory with a random name, so we can call this script multiple times simultaneously without race conditions. The `trap` command allows us to schedule commands to be run every time a script exits. It runs even when the script ends with an error. In this case, we use trap to clean up the temporary directory.

We’ll use `cat` to generate the `kustomization.yaml` file for the temporary Kustomize project. It’s set to use the `k8s-base` directory as the base project and configures the image tag to use the Git commit hash we read earlier.

```bash
cat <<EOF > "${TEMPDIR}/kustomization.yaml"
bases:
- ../k8s-base
images:
- name: myapp
  newName: registry.gitlab.com/jkpl/kustomize-demo
  newTag: "${IMAGE_TAG}"
EOF
```

Finally, we’ll generate the manifests from the temporary project using the Kustomize command in `kubectl`. The manifests can either piped to a file or directly to the `kubectl apply -f -` command to deploy the resources in Kubernetes.

```bash
kubectl kustomize "${TEMPDIR}"
```

With this approach, we can keep the templating part separate from the Kustomize project stored in the Git repository. The script still does do templating, but it’s limited to the temporary project. In larger projects, this can be easier to reason with because you know where all templating is done.

## Kustomize CLI tool

In addition to the Kustomize command integrated into `kubectl`, there’s also a dedicated Kustomize CLI tool available that can be [installed separately](https://kubernetes-sigs.github.io/kustomize/installation/). The CLI tool is [more up-to-date compared to the kubectl built-in command](https://kubernetes-sigs.github.io/kustomize/faq/#kubectl-doesnt-have-the-latest-kustomize-when-will-it-be-updated), which means that it includes bug-fixes and additional features not present in the built-in command.

The CLI tool includes a command for editing Kustomize projects from the command line, which we can use to feed in our dynamic configurations. Specifically, we can use the `edit set image` sub-command to set our image tag in the `k8s-base` directory.

```bash
cd k8s-base
IMAGE_TAG=$(git rev-parse HEAD)
kustomize edit set image \
    "myapp=registry.gitlab.com/jkpl/kustomize-demo:${IMAGE_TAG}"
```

Like in the first `sed` example, this command will update the `kustomization.yaml` file in place. However, unlike in the `sed` example, we can repeat the command as many times as we like.

We can also combine the Kustomize CLI tool with the temporary project approach by replacing the `cat` command with calls to the CLI tool.

```bash
#!/usr/bin/env bash
IMAGE_TAG=$(git rev-parse HEAD)TEMPDIR=$(mktemp -d tmp.k8s.XXXXX)
delete_temp_dir() {
    if [ -d "${TEMPDIR}" ]; then
        rm -r "${TEMPDIR}"
    fi
}
trap delete_temp_dir EXIT(
    cd "${TEMPDIR}"
    kustomize create --resources ../k8s-base
    kustomize edit set image \
        "myapp=registry.gitlab.com/jkpl/kustomize-demo:${IMAGE_TAG}"
    kustomize build
)
```

Like earlier, we create a new temporary directory to host the temporary project. In that directory, we create a new project based on the `k8s-base` directory using the `kustomize create` command and add the image configuration. Finally, we use `kustomize build` to generate the Kubernetes manifests. All of these commands are run in a sub-shell to ensure the temporary directory can be deleted in the end.

## Conclusions

In this article, I presented a use-case for when dynamic configurations can be really useful, and how we can load them in Kustomize via templating, temporary projects, and the Kustomize CLI tool.

As mentioned in the article, [the code for the demo is available in GitLab](https://gitlab.com/jkpl/kustomize-demo/). Thanks for reading!
