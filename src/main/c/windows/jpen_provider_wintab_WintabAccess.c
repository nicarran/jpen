/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>
Copyright 2009 Marcello Bastea-Forte <marcello at cellosoft.com>

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
#include "../jpen_provider_wintab_WintabAccess.h"
#include "Access.h"
#include "../nativeBuild/windows-BuildNumber.h"

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    create
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_create
(JNIEnv *pEnv, jclass class) {
	return Access_create();
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getError
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_wintab_WintabAccess_getError
(JNIEnv *pEnv, jclass class) {
	return (*pEnv)->NewStringUTF(pEnv, Access_row.error);
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getNativeBuild
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getNativeBuild
(JNIEnv *pEnv, jclass class){
	return BUILD_NUMBER;
}


/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getValue
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getValue
(JNIEnv *pEnv, jclass class, jint cellIndex, jint valueType) {
	return Access_getP(cellIndex)->valuatorValues[valueType];
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    nextPacket
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_jpen_provider_wintab_WintabAccess_nextPacket
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Access_nextPacket(Access_getP(cellIndex))? JNI_TRUE: JNI_FALSE;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getEnabled
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_jpen_provider_wintab_WintabAccess_getEnabled
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Access_getEnabled(Access_getP(cellIndex))? JNI_TRUE: JNI_FALSE;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getStatus
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getStatus
(JNIEnv *pEnv, jclass class, jint cellIndex){
	return Access_getP(cellIndex)->status;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    setEnabled
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jpen_provider_wintab_WintabAccess_setEnabled
(JNIEnv *pEnv, jclass class, jint cellIndex, jboolean enabled) {
	Access_setEnabled(Access_getP(cellIndex), enabled==JNI_TRUE);
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getLevelRange
 * Signature: (II)[I
 */
JNIEXPORT jintArray JNICALL Java_jpen_provider_wintab_WintabAccess_getLevelRange
(JNIEnv *pEnv, jclass class, jint cellIndex, jint levelTypeOrdinal) {
	jint range[4];
	Access_getValuatorRange(Access_getP(cellIndex), levelTypeOrdinal, range);
	jintArray r=(*pEnv)->NewIntArray(pEnv, 4);
	if(!r)
		return NULL;
	(*pEnv)->SetIntArrayRegion(pEnv, r, 0, 4, range);
	return r;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getCursor
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getCursor
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Access_getP(cellIndex)->cursor;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getTime
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_jpen_provider_wintab_WintabAccess_getTime
(JNIEnv *pEnv, jclass class, jint cellIndex){
	return Access_getP(cellIndex)->time;
}


/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getButtons
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getButtons
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Access_getP(cellIndex)->buttons;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getCursorTypeOrdinal
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getCursorTypeOrdinal
(JNIEnv *pEnv, jclass class, jint cursor) {
	return Access_getCsrType(cursor);
}


/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getRawCursorType
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getRawCursorType
(JNIEnv *pEnv, jclass class, jint cursor) {
	UINT cursorType = 0;
	WTInfo( WTI_CURSORS + cursor, CSR_TYPE, &cursorType );
	return cursorType;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getCursorName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_wintab_WintabAccess_getCursorName
(JNIEnv *pEnv, jclass class, jint cursor) {
	TCHAR r[256];
	WTInfo( WTI_CURSORS + cursor, CSR_NAME, &r);
	r[255] = 0;
	return (*pEnv)->NewStringUTF(pEnv, r);
}


/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getDeviceName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_wintab_WintabAccess_getDeviceName
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	TCHAR r[256];
	WTInfo( WTI_DEVICES + Access_getP(cellIndex)->device, DVC_NAME, &r);
	r[255] = 0;
	return (*pEnv)->NewStringUTF(pEnv, r);
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getDeviceHardwareCapabilities
 * Signature: (I)I;
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getDeviceHardwareCapabilities
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	UINT hardware = 0;
	WTInfo( WTI_DEVICES + Access_getP(cellIndex)->device, DVC_HARDWARE, &hardware);
	return hardware;
}


/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getPacketRate
 * Signature: ()I;
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getPacketRate
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	UINT rate = 0;
	WTInfo(WTI_DEVICES + Access_getP(cellIndex)->device, DVC_PKTRATE, &rate);
	return rate;
}

JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getButtonCount
(JNIEnv *pEnv, jclass class, jint cursor) {
	BYTE buttons;
	WTInfo(WTI_CURSORS + cursor, CSR_BUTTONS, &buttons);
	return buttons;
}

JNIEXPORT jobjectArray JNICALL Java_jpen_provider_wintab_WintabAccess_getButtonNames
(JNIEnv *pEnv, jclass class, jint cursor) {

	int buttonCount = 0;
	WTInfo( WTI_CURSORS + cursor, CSR_BUTTONS, &buttonCount);

	jobjectArray r=(*pEnv)->NewObjectArray(pEnv, buttonCount, (*pEnv)->FindClass(pEnv, "java/lang/String"), NULL);
	if (!r)
		return NULL;
	if (buttonCount) {
		TCHAR buttons[2048];

		// This method returns a null-delimited and terminated array
		// e.g: Button 1\0Button2\0\0
		WTInfo( WTI_CURSORS + cursor, CSR_BTNNAMES, &buttons);

		// Make sure we don't have any buffer overruns (hate null-terminated strings)...
		buttons[2046] = buttons[2047] = 0;

		int i=0;
		TCHAR *buttonPtr = buttons;
		while (*buttonPtr && i < buttonCount) {
	        (*pEnv)->SetObjectArrayElement(pEnv, r, i, (*pEnv)->NewStringUTF(pEnv, buttonPtr));
			buttonPtr += strlen(buttonPtr);
			buttonPtr++;
			i++;
		}
	}
	return r;
}


JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getCapabilityMask
(JNIEnv *pEnv, jclass class, jint cursor) {
	WTPKT wtpkt;
	WTInfo(WTI_CURSORS + cursor, CSR_PKTDATA, &wtpkt);
	return wtpkt;
}
/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getPhysicalId
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_getPhysicalId
(JNIEnv *pEnv, jclass class, jint cursor) {
	DWORD r;
	// Physical ID is a 32bit unique id for a cursor type. It needs to be combined with CSR_TYPE to truly be unique:

	// According to Wacom (http://www.wacomeng.com/devsupport/ibmpc/gddevpc.html):
	//  The ID code from the device is in two sections. It is the combination of the two
	//  that is guaranteed unique. One section, the CSR_TYPE, is actually a code unique
	//  to each device type. The other section, the CSR_PHYSID, is a unique 32 bit number
	//  within a device type. CSR_PHYSID  may be repeated between device types, but not
	//  within a type.

	WTInfo( WTI_CURSORS + cursor, CSR_PHYSID, &r );
	return r;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    destroy
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_wintab_WintabAccess_destroy
(JNIEnv *pEnv, jclass class, jint cellIndex) {
	return Access_destroy(cellIndex);
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getSystemCursorEnabled
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_jpen_provider_wintab_WintabAccess_getSystemCursorEnabled
(JNIEnv *pEnv, jclass class, jint cellIndex){
	return Access_getSystemCursorEnabled(Access_getP(cellIndex))? JNI_TRUE: JNI_FALSE;
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    setSystemCursorEnabled
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jpen_provider_wintab_WintabAccess_setSystemCursorEnabled
(JNIEnv *pEnv, jclass class, jint cellIndex, jboolean enabled){
	Access_setSystemCursorEnabled(Access_getP(cellIndex), enabled==JNI_TRUE);
}