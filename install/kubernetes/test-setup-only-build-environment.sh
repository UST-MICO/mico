#!/bin/bash

# This script only creates the Kubernetes resources that are required for building of MicoServices.
# It can be used for local testing of mico-core.
echo -e "MICO Test Setup (only build environment)\n----------------------------------------"

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
    echo "Using DockerHub credentials provided by environment variables."
fi

export MICO_TEST_NAMESPACE="mico-testing"
echo "Using namespace '${MICO_TEST_NAMESPACE}'"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $DIR

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

echo -e "\nScript execution finished!"

# To delete the test namespace use:
# kubectl delete namespace ${MICO_TEST_NAMESPACE}
