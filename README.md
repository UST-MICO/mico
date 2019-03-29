[![Build Status](https://travis-ci.org/UST-MICO/mico.svg?branch=master)](https://travis-ci.org/UST-MICO/mico)

# MICO

> A Management System for Microservice Compositions

This is the main repository for the development project MICO at the University of Stuttgart in the masters course Software Engineering.

## Installation

> Note: Currently, MICO is only tested with the Azure Kubernetes Service (AKS). A quick test was also made with the Google Kubernetes Engine (GKE).

**Requirements:**

- Kubernetes cluster with at least 8 GB free memory
- `kubectl` ([Install and Set Up kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/))
- DockerHub account (required to store Docker images of services that are build by MICO)

Configure `kubectl` to use the cluster in the current context.

**Setup script:**

The interactive setup script `install/kubernetes/setup.sh` will install all MICO components and its dependencies in your cluster.

Some cloud providers (like Microsoft Azure) offers the possibility to create a _static_ public IP address.
If you want to use such a static IP address to access the **MICO dashboard** you can provide it during the execution of the script.
Otherwise your cloud provider will create automatically a IP address for it.

Execute the script:

```bash
./install/kubernetes/setup.sh
```

After the script is finished, the components (especially the Neo4j database) needs some time to be ready (up to 5 minutes).

Check the current deployment status of the MICO components until all pods are running:

```bash
kubectl get pods -n mico-system --watch
```

Get the public IP address (or the hostname) of the MICO dashboard:

```bash
kubectl get svc mico-admin -n mico-system -o jsonpath="{.status.loadBalancer.ingress[*]['ip', 'hostname']}"
```

**Clean up:**

```bash
kubectl delete namespace mico-system mico-workspace mico-build-bot \
 && kubectl delete -f install/kubernetes/mico-cluster-admin.yaml \
 && kubectl delete -f install/kubernetes/knative-build.yaml \
 && kubectl delete -f install/kubernetes/monitoring.yaml
```

## Tutorials

- [Tutorial Overview](https://mico-docs.readthedocs.io/en/latest/tutorials/index.html)
- [How To add a service](https://mico-docs.readthedocs.io/en/latest/tutorials/01-add-a-service.html)
- [How To manager a service](https://mico-docs.readthedocs.io/en/latest/tutorials/02-manage-service.html)
- [How To add an application](https://mico-docs.readthedocs.io/en/latest/tutorials/03-add-an-application.html)
- [How to manage an application](https://mico-docs.readthedocs.io/en/latest/tutorials/04-manage-an-application.html)

## Documentation

- [USERS](https://mico-docs.readthedocs.io)
- [CONTRIBUTORS](https://mico-dev.readthedocs.io)

## Update Source File Headers

### mico-core

It is easy to update the license headers of the mico-core project with IntelliJ IDEA.
Create a Copyright Profile in `Settings > Editor > Copyright > Copyright Profiles` with the license from [CONTRIBUTING.md](CONTRIBUTING.md#Source-File-Headers). Set this profile to the default one for the project and use the "Update Copyright..." feature
on the project root.
