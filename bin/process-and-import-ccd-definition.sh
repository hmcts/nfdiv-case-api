#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))

root_dir=$(realpath $(dirname ${0})/..)
build_dir=${root_dir}/build/ccd-config

 ${scriptPath}/ccd-build-definition.sh

for dir in $(find ${root_dir}/build/definitions/ -maxdepth 1 -mindepth  1 -type d -exec basename {} \;)
do
  caseType=$(echo $dir | tr 'A-Z' 'a-z')
  definitionOutputFile=${build_dir}/ccd-$caseType-${CCD_DEF_NAME:-dev}.xlsx
  ${scriptPath}/ccd-import-definition.sh $definitionOutputFile
done
