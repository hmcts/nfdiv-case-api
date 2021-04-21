#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))

echo "Script Path ${scriptPath}"

root_dir=$(realpath $(dirname ${0})/..)
config_dir=${root_dir}/build/definitions
build_dir=${root_dir}/build/ccd-config
definitionOutputFile=${build_dir}/ccd-nfd-${CCD_DEF_NAME:-dev}.xlsx

mkdir -p ${build_dir}

${scriptPath}/generate-ccd-definition.sh $config_dir $definitionOutputFile "-e *-nonprod.json,*-testing.json"
