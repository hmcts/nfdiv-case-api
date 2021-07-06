#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
caseType=${1}
echo "Script Path ${scriptPath}"

root_dir=$(realpath $(dirname ${0})/..)
config_dir=${root_dir}/build/definitions/${caseType}
build_dir=${root_dir}/build/ccd-config
definitionOutputFile=${build_dir}/ccd-${caseType}-${CCD_DEF_NAME:-dev}.xlsx

mkdir -p ${build_dir}

${scriptPath}/generate-ccd-definition.sh $config_dir $definitionOutputFile "-e *-nonprod.json,*-testing.json"
