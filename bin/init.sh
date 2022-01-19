az keyvault secret show --vault-name nfdiv-aat -o tsv --query value --name nfdiv-local-env-config | base64 -d > .devenv

az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1
az acr login --name hmctsprivate --subscription 8999dec3-0104-4a27-94ee-6588559729d1
