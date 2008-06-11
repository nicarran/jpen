#! /bin/bash

echo [Clean]
./build_clean.sh
echo [Building JPEN]
./build_write_revision.sh
ant dist
echo [Building native libraries]
./build_native.sh
echo [Building installer]
ant so_hack
ant installer
rm -rf dist/temp
echo [Building bridge]
./build_write_revision.sh
ant -f build_bridge.xml dist
rm -rf dist/temp
