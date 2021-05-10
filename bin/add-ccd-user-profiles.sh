#!/usr/bin/env bash

read -r -d '' CCD_USERS << EOM
DivCaseWorkerUser@AAT.com|DIVORCE|NO_FAULT_DIVORCE|Submitted
TEST_CASE_WORKER_USER@mailinator.com|DIVORCE|NO_FAULT_DIVORCE|Submitted
TEST_SOLICITOR@mailinator.com|DIVORCE|NO_FAULT_DIVORCE|Submitted
divorce_as_caseworker_admin@mailinator.com|DIVORCE|NO_FAULT_DIVORCE|Submitted
EOM

echo "Getting service_token from s2s"
dir=$(dirname ${0})
serviceToken=$(${dir}/s2s-token.sh ccd_data)

users=$(echo "${CCD_USERS}" | tr "," "\n")
for user in $users
do
    echo "Adding \"$user\" ..."
    EMAIL=$(echo $user | cut -f1 -d'|')
    JURISDICTION=$(echo $user | cut -f2 -d'|')
    DEFAULT_CASE_TYPE=$(echo $user | cut -f3 -d'|')
    DEFAULT_CASE_STATE=$(echo $user | cut -f4 -d'|')
    post=$(curl --silent --show-error -X PUT ${CCD_USER_PROFILE_URL:-http://localhost:4453}/user-profile/users  -H "content-type: application/json" -H "ServiceAuthorization: Bearer ${serviceToken}" -d "[{\"id\": \"${EMAIL}\",\"work_basket_default_jurisdiction\": \"${JURISDICTION}\",\"work_basket_default_case_type\": \"${DEFAULT_CASE_TYPE}\",\"work_basket_default_state\": \"${DEFAULT_CASE_STATE}\"}]\"")
    echo "done: ${post}"
done
