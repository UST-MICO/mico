
[![Build Status](https://travis-ci.org/UST-MICO/mico.svg?branch=master)](https://travis-ci.org/UST-MICO/mico)

# MICO

> A Management System for Microservice Compositions

This is the main repository for the development project MICO at the University of Stuttgart in the masters course Software Engineering.

## Setup MICO
> Note: Currently, MICO is only tested with the Azure Kubernetes Service (AKS).

**Requirements:**
* Kubernetes cluster with at least 8 GB free memory
* `kubectl` ([Install and Set Up kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/))
* DockerHub account (required to store Docker images of services that are build by MICO)

Configure `kubectl` to use the cluster as the current context.

**Setup script:**

The interactive setup script `install/kubernetes/setup.sh` will install all MICO components and its dependencies in your cluster.

Some cloud providers (like Microsoft Azure) offers the possibility to create a *static* public IP address.
If you want to use such a static IP address to access the **MICO dashboard** you can provide it during the execution of the script.
Otherwise your cloud provider will create automatically a IP address for you.

Prepare your DockerHub credentials for the setup script:
```bash
echo -n "username" | base64 -w 0
echo -n "password" | base64 -w 0
```

Execute the script:
```bash
./install/kubernetes/setup.sh
```

After the setup script is finished, the components (especially the Neo4j database) needs some time to be ready (up to 5 minutes).

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

## Documentation

* [USERS](https://mico-docs.readthedocs.io) 
* [CONTRIBUTORS](https://mico-dev.readthedocs.io)
