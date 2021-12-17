#!/usr/bin/env bash

scriptPath=$(dirname $(realpath $0))

# Roles used during the CCD import
${scriptPath}/add-ccd-role.sh "caseworker-divorce-courtadmin_beta"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-superuser"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-courtadmin-la"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-courtadmin"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-solicitor"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-pcqextractor"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-systemupdate"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-bulkscan"
${scriptPath}/add-ccd-role.sh "caseworker-caa"
${scriptPath}/add-ccd-role.sh "citizen"
${scriptPath}/add-ccd-role.sh "caseworker-divorce"
${scriptPath}/add-ccd-role.sh "caseworker"
${scriptPath}/add-ccd-role.sh "pui-case-manager"
${scriptPath}/add-ccd-role.sh "pui-finance-manager"
${scriptPath}/add-ccd-role.sh "pui-organisation-manager"
${scriptPath}/add-ccd-role.sh "pui-user-manager"
