
[![Build Status](https://travis-ci.org/UST-MICO/mico.svg?branch=master)](https://travis-ci.org/UST-MICO/mico)

# MICO

> A Management System for Microservice Compositions

This is the main repository for the development project MICO at the University of Stuttgart in the masters course Software Engineering.

## Setup MICO
> Note: Currently, MICO is only tested with the Azure Kubernetes Service (AKS).

To setup MICO you need kubectl and the command-line tool of your cloud provider (for example Microsoft Azure):
* *kubectl*: [Install and Set Up kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
* Azure CLI *az*: [Install the Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)

Now configure the cloud provider CLI to connect to your cluster and configure `kubectl` to use the cluster as the current context. The cluster needs at least 8GB of RAM.

Prepare your DockerHub credentials BASE64 encoded for the setup script:
```bash
echo -n "username" | base64 -w 0
echo -n "password" | base64 -w 0
```

You can now run the interactive setup script `setup.sh` which is stored in the directory `install/kubernetes`. For some cloud providers it is possible to specifiy a static public IP address to enable public access. If your cloud provider does not support this option or you don't want to grant public access, leave the field blank:
```bash
./setup.sh
```

After the setup script finished, the single components need some time to be fully deployed.

## Documentation

* [USERS](https://mico-docs.readthedocs.io) 
* [CONTRIBUTORS](https://mico-dev.readthedocs.io)
