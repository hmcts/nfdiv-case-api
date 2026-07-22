root_dir=$(realpath $(dirname ${0})/..)

empty_definition=$(find ${root_dir}/build/ccd-config -maxdepth 1 -type f -name '*.xlsx' -size 0 -print -quit)
if [ -n "$empty_definition" ]; then
  echo "Generated CCD definition is empty: $empty_definition" >&2
  exit 1
fi
