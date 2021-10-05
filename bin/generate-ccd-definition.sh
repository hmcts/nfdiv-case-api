#!/usr/bin/env bash

set -eu

definition_processor_version=latest

definition_dir=${1}
definition_output_file=${2}
additionalParameters=${3-}

definition_input_dir=${definition_dir}

if [[ ! -e ${definition_output_file} ]]; then
   touch ${definition_output_file}
fi

echo "Definition input directory: ${definition_input_dir}"
echo "Definition output file: ${definition_output_file}"
echo "Additional params: ${additionalParameters}"

docker run --pull always --user $UID --rm --name json2xlsx \
  -v ${definition_input_dir}:/tmp/ccd-definition \
  -v ${definition_output_file}:/tmp/ccd-definition.xlsx \
  hmctspublic.azurecr.io/ccd/definition-processor:${definition_processor_version} \
  json2xlsx -D /tmp/ccd-definition -o /tmp/ccd-definition.xlsx ${additionalParameters}
