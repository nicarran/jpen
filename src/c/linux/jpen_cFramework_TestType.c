/* [{
* (C) Copyright 2007 Nicolas Carranza and individual contributors.
* See the jpen-copyright.txt file in the jpen distribution for a full
* listing of individual contributors.
*
* This file is part of jpen.
*
* jpen is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* jpen is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with jpen.  If not, see <http://www.gnu.org/licenses/>.
* }] */
#include "../jpen_cFramework_TestType.h"
#include "TestType.h"
/*
 * Class:     jpen_cFramework_TestType
 * Method:    createTestType
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_cFramework_TestType_createTestType
(JNIEnv *pEnv, jobject o){
  return TestType_create();
}

/*
 * Class:     jpen_cFramework_TestType
 * Method:    getTestTypeError
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_cFramework_TestType_getTestTypeError
(JNIEnv *pEnv, jclass class){
  return (*pEnv)->NewStringUTF(pEnv, TestType_row.error);
}

/*
 * Class:     jpen_cFramework_TestType
 * Method:    destroyTestType
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_cFramework_TestType_destroyTestType
(JNIEnv *pEnv, jobject o, jint cellIndex){
  return TestType_destroy(cellIndex);
}

/*
 * Class:     jpen_cFramework_TestType
 * Method:    getUsedSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_cFramework_TestType_getUsedSize
(JNIEnv *pEnv, jclass class){
  return TestType_row.usedSize;
}

/*
 * Class:     jpen_cFramework_TestType
 * Method:    getSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_cFramework_TestType_getSize
(JNIEnv *pEnv, jclass class){
  return TestType_row.size;
}

/*
 * Class:     jpen_cFramework_TestType
 * Method:    getFirstUsedCell
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_cFramework_TestType_getFirstUsedCell
(JNIEnv *pEnv, jclass class){
  return TestType_row.firstUsedCell;
}

/*
 * Class:     jpen_cFramework_TestType
 * Method:    getFirstFreeCell
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_cFramework_TestType_getFirstFreeCell
(JNIEnv *pEnv, jclass class){
  return TestType_row.firstFreeCell;
}

/*
 * Class:     jpen_cFramework_TestType
 * Method:    getNextCell
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_cFramework_TestType_getNextCell
(JNIEnv *pEnv, jclass class, jint cellIndex){
  return TestType_getPCell(cellIndex)->nextCell;
}

/*
 * Class:     jpen_cFramework_TestType
 * Method:    getPrevCell
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_cFramework_TestType_getPrevCell
(JNIEnv *pEnv, jclass class, jint cellIndex){
  return TestType_getPCell(cellIndex)->prevCell;
}
