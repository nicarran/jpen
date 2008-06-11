#! /bin/bash

mkdir -p dist/temp/c
cp src/c/*.{m,c,h} dist/temp/c

mkdir -p dist/temp/c/windows
cp src/c/windows/*.{m,c,h} dist/temp/c/windows
cp src/c/utils/*.{m,c,h} dist/temp/c/windows

mkdir -p dist/temp/c/linux
cp src/c/linux/*.{m,c,h} dist/temp/c/linux
cp src/c/utils/*.{m,c,h} dist/temp/c/linux

mkdir -p dist/temp/c/osx
cp src/c/osx/*.{m,c,h} dist/temp/c/osx
cp src/c/utils/*.{m,c,h} dist/temp/c/osx


# Wintab files:
mkdir -p dist/temp/c/windows/INCLUDE
cp src/c/windows/INCLUDE/*.{c,h,C,H} dist/temp/c/windows/INCLUDE
mkdir -p dist/temp/c/windows/lib
cp src/c/windows/lib/*.{exp,lib,def,EXP,LIB,DEF} dist/temp/c/windows/lib

