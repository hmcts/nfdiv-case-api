#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))

echo "Script Path ${scriptPath}"

root_dir=$(realpath $(dirname ${0})/..)
config_dir=${root_dir}/ccd-definitions/definitions/development
build_dir=${root_dir}/ccd-definitions/build/ccd-config
definitionOutputFile=${build_dir}/ccd-nfd-dev.xlsx

mkdir -p ${build_dir}

${scriptPath}/generate-ccd-definition.sh $config_dir $definitionOutputFile "-e *-nonprod.json,*-testing.json"
${scriptPath}/ccd-import-definition.sh $definitionOutputFile
