#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
basePath=$(dirname $(dirname $scriptPath))
ccdDefinitionsPath=$basePath/nfdiv-case-api/ccd-definitions/definitions/development

definitionOutputFile=$basePath/nfdiv-case-api/ccd-definitions/build/ccd-config/ccd-nfd-dev.xlsx
params="$@"

cd $basePath

echo "Definition directory: ${ccdDefinitionsPath}"
echo "Definition spreadsheet ${definitionOutputFile}"
echo "Additional parameters: ${params}"

mkdir -p $(dirname ${definitionOutputFile})

${scriptPath}/generate-ccd-definition.sh $ccdDefinitionsPath $definitionOutputFile "${params}"
${scriptPath}/ccd-import-definition.sh $definitionOutputFile
