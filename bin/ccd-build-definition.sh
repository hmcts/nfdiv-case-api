#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
caseType=${1}
echo "Script Path ${scriptPath}"

root_dir=$(realpath $(dirname ${0})/..)
build_dir=${root_dir}/build/ccd-config

mkdir -p ${build_dir}

for dir in $(find ${root_dir}/build/definitions/ -maxdepth 1 -mindepth  1 -type d -exec basename {} \;)
do
  caseType=$(echo $dir | tr 'A-Z' 'a-z')
  config_dir=${root_dir}/build/definitions/${caseType}
  definitionOutputFile=${build_dir}/ccd-${caseType}-${CCD_DEF_NAME:-dev}.xlsx

  ${scriptPath}/generate-ccd-definition.sh $config_dir $definitionOutputFile "-e *-nonprod.json,*-testing.json"
done
