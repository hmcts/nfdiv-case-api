#!/usr/bin/env bash

set -eu

microservice=${1}
oneTimePassword=${2}

curl --insecure --fail --show-error --silent -X POST \
  ${SERVICE_AUTH_PROVIDER_URL:-http://localhost:4502}/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'${microservice}'",
    "oneTimePassword": "'${oneTimePassword}'"
  }'
