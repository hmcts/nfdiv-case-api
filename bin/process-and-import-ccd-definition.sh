#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
basePath=$(dirname $(dirname $scriptPath))
ccdDefinitionsPath=$basePath/nfdiv-case-api/ccd-definitions/definitions/development

config_build_dir=$basePath/nfdiv-case-api/ccd-definitions/build/ccd-config
definitionOutputFile=${config_build_dir}/ccd-nfd-dev.xlsx

cd $basePath

echo "Definition directory: ${ccdDefinitionsPath}"
echo "Definition spreadsheet ${definitionOutputFile}"

mkdir -p ${config_build_dir}

${scriptPath}/generate-ccd-definition.sh $ccdDefinitionsPath $definitionOutputFile "-e *-nonprod.json,*-testing.json"
${scriptPath}/ccd-import-definition.sh $definitionOutputFile
