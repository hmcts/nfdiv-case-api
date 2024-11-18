#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
echo "Script Path ${scriptPath}"

root_dir=$(realpath $(dirname ${0})/..)
build_dir=${root_dir}/build/ccd-config

for file in $(find  ${build_dir} -name "*.xlsx")
do
   echo "files for upload is " + $file
   filename=$(basename $file)
   echo "file name is " + $filename
  (${scriptPath}/ccd-import-definition.sh $file $filename)
done

wait
