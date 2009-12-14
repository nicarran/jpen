del *.o
\mingw\bin\gcc -Wall -c -mrtd -D_JNI_IMPLEMENTATION -Ic:\java\jdk1.6\include -Ic:\java\jdk1.6\include\win32 *.c
\mingw\bin\gcc -Wall -Wl,--kill-at -shared -o jpen-2-2.dll *.o -Llib -lWintab32
copy /Y jpen-2-2.dll \windows\system32