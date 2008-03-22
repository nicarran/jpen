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
#ifndef Access_h
#define Access_h
#include "macros.h"
#include <jni.h>
#include <windows.h>
#include "INCLUDE/WINTAB.H"
//#ifdef USE_X_LIB
//#include "INCLUDE/WINTABX.h"
//#endif
#define PACKETDATA	(PK_STATUS | PK_X | PK_Y | PK_NORMAL_PRESSURE | PK_CURSOR | PK_BUTTONS | PK_ORIENTATION)
#define PACKETMODE	0
// #define PACKETTILT PKEXT_ABSOLUTE // TILT extension is not widely implemented (wacom), so I disable it and use pkOrientation to get tilt data 
#include "INCLUDE/PKTDEF.h"

//vvv Taken from csrmaskex wacom example.
// cellosoft.jtablet is also a good example. Thanks marcello (cellosoft)!!
#define CSR_TYPE_GENERAL_MASK			( ( UINT ) 0xC000 )
#define CSR_TYPE_GENERAL_PENTIP		( ( UINT ) 0x4000 )
#define CSR_TYPE_GENERAL_PUCK			( ( UINT ) 0x8000 )
#define CSR_TYPE_GENERAL_PENERASER	( ( UINT ) 0xC000 )

// The CSR_TYPE WTInfo data item is new to Wintab 1.2 and is not defined
// in the Wintab 1.26 SDK, so we have to define it.
#ifndef CSR_TYPE
#	define CSR_TYPE 20
#endif
//^^^

#define MAX_WINTAB_QUEUE_SIZE 64
#define MIN_WINTAB_QUEUE_SIZE 16
#define QUEUE_SIZE 16

/* This must be like PLevel.Type enumeration: */
enum{
	E_Valuators_x,
	E_Valuators_y,
	E_Valuators_press,
	E_Valuators_orAzimuth,
	E_Valuators_orAltitude,
	E_Valuators_size,
};

enum{
	E_csrTypes_undef,
	E_csrTypes_penTip,
	E_csrTypes_puck,
	E_csrTypes_penEraser,
};
struct Access {
	int cellIndex;
	HCTX ctx;
	UINT device;
	int enabled;
	int valuatorValues[E_Valuators_size];
	PACKET queue[QUEUE_SIZE]; // buffer to save some WTPacketsGet calls
	int queueSize;
	int queueConsumableIndex;
	UINT cursor;
	DWORD buttons;
	UINT status;
	int tiltExtSupported; // not used if not def PACKETTILT (see above)
};
m_declareRow(Access);
extern int Access_nextPacket(SAccess *pAccess);
extern int Access_getEnabled(SAccess *pAccess);
extern void Access_setEnabled(SAccess *pAccess, int enabled);
extern void Access_getValuatorRange(SAccess *pAccess, int valuator, jint *pRange);
extern int Access_getCsrType(int cursor);
extern UINT Access_getFirstCursor(SAccess *pAccess);
extern UINT Access_getCursorsCount(SAccess *pAccess);
//extern static int Access_fillPacketQueue(SAccess *pAccess);

#endif
