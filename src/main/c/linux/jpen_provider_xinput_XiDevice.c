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
#include "../jpen_provider_xinput_XiDevice.h"
#include "Device.h"

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getIsListening
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_jpen_provider_xinput_XiDevice_getIsListening
(JNIEnv *pEnv, jclass class, jint cellIndex){
	return Device_getP(cellIndex)->isListening? JNI_TRUE: JNI_FALSE;
}

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    setIsListening
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jpen_provider_xinput_XiDevice_setIsListening
(JNIEnv *pEnv, jclass class, jint cellIndex, jboolean isListening){
	return Device_setIsListening(Device_getP(cellIndex), isListening == JNI_TRUE? true: false);
}

/* Class:     jpen_provider_xinput_Device
 * Method:    getLevelRangeMin
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getLevelRangeMin
(JNIEnv *pEnv, jclass class, jint cellIndex, jint valuatorType) {
	return Device_getP(cellIndex)->valuatorRangeMins[valuatorType];
}

/*
 * Class:     jpen_provider_xinput_Device
 * Method:    getLevelRangeMax
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getLevelRangeMax
(JNIEnv *pEnv, jclass class, jint cellIndex, jint valuatorType) {
	return Device_getP(cellIndex)->valuatorRangeMaxs[valuatorType];
}

/*
 * Class:     jpen_provider_xinput_Device
 * Method:    getValue
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getValue
(JNIEnv *pEnv, jclass class, jint cellIndex, jint valuatorType) {
	return Device_getP(cellIndex)->valuatorValues[valuatorType];
}

/*
 * Class:     jpen_provider_xinput_Device
 * Method:    destroy
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_destroy
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Device_destroy(cellIndex);
}

/*
 * Class:     jpen_provider_xinput_Device
 * Method:    getError
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_xinput_XiDevice_getError
(JNIEnv *pEnv, jclass class) {
	return (*pEnv)->NewStringUTF(pEnv, Device_row.error);
}

/*
 * Class:     jpen_provider_xinput_Device
 * Method:    nextEvent
 * Signature: (I)V
 */
JNIEXPORT jboolean JNICALL Java_jpen_provider_xinput_XiDevice_nextEvent
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Device_nextEvent(Device_getP(cellIndex))? JNI_TRUE: JNI_FALSE;
}

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    stopWaitingNextEvent
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_jpen_provider_xinput_XiDevice_stopWaitingNextEvent
(JNIEnv *pEnv, jclass class, jint cellIndex){
	Device_stopWaitingNextEvent(Device_getP(cellIndex));
}

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    waitNextEvent
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_jpen_provider_xinput_XiDevice_waitNextEvent
(JNIEnv *pEnv, jclass class, jint cellIndex){
	return Device_waitNextEvent(Device_getP(cellIndex));
}


/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getLastEventTime
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_jpen_provider_xinput_XiDevice_getLastEventTime
(JNIEnv *pEnv, jclass class, jint cellIndex){
	return Device_getP(cellIndex)->lastEventTime;
}

/*
 * Class:     jpen_provider_xinput_Device
 * Method:    getLastEventType
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getLastEventType
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Device_getP(cellIndex)->lastEventType;
}

/*
 * Class:     jpen_provider_xinput_Device
 * Method:    getLastEventButton
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getLastEventButton
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Device_getP(cellIndex)->lastEventButton;
}

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    refreshLevelRanges
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_jpen_provider_xinput_XiDevice_refreshLevelRanges
(JNIEnv *pEnv, jclass class, jint cellIndex){
	Device_refreshValuatorRanges(Device_getP(cellIndex));
}
