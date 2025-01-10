#!/usr/bin/env bash

#export IDAM_STUB_LOCALHOST=http://localhost:5555

set -eu

dir=$(dirname ${0})
filepath=${1}
filename=$(basename ${filepath})
uploadFilename="$(date +"%Y%m%d-%H%M%S")-${filename}"

userToken=$(${dir}/idam-lease-user-token.sh ${DEFINITION_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net} ${DEFINITION_IMPORTER_PASSWORD:-Password12!})
serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${CCD_API_GATEWAY_S2S_KEY:-AAAAAAAAAAAAAAAC}))

uploadResponse=$(curl --insecure --silent -w "\n%{http_code}"  --show-error --max-time 60  -X POST \
  ${DEFINITION_STORE_URL_BASE:-http://localhost:4451}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F "file=@${filepath};filename=${uploadFilename}" || echo 'bypass-if-error')


echo "Upload response ${uploadResponse}"

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

echo "File upload status : ${upload_http_code} "
echo "Upload response content : ${upload_response_content}"
