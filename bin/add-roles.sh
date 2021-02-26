#!/usr/bin/env bash

scriptPath=$(dirname $(realpath $0))

# Roles used during the CCD import
${scriptPath}/add-ccd-role.sh "caseworker-divorce-courtadmin_beta"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-superuser"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-courtadmin-la"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-courtadmin"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-solicitor "
