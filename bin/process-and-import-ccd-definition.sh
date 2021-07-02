#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))

root_dir=$(realpath $(dirname ${0})/..)
build_dir=${root_dir}/build/ccd-config
definitionOutputFile=${build_dir}/ccd-nfd-${CCD_DEF_NAME:-dev}.xlsx

${scriptPath}/ccd-build-definition.sh
${scriptPath}/ccd-import-definition.sh $definitionOutputFile
