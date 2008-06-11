#! /bin/bash

cd dist/temp/c/osx

gcc -c -I/System/Library/Frameworks/JavaVM.framework/Headers osx_push_provider.m
gcc -dynamiclib -o jpen-2.jnilib osx_push_provider.o -framework JavaVM -framework AppKit

cd ../../../..

cp dist/temp/c/osx/jpen-2.jnilib dist/
