#!/bin/ash
set -eu

echo "MICO Rest Api Url is: ${MICO_REST_API}"
envsubst '${MICO_REST_API}' < /etc/nginx/nginx.conf > /etc/nginx/nginx.conf

if [ "${NAMESERVER}" == "" ]; then
    export NAMESERVER=$(awk '/^nameserver/{print $2}' /etc/resolv.conf)
fi

echo "Nameserver is:" ${NAMESERVER}

envsubst '${NAMESERVER}' < /etc/nginx/nginx.conf > /etc/nginx/nginx.conf

exec "$@"
