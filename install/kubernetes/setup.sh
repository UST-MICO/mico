#!/bin/bash

# Check if all required environment variable are set
err=false

if [[ -z "${MICO_PUBLIC_IP}" ]]; then
    echo "Environment variable MICO_PUBLIC_IP is not set."
    err=true
fi

if [[ -z "${DOCKERHUB_USERNAME_BASE64}" ]]; then
    echo "Environment variable DOCKERHUB_USERNAME_BASE64 is not set."
    err=true
fi

if [[ -z "${DOCKERHUB_PASSWORD_BASE64}" ]]; then
    echo "Environment variable DOCKERHUB_PASSWORD_BASE64 is not set."
    err=true
fi

if [ "$err" = true ]; then
    exit 1
fi

# Create MICO namespaces
kubectl apply -f mico-namespaces.yaml

# Create ClusterRoleBinding for mico-system
kubectl apply -f fabric8-rbac.yaml

# Prepare MICO build bot namespace
envsubst < mico-build-bot.yaml | kubectl apply -f -

# Install MICO components
kubectl apply -f neo4j.yaml
kubectl apply -f mico-core.yaml
envsubst < mico-admin.yaml | kubectl apply -f -

# Install external components
kubectl apply -f /kube-state-metrics
kubectl apply -f knative-build.yaml
kubectl apply -f monitoring.yaml
