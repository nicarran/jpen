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
#include "../jpen_provider_wintab_WintabAccess.h"
#include "Access.h"
#include "BuildNumber.h"

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
	jint range[2];
	Access_getValuatorRange(Access_getP(cellIndex), levelTypeOrdinal, range);
	jintArray r=(*pEnv)->NewIntArray(pEnv, 2);
	if(!r)
		return NULL;
	(*pEnv)->SetIntArrayRegion(pEnv, r, 0, 2, range);
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
 * Method:    getCursorName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_wintab_WintabAccess_getCursorName
(JNIEnv *pEnv, jclass class, jint cursor) {
	TCHAR r[256];
	WTInfo( WTI_CURSORS + cursor, CSR_NAME, &r);
	return (*pEnv)->NewStringUTF(pEnv, r);
}

/*
 * Class:     jpen_provider_wintab_WintabAccess
 * Method:    getPhysicalId
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_jpen_provider_wintab_WintabAccess_getPhysicalId
(JNIEnv *pEnv, jclass class, jint cursor) {
	DWORD r;
	WTInfo( WTI_CURSORS + cursor, CSR_PHYSID, &r );
	jlong r2=r;
	return r2;
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
