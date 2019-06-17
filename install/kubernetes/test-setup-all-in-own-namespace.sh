#!/bin/bash

# This script creates a new namespace that includes the complete MICO setup.

# Check if DockerHub credentials are already provided
if [[ -z "${DOCKERHUB_USERNAME_BASE64}" || -z "${DOCKERHUB_PASSWORD_BASE64}" ]]; then
    # Read in DockerHub username
    echo "Please provide the user name for DockerHub:"
    read uname
    if [[ -z "$uname" ]]; then
        echo "ERROR: No username provided"
        exit 1
    fi
    export DOCKERHUB_USERNAME_BASE64=$(echo -n $uname | base64 | tr -d \\n)

    # Read in DockerHub password
    echo "Please provide the password for DockerHub:"
    read -s pw
    if [[ -z "$pw" ]]; then
        echo "ERROR: No password provided"
        exit 1
    fi
    export DOCKERHUB_PASSWORD_BASE64=$(echo -n $pw | base64 | tr -d \\n)
else
    echo "Using DockerHub credentials provided by environment variables."
fi

# Read in name for test namespace
echo "Please provide the name for the test namespace"
read nspace
if [[ -z "$nspace" ]]; then
    echo "ERROR: No name for test namespace provided"
    exit 1
fi
export MICO_TEST_NAMESPACE=$nspace
echo "Use test namespace '${MICO_TEST_NAMESPACE}'"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
echo "Change directory to '$DIR'"
cd $DIR

mkdir tmp
cp *.yaml tmp/

#cp /**/*.yaml tmp/
# loop through files and change namespace
# database namepace?

cd tmp

# Prepare MICO build bot namespace
echo "$(envsubst < mico-build-bot.yaml)" > mico-build-bot.yaml

# Replace namespaces with test namespace
sed -i -- 's/mico-build-bot/'"${MICO_TEST_NAMESPACE}"'/g' *
sed -i -- 's/mico-system/'"${MICO_TEST_NAMESPACE}"'/g' *

# Remove public IP
sed -i -- '/${MICO_PUBLIC_IP}/d' mico-admin.yaml

# Change namespace 'mico-workspace'
sed -i -- '/application.properties: |-/a \    kubernetes.build-bot.namespace-build-execution='"${MICO_TEST_NAMESPACE}"'' mico-core.yaml
sed -i -- '/application.properties: |-/a \    kubernetes.namespace-mico-workspace='"${MICO_TEST_NAMESPACE}"'' mico-core.yaml

# Change namespace 'knative-build'
sed -i -- 's/namespace: knative-build/namespace: '"${MICO_TEST_NAMESPACE}"'/g' knative-build.yaml

# Change namespace 'monitoring'
sed -i -- 's/namespace: monitoring/namespace: '"${MICO_TEST_NAMESPACE}"'/g' monitoring.yaml
sed -i -- 's/monitoring/'"${MICO_TEST_NAMESPACE}"'/g' mico-core.yaml

# Create test namespace
kubectl create namespace ${MICO_TEST_NAMESPACE}

# Grant cluster-admin permissions
kubectl create clusterrolebinding ${MICO_TEST_NAMESPACE}-cluster-admin \
  --clusterrole=cluster-admin \
  --serviceaccount=${MICO_TEST_NAMESPACE}:default

# Apply required components
kubectl apply -f neo4j.yaml
kubectl apply -f redis.yaml
kubectl apply -f mico-core.yaml
kubectl apply -f mico-admin.yaml
kubectl apply -f mico-build-bot.yaml
kubectl apply -f knative-build.yaml
kubectl apply -f monitoring.yaml

cd ../
rm -rf tmp/

# To delete the test namespace use:
# kubectl delete namespace ${MICO_TEST_NAMESPACE}
