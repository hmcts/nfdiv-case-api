#!/usr/bin/env bash
IDAM_API_BASE_URL=https://idam-api.aat.platform.hmcts.net
SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
CCD_BASE_URL=http://ccd-data-store-api-aat.service.core-compute-aat.internal
OAUTH2_CLIENT_SECRET=$(az keyvault secret show --vault-name nfdiv-aat -o tsv --query value --name idam-secret)
REDIRECT_URI=http://localhost:3001/oauth2/callback

<<USERCONFIG
    Configure users in below format
    read -r -d '' CCD_USERS <<EOM
      TestUser1@test.com|Password
      TestUser2@test.com|Password
    EOM
USERCONFIG


read -r -d '' CCD_USERS <<EOM
EOM

if [ -z "$CCD_USERS" ]
then
      echo "Users are not configured. Please see comments in script file."
      exit
else
      echo "Users configured"
fi

echo "Generating service auth token"
serviceToken=$(curl --insecure --fail --show-error --silent -X POST ${SERVICE_AUTH_PROVIDER_API_BASE_URL}/testing-support/lease -H "Content-Type: application/json" -d '{"microservice": "ccd_data"}')

users=$(echo "${CCD_USERS}" | tr "," "\n")

for user in $users; do
  email=$(echo $user | cut -f1 -d'|')
  password=$(echo $user | cut -f2 -d'|')

  echo "Generating Idam token for user $email"
  code=$(curl --insecure --fail --show-error --silent -X POST --user "$email:$password" "${IDAM_API_BASE_URL}/oauth2/authorize?redirect_uri=${REDIRECT_URI}&response_type=code&client_id=divorce" -d "" | docker run --rm --interactive stedolan/jq -r .code)
  idamToken=$(curl --insecure --fail --show-error --silent -X POST -H "Content-Type: application/x-www-form-urlencoded" --user "divorce:${OAUTH2_CLIENT_SECRET}" "${IDAM_API_BASE_URL}/oauth2/token?code=${code}&redirect_uri=${REDIRECT_URI}&grant_type=authorization_code" -d "" | docker run --rm --interactive stedolan/jq -r .access_token)

  echo "Retrieving user details for user $email"
  userDetails=$(curl --insecure --fail --show-error --silent -X GET -H "Authorization: Bearer $idamToken" "${IDAM_API_BASE_URL}/details")
  firstName=$(echo "$userDetails" | docker run --rm --interactive stedolan/jq -r .forename)
  lastName=$(echo "$userDetails" | docker run --rm --interactive stedolan/jq -r .surname)
  userId=$(echo "$userDetails" | docker run --rm --interactive stedolan/jq -r .id)

  echo "Retrieving ccd case with user id $userId"
  caseId=$(curl --insecure --fail --show-error --silent -X GET ${CCD_BASE_URL}/citizens/$userId/jurisdictions/DIVORCE/case-types/NO_FAULT_DIVORCE/cases -H "Authorization: Bearer $idamToken" -H "Content-Type: application/json" -H "ServiceAuthorization: Bearer $serviceToken" | docker run --rm --interactive stedolan/jq '.[0].id')
  eventToken=$(curl --insecure --fail --show-error --silent -X GET ${CCD_BASE_URL}/cases/$caseId/event-triggers/patchCase -H "Authorization: Bearer $idamToken" -H "Content-Type: application/json" -H "ServiceAuthorization: Bearer $serviceToken" | docker run --rm --interactive stedolan/jq -r .token)

  echo "Patching ccd case $caseId for $userId"

  curl --insecure --fail --show-error --silent -X POST http://ccd-data-store-api-aat.service.core-compute-aat.internal/citizens/$userId/jurisdictions/DIVORCE/case-types/NO_FAULT_DIVORCE/cases/$caseId/events -H "Authorization: Bearer $idamToken" -H "Content-Type: application/json" -H "ServiceAuthorization: Bearer $serviceToken" -d '{"event_token" : "'${eventToken}'","data":{"applicantFirstName": "'${firstName}'","applicantLastName":"'${lastName}'","applicantEmail" : "'${email}'"},"event" :{"id" :"patchCase"}}'
done
