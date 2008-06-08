#! bash

mkdir -p dist/temp/c

mkdir -p dist/temp/c/windows
mkdir -p dist/temp/c/linux
cp src/c/*.{c,h} dist/temp/c
cp src/c/windows/*.{c,h} dist/temp/c/windows
cp src/c/linux/*.{c,h} dist/temp/c/linux
cp src/c/utils/*.{c,h} dist/temp/c/windows
cp src/c/utils/*.{c,h} dist/temp/c/linux

# Wintab files:
mkdir -p dist/temp/c/windows/INCLUDE
cp src/c/windows/INCLUDE/*.{c,h,C,H} dist/temp/c/windows/INCLUDE
mkdir -p dist/temp/c/windows/lib
cp src/c/windows/lib/*.{exp,lib,def,EXP,LIB,DEF} dist/temp/c/windows/lib

