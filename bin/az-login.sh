azureConfigName=$1

env AZURE_CONFIG_DIR="/opt/jenkins/.azure-${azureConfigName}"
az acr login --name hmctsprod --subscription DCD-CNP-PROD
