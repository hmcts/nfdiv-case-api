#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
echo "Script Path ${scriptPath}"

root_dir=$(realpath $(dirname ${0})/..)
build_dir=${root_dir}/build/ccd-config

mkdir -p ${build_dir}

az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1

for dir in $(find ${root_dir}/build/definitions/ -maxdepth 1 -mindepth  1 -type d -exec basename {} \;)
do
  config_dir=${root_dir}/build/definitions/${dir}
  definitionOutputFile=${build_dir}/ccd-${dir}-${CCD_DEF_NAME:-dev}.xlsx

  (${scriptPath}/generate-ccd-definition.sh $config_dir $definitionOutputFile "-e *-nonprod.json,*-testing.json") &
done

wait
