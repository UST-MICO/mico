#!/bin/bash

# Check if required environment variable are set
if [[ -z "${DOCKERHUB_USERNAME_BASE64}" ]]; then
    err=true
    echo "Warning: Environment variable DOCKERHUB_USERNAME_BASE64 is not set."
fi

if [[ -z "${DOCKERHUB_PASSWORD_BASE64}" ]]; then
    err=true
    echo "Warning: Environment variable DOCKERHUB_PASSWORD_BASE64 is not set."
fi

# Check if test namespace is set
if [[ -z "${MICO_TEST_NAMESPACE}" ]]; then
    err=true
    echo "Environment variable MICO_TEST_NAMESPACE is not set."
else
    echo "Use test namespace '${MICO_TEST_NAMESPACE}''"
fi

if [ "$err" = true ]; then
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
echo "Change directory to '$DIR'"
cd $DIR

mkdir tmp
cp *.yaml tmp/
cd tmp

# Prepare MICO build bot namespace
envsubst < mico-build-bot.yaml | > mico-build-bot.yaml

# Replace namespaces with test namespace
sed -i -- 's/mico-build-bot/'"${MICO_TEST_NAMESPACE}"'/g' *
sed -i -- 's/mico-system/'"${MICO_TEST_NAMESPACE}"'/g' *

# Remove public IP
sed -i -- '/${MICO_PUBLIC_IP}/d' mico-admin.yaml

# Change namespace 'mico-workspace'
sed -i -- '/application.properties: |-/a \    kubernetes.build-bot.namespace-build-execution='"${MICO_TEST_NAMESPACE}"'' mico-core.yaml
sed -i -- '/application.properties: |-/a \    kubernetes.namespace-mico-workspace='"${MICO_TEST_NAMESPACE}"'' mico-core.yaml

kubectl create namespace ${MICO_TEST_NAMESPACE}
kubectl apply -f fabric8-rbac.yaml
kubectl apply -f neo4j.yaml
kubectl apply -f mico-core.yaml
kubectl apply -f mico-admin.yaml
kubectl apply -f mico-build-bot.yaml

cd ../
rm -rf tmp/

# To delete the test namespace use:
# kubectl delete namespace ${MICO_TEST_NAMESPACE}
