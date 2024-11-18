#!/usr/bin/env bash

# User used during the CCD import and ccd-role creation
roles=("caseworker-divorce-courtadmin_beta" "caseworker-divorce-superuser" "caseworker-divorce-courtadmin-la" "caseworker-divorce-courtadmin" "caseworker-divorce-solicitor" "caseworker-divorce-judg" "caseworker-divorce-pcqextractor" "caseworker-divorce-systemupdate" "caseworker-divorce-bulkscan" "caseworker-caa" "caseworker-approver" "citizen" "caseworker-divorce" "caseworker" "payments" "pui-case-manager" "pui-finance-manager" "pui-organisation-manager" "pui-user-manager")
for role in "${roles[@]}"
do
  ./ccd-add-role.sh "${role}"
done
