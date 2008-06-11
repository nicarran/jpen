#! /bin/bash

./build_setup.sh
# ./build_win.sh
# ./build_linux.sh
./build_osx.sh

# Update the pre-packaged dist:
cp dist/jpen-2.* prepackaged_hack

