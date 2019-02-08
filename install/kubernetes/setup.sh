#!/bin/bash

# Read in public IP for MICO, if none is provided don't set the field loadBalancerIP
echo "Please provide a public IP address for MICO. Leave blank if you don't want so set an IP:"
read ip

# Read in DockerHub username
echo "Please provide the base64 encoded user name for DockerHub:"
read uname
export DOCKERHUB_USERNAME_BASE64=$uname

# Read in DockerHub password
echo "Please provide the base64 enoded password for DockerHub:"
read pw
export DOCKERHUB_PASSWORD_BASE64=$pw

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
echo "Change directory to '$DIR'"
cd $DIR

# Create MICO namespaces
kubectl apply -f mico-namespaces.yaml

# Create ClusterRoleBinding for mico-system
kubectl apply -f mico-cluster-admin.yaml

# Prepare MICO build bot namespace
envsubst < mico-build-bot.yaml | kubectl apply -f -

# Install MICO components
kubectl apply -f neo4j.yaml
kubectl apply -f mico-core.yaml
if [ -z "$ip" ]; then
    sed '/${MICO_PUBLIC_IP}/d' mico-admin.yaml | kubectl apply -f -
else
    export MICO_PUBLIC_IP=$ip
    envsubst < mico-admin.yaml | kubectl apply -f -
fi

# Install external components
kubectl apply -f /kube-state-metrics
kubectl apply -f knative-build.yaml
kubectl apply -f monitoring.yaml
