#!/bin/ash
set -eu

echo "MICO Rest Api Url" ${MICO_REST_API}
envsubst '${MICO_REST_API}' < /etc/nginx/nginx.conf > /etc/nginx/nginx.conf

exec "$@"
