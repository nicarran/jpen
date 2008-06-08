cd src/c/linux

gcc -Wall -fpic -shared -o libjpen-2.so -lXi *.c

cd ../../..
