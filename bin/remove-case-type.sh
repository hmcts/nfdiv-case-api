#!/bin/env bash

CASE_TYPE_ID=\'NFD_PR_${CHANGE_ID}\',\'NO_FAULT_DIVORCE_BulkAction_PR_${CHANGE_ID}\'

echo "Removing case types: ${CASE_TYPE_ID} from CCD"
psql -f ./bin/remove-case-type-from-def-store.sql --variable "changeId=${CHANGE_ID}" --variable "caseTypeReferences=${CASE_TYPE_ID}" "host=${CCD_DEF_STORE_POSTGRES_HOST} port=${CCD_DEF_STORE_POSTGRES_PORT} dbname=${CCD_DEF_STORE_POSTGRES_DATABASE} user=${CCD_DEF_STORE_POSTGRES_USERNAME} password=${CCD_DEF_STORE_POSTGRES_PASSWORD} sslmode=require"
psql -f ./bin/remove-case-type-from-data-store.sql --variable "caseTypeReferences=${CASE_TYPE_ID}" "host=${CCD_DATA_STORE_POSTGRES_HOST} port=${CCD_DATA_STORE_POSTGRES_PORT} dbname=${CCD_DATA_STORE_POSTGRES_DATABASE} user=${CCD_DATA_STORE_POSTGRES_USERNAME} password=${CCD_DATA_STORE_POSTGRES_PASSWORD} sslmode=require"

