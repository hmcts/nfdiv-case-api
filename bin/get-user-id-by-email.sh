#!/usr/bin/env bash

email=${1}
idamUser=$(az keyvault secret show --vault-name nfdiv-aat -o tsv --query value --name idam-admin-username)
idamPassword=$(az keyvault secret show --vault-name nfdiv-aat -o tsv --query value --name idam-admin-password)
token=$(curl -s -H 'Content-Type: application/x-www-form-urlencoded' -XPOST "https://idam-api.aat.platform.hmcts.net/loginUser?username=$idamUser&password=$idamPassword" | docker run --rm --interactive stedolan/jq -r .api_auth_token)

curl -X GET "https://idam-api.aat.platform.hmcts.net/users?email=$email" -H "accept: */*" -H "authorization: AdminApiAuthToken $token"
