#! /bin/bash

./build_setup.sh
./build_win.sh
./build_linux.sh
./build_osx.sh

# Update the pre-packaged dist:
#cp dist/jpen-2*.so prepackaged_hack/jpen-2.so
#cp dist/jpen-2*.dll prepackaged_hack/jpen-2.dll
#cp dist/jpen-2*.jnilib prepackaged_hack/jpen-2.jnilib
