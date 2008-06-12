#! /bin/bash

dist_name=`cat dist/temp/dist_name`

cd dist/temp/c/osx

gcc -c -I/System/Library/Frameworks/JavaVM.framework/Headers osx_push_provider.m
gcc -dynamiclib -o lib$dist_name.jnilib osx_push_provider.o -framework JavaVM -framework AppKit

cd ../../../..

cp dist/temp/c/osx/lib$dist_name.jnilib dist/
