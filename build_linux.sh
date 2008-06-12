#! /bin/bash

dist_name=`cat dist/temp/dist_name`

cd dist/temp/c/linux

# SUPER IMPORTANT SYNTAX NOTE: mac gcc uses -dynamiclib instead of -shared
gcc -arch i386 -I/System/Library/Frameworks/JavaVM.framework/Headers -L/usr/X11R6/lib -L/usr/lib -Wall -fPIC -dynamiclib -o lib$dist_name.so -lX11 -lXi *.c
# -shared

cd ../../../..

# Undefined symbols: "_main", referenced from: start in crt1.10.5.o

cp dist/temp/c/linux/lib$dist_name.so dist
