/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
#include "../jpen_provider_xinput_XiBus.h"
#include "Bus.h"
#include "../nativeBuild/linux-BuildNumber.h"

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    getNativeBuild
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_getNativeBuild
(JNIEnv *pEnv, jclass class){
	return BUILD_NUMBER;
}

/*
 * Class:     jpen_provider_xinput_Bus
 * Method:    create
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_create
(JNIEnv *pEnv, jclass class){
  return Bus_create();
}

/*
 * Class:     jpen_provider_xinput_Bus
 * Method:    getError
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_xinput_XiBus_getError
(JNIEnv *pEnv, jclass class){
  return (*pEnv)->NewStringUTF(pEnv, Bus_row.error);
}

/*
 * Class:     jpen_provider_xinput_Bus
 * Method:    getDevicesSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_getDevicesSize
(JNIEnv *pEnv, jclass class, jint cellIndex){
  return Bus_getP(cellIndex)->deviceInfoSize;
}

/*
 * Class:     jpen_provider_xinput_Bus
 * Method:    getDeviceName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_xinput_XiBus_getDeviceName
(JNIEnv *pEnv, jclass class, jint cellIndex, jint deviceIndex){
  return (*pEnv)->NewStringUTF(pEnv, Bus_getP(cellIndex)->pDeviceInfo[deviceIndex].name);
}

/*
 * Class:     jpen_provider_xinput_Bus
 * Method:    setDevice
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_setDevice
(JNIEnv *pEnv, jclass class, jint cellIndex, jint deviceIndex){
  return Bus_setDevice(Bus_getP(cellIndex), deviceIndex);
}

/*
 * Class:     jpen_provider_xinput_Bus
 * Method:    destroy
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_destroy
(JNIEnv *pEnv, jclass class, jint cellIndex){
  return Bus_destroy(cellIndex);
}

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    refreshDeviceInfo
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_refreshDeviceInfo
(JNIEnv *pEnv, jclass class, jint cellIndex){
	return Bus_refreshDeviceInfo(Bus_getP(cellIndex));
}

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    printXNextRequestSerial
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_jpen_provider_xinput_XiBus_printXNextRequestSerial
(JNIEnv *pEnv, jclass class, jint cellIndex){
	return Bus_printXNextRequestSerial(Bus_getP(cellIndex));
}

