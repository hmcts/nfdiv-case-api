#!/usr/bin/env bash

if [ -z "${DEPLOYMENT_ENVIRONMENT}" ]; then
  echo "DEPLOYMENT_ENVIRONMENT is not set"
  exit 1
fi

export AZURE_CONFIG_DIR="/opt/jenkins/.azure-${DEPLOYMENT_ENVIRONMENT}"
az acr login --name hmctsprod --subscription DCD-CNP-PROD
