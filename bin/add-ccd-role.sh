#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

role=${1}

userToken=$(${dir}/idam-user-token.sh ${CCD_DEFINITION_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net} ${CCD_DEFINITION_IMPORTER_PASSWORD:-Password12!})
serviceToken=$(${dir}/s2s-token.sh ccd_gw)

echo "Creating CCD role: ${role}"

curl --insecure --fail --show-error --silent --output /dev/null -X PUT \
  ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/api/user-role \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: ${serviceToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "'${role}'",
    "security_classification": "PUBLIC"
  }'
