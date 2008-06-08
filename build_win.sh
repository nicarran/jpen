#! bash

dist_name=`cat dist/temp/dist_name`

LOCAL_MINGW_HOME=c:/mingw
LOCAL_JAVA_HOME=c:/java/jdk/1.6.0

if [ ! -e $LOCAL_MINGW_HOME ]; then
	echo "ERROR: missing MinGW"
	exit
fi
if [ ! -e $LOCAL_MINGW_HOME/bin/gcc ]; then
	echo "ERROR: missing gcc"
	exit
fi

cd dist/temp/c/windows

if [ ! -e INCLUDE/WINTAB.H ]; then
	echo "ERROR: missing Wintab includes"
	exit
fi
if [ ! -e lib/WINTAB32.EXP ]; then
	echo "ERROR: missing Wintab libraries"
	exit
fi

$LOCAL_MINGW_HOME/bin/gcc -Wall -c -mrtd -D_JNI_IMPLEMENTATION -I$LOCAL_JAVA_HOME/include -I$LOCAL_JAVA_HOME/include/win32 *.c 
$LOCAL_MINGW_HOME/bin/gcc -Wall -Wl,-kill-at -shared -o $dist_name.dll *.o -Llib -lWintab32 

cd ../../../..

cp dist/temp/c/windows/$dist_name.dll dist
