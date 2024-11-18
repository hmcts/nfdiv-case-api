#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
echo "Script Path ${scriptPath}"

root_dir=$(realpath $(dirname ${0})/..)
build_dir=${root_dir}/build/ccd-config

for file in $(find ${build_dir}/ -name "*.xlsx" \;)
do
   echo "files for upload is " + file
  (${scriptPath}/ccd-import-definition.sh $build_dir $file)
done

wait
