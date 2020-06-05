#!/bin/bash

# This script installs MICO with all its dependencies.
echo -e "MICO Setup\n----------"

# Read in public IP for MICO, if none is provided don't set the field loadBalancerIP
if [[ -z "${MICO_PUBLIC_IP}" ]]; then
    echo "Public IP address for MICO Dashboard (optional, leave it blank to get an random IP address from your provider):"
    read MICO_PUBLIC_IP
fi

# Read in public IP for OpenFaaS Portal, if none is provided don't set the field loadBalancerIP
if [[ -z "${OPENFAAS_PORTAL_PUBLIC_IP}" ]]; then
    echo "Public IP address for OpenFaaS Portal (optional, leave it blank to get an random IP address from your provider):"
    read OPENFAAS_PORTAL_PUBLIC_IP
fi

# Check if DockerHub credentials are already provided
if [[ -z "${DOCKERHUB_USERNAME_BASE64}" || -z "${DOCKERHUB_PASSWORD_BASE64}" ]]; then
    # Read in DockerHub username
    echo "DockerHub username:"
    read DOCKERHUB_USERNAME
    if [[ -z "$DOCKERHUB_USERNAME" ]]; then
        echo "ERROR: No username provided"
        exit 1
    fi
    export DOCKERHUB_USERNAME_BASE64=$(echo -n $DOCKERHUB_USERNAME | base64 | tr -d \\n)

    # Read in DockerHub password
    echo "DockerHub password:"
    read -s DOCKERHUB_PASSWORD
    if [[ -z "$DOCKERHUB_PASSWORD" ]]; then
        echo "ERROR: No password provided"
        exit 1
    fi
    export DOCKERHUB_PASSWORD_BASE64=$(echo -n $DOCKERHUB_PASSWORD | base64 | tr -d \\n)
else
    echo "Info: Using DockerHub credentials provided by environment variables."
fi

# Change directory so Kubernetes configurations can be applied with relative path
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $DIR

echo -e "\nCreate Kubernetes resources\n---------------------------"

# Create MICO namespaces
kubectl apply -f mico-namespaces.yaml

# Create ClusterRoleBinding for mico-system
kubectl apply -f mico-cluster-admin.yaml

# Prepare MICO build bot namespace
envsubst < mico-build-bot.yaml | kubectl apply -f -

# Install MICO components
kubectl apply -f neo4j.yaml
kubectl apply -f redis.yaml
kubectl apply -f mico-core.yaml

# Set public IP address for MICO dashboard
if [[ -z "$MICO_PUBLIC_IP" ]]; then
    sed '/${MICO_PUBLIC_IP}/d' mico-admin.yaml | kubectl apply -f -
else
    export MICO_PUBLIC_IP
    envsubst < mico-admin.yaml | kubectl apply -f -
fi

# Install external components
kubectl apply -f ./kube-state-metrics
kubectl apply -f https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml

# Setup Kafka
kubectl apply -k ./kafka/variants/dev-small/

# Setup OpenFaaS
if [[ -z "${OPENFAAS_PORTAL_PASSWORD}" ]]; then
    OPENFAAS_PORTAL_PASSWORD=$(head -c 12 /dev/urandom | shasum| cut -d' ' -f1)
fi

kubectl -n openfaas create secret generic basic-auth \
--from-literal=basic-auth-user=admin \
--from-literal=basic-auth-password="$OPENFAAS_PORTAL_PASSWORD"

kubectl -n monitoring create secret generic basic-auth \
--from-literal=basic-auth-user=admin \
--from-literal=basic-auth-password="$OPENFAAS_PORTAL_PASSWORD"

kubectl apply -f ./monitoring
kubectl apply -f ./openfaas

# Set public IP address for OpenFaaS Portal (only if given by user)
if [[ -z "OPENFAAS_PORTAL_PUBLIC_IP" ]]; then
    sed '/${OPENFAAS_PORTAL_PUBLIC_IP}/d' openfaas/gateway-external-svc.yaml | kubectl apply -f -
else
    export OPENFAAS_PORTAL_PUBLIC_IP
    envsubst < openfaas/gateway-external-svc.yaml | kubectl apply -f -
fi

echo -e "\nOpenFaaS Portal Admin password:"
echo $OPENFAAS_PORTAL_PASSWORD
echo -e "\nScript execution finished!"
