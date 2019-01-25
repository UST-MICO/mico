#!/bin/bash
set -e

echo "starting" >&2
envsubst '${MICO_REST_API}' < /etc/nginx/nginx.conf > /etc/nginx/nginx.conf
echo "replaced" >&2
exec "$@"
