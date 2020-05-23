#!/bin/bash

# This script creates the minimal Kubernetes resources required for using MICO system, without MICO components, redis, and neo4j in the cluster.
echo -e "MICO Development Setup \n----------------------------------------"

# Check if DockerHub credentials are already provided
if [[ -z "${DOCKERHUB_USERNAME}" || -z "${DOCKERHUB_PASSWORD}" ]]; then
    echo "ERROR: One or more environment variables for DockerHub are not specified. Please, specify DOCKERHUB_USERNAME, DOCKERHUB_PASSWORD for accessing DockerHub."
    echo "Docker registry URL can be configured in the application-dev.properties."
    exit 1
fi

export MICO_TEST_NAMESPACE="mico-testing"
echo "Using namespace '${MICO_TEST_NAMESPACE}'"

# Change directory so Kubernetes configurations can be applied with relative path
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $DIR

echo -e "\nCreate Kubernetes resources\n---------------------------"

# Create MICO namespaces
kubectl apply -f mico-namespaces.yaml

mkdir tmp
cp mico-build-bot.yaml tmp/
cd tmp

# Create test namespace
kubectl create namespace ${MICO_TEST_NAMESPACE}

# Create secrets and service accounts for MICO build bot
echo "$(envsubst < mico-build-bot.yaml)" > mico-build-bot.yaml
sed -i -- 's/mico-build-bot/'"${MICO_TEST_NAMESPACE}"'/g' *
kubectl apply -f mico-build-bot.yaml

cd ../
rm -rf tmp/

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
