#!/usr/bin/env sh
set -eu

envsubst '${MICO_REST_API}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

exec "$@"
