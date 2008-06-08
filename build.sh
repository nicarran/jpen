#! bash

./build_clean.sh
./build_write_revision.sh
ant dist
echo [Building native libraries]
./build_native.sh
echo [Building installer]
ant so_hack
ant installer
rm -rf dist/temp
