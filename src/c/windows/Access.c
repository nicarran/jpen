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
#include "Access.h"

m_implementRow(Access);

/* Taken from PKTDEF.H example. */
#ifdef PACKETTILT
static UINT ScanExts(UINT wTag) {
	UINT i;
	UINT wScanTag;

	/* scan for wTag's info category. */
	for (i = 0; WTInfo(WTI_EXTENSIONS + i, EXT_TAG, &wScanTag); i++) {
		if (wTag == wScanTag) {
			/* return category offset from WTI_EXTENSIONS. */
			return i;
		}
	}
	/* return error code. */
	return 0xFFFF;
}
#endif

int Access_preCreate(SAccess *pAccess) {
	HWND hWnd=GetDesktopWindow();
	if(!hWnd) {
		Access_setError("Couldn't get desktop window.");
		return errorState;
	}

	WTInfo(WTI_DEFCONTEXT , 0, &(pAccess->lc));

	pAccess->device=pAccess->lc.lcDevice;

	strcpy(pAccess->lc.lcName, "JPen Access");
	pAccess->lc.lcOptions |= CXO_SYSTEM;
	pAccess->lc.lcPktData = PACKETDATA;
	pAccess->lc.lcPktMode = PACKETMODE;

	pAccess->tiltExtSupported=false;
#ifdef PACKETTILT
	UINT categoryTILT = ScanExts(WTX_TILT);
	pAccess->tiltExtSupported=categoryTILT!=0xFFFF;
	if(pAccess->tiltExtSupported) {
		WTPKT maskTILT;
		WTInfo(WTI_EXTENSIONS + categoryTILT, EXT_MASK, &maskTILT);
		pAccess->lc.lcPktData |= maskTILT;
#if PACKETTILT == PKEXT_RELATIVE
		pAccess->lc.lcPktMode |= maskTILT;
#endif
#endif

		pAccess->lc.lcMoveMask = PACKETDATA;// use lcPktData?
		pAccess->lc.lcBtnUpMask = pAccess->lc.lcBtnDnMask;
		pAccess->lc.lcSysMode=FALSE;
		pAccess->ctx=WTOpen(hWnd, &(pAccess->lc), FALSE);
		if(!pAccess->ctx) {
			Access_setError("Couldn't open default context.");
			return errorState;
		}

		// Set queue size
		int queueSize=MAX_WINTAB_QUEUE_SIZE;
		for(; queueSize>=MIN_WINTAB_QUEUE_SIZE; queueSize-=16)
			if(WTQueueSizeSet(pAccess->ctx, queueSize))
				break;
		//printf("C: wintab queue size: %i \n",WTQueueSizeGet(pAccess->ctx)); // D

		return cleanState;
	}

	void Access_getValuatorRange(SAccess *pAccess, int valuator, jint *pRange) {
		if(Access_refreshLc(pAccess)){
			Access_appendError(" Couldn't get status.");
		}
		switch(valuator){
		case E_Valuators_x:
			pRange[0]=pRange[1]=pAccess->lc.lcOutOrgX;
			pRange[1]+=pAccess->lc.lcOutExtX;
			break;
		case E_Valuators_y:
			pRange[0]=pRange[1]=pAccess->lc.lcOutOrgY;
			pRange[1]+=pAccess->lc.lcOutExtY;
			break;
		case E_Valuators_press:
			{
				AXIS axis;
				WTInfo(WTI_DEVICES+pAccess->device, DVC_NPRESSURE, &axis);
				pRange[0]=axis.axMin;
				pRange[1]=axis.axMax;
			}
			break;
		default:
			pRange[0]=pRange[1]=0;
		}

		// E_Valuators_orAzimuth, y E_Valuators_orAltitude
		/*AXIS axises[3];
		WTInfo(WTI_DEVICES+pAccess->device, DVC_ORIENTATION, &axises);*/
		// UUPS: I commented this (above) out.  wacom->Intuos3 does not give me real capabilities here... it gives:
		// azimuth: 0 - 3600
		// altitude: 0 - 900
		// -> I dont know how to get the "real" tablet limits using wintab : (
		/*switch(valuator){
		case	E_Valuators_orAzimuth:
			axis=axises[0];
			break;
		case E_Valuators_orAltitude:
			axis=axises[1];
			break;
	}
		pRange[0]=axis.axMin;
		pRange[1]=axis.axMax;*/
	}

	int Access_refreshLc(SAccess *pAccess){
		if(!WTGet(pAccess->ctx, &(pAccess->lc))) {
			Access_setError("Couldn't get LOGCONTEXT info.");
			return errorState;
		}
		return cleanState;
	}

	int Access_getEnabled(SAccess *pAccess) {
		if(Access_refreshLc(pAccess)){
			Access_appendError(" Couldn't get status.");
			return errorState;
		}
		return !(pAccess->lc.lcStatus&CXS_DISABLED);
	}

	void Access_setEnabled(SAccess *pAccess, int enabled) {
		if(enabled==Access_getEnabled(pAccess))
			return;
		WTEnable(pAccess->ctx, enabled);
		// flush queue
		WTPacketsGet( pAccess->ctx, MAX_WINTAB_QUEUE_SIZE, NULL);
		WTOverlap(pAccess->ctx, enabled);
	}

	int Access_preDestroy(SAccess *pAccess) {
		if(pAccess->ctx)
			WTClose(pAccess->ctx);
		pAccess->ctx=NULL;
		return cleanState;
	}

	static int Access_queueIsEmpty(SAccess *pAccess) {
		return pAccess->queueSize==pAccess->queueConsumableIndex;
	}

	static void Access_fillPacketQueue(SAccess *pAccess) {
		if(Access_queueIsEmpty(pAccess)) {
			pAccess->queueConsumableIndex=0;
			pAccess->queueSize=WTPacketsGet(pAccess->ctx, QUEUE_SIZE, pAccess->queue);
			//printf("C: new queueSize: %i\n", pAccess->queueSize);
		}
	}

	int Access_nextPacket(SAccess *pAccess) {
		Access_fillPacketQueue(pAccess);
		if(Access_queueIsEmpty(pAccess))
			return 0;
		PACKET p=pAccess->queue[pAccess->queueConsumableIndex++];
		pAccess->valuatorValues[E_Valuators_x]= p.pkX;
		pAccess->valuatorValues[E_Valuators_y]= p.pkY;
		pAccess->valuatorValues[E_Valuators_press]= p.pkNormalPressure;
		pAccess->valuatorValues[E_Valuators_orAzimuth]=p.pkOrientation.orAzimuth;
		pAccess->valuatorValues[E_Valuators_orAltitude]=p.pkOrientation.orAltitude;
		pAccess->cursor=p.pkCursor;
		pAccess->buttons=p.pkButtons;
		pAccess->status=p.pkStatus;
		pAccess->time=p.pkTime;
		// vvv EXPERIMENTAL
		//printf("pitch=%li, roll=%li, yaw=%li\n", p.pkRotation.roPitch, p.pkRotation.roRoll, p.pkRotation.roYaw);
		//^^^
		return 1;
	}

	UINT Access_getFirstCursor(SAccess *pAccess) {
		UINT r;
		WTInfo(WTI_DEVICES+pAccess->device, DVC_FIRSTCSR, &r);
		return r;
	}

	UINT Access_getCursorsCount(SAccess *pAccess) {
		UINT r;
		WTInfo(WTI_DEVICES+pAccess->device, DVC_NCSRTYPES, &r);
		return r;
	}

	int Access_getCsrType(int cursor) {
		UINT cursorType = 0;
		if (WTInfo( WTI_CURSORS + cursor, CSR_TYPE, &cursorType ) != sizeof(cursorType))
			return E_csrTypes_undef;

		switch (cursorType & CSR_TYPE_GENERAL_MASK) {
		case CSR_TYPE_GENERAL_PENTIP:
			return E_csrTypes_penTip;
		case CSR_TYPE_GENERAL_PUCK:
			return E_csrTypes_puck;
		case CSR_TYPE_GENERAL_PENERASER:
			return E_csrTypes_penEraser;
		}

		return E_csrTypes_undef;
	}

	/**
	Taken from the jdk demo jvmti/hprof/src/windows/hprof_md.c
	*/
	static jlong currentTimeMillis(void)
	{
		static jlong fileTime_1_1_70 = 0;
		SYSTEMTIME st0;
		FILETIME   ft0;

		if (fileTime_1_1_70 == 0) {
			/* Initialize fileTime_1_1_70 -- the Win32 file time of midnight
			* 1/1/70.
			 */ 

			memset(&st0, 0, sizeof(st0));
			st0.wYear  = 1970;
			st0.wMonth = 1;
			st0.wDay   = 1;
			SystemTimeToFileTime(&st0, &ft0);
			fileTime_1_1_70 = FT2JLONG(ft0);
		}

		GetSystemTime(&st0);
		SystemTimeToFileTime(&st0, &ft0);

		return (FT2JLONG(ft0) - fileTime_1_1_70) / 10000;
	}

	jlong Access_getBootTimeUtc(){
		return currentTimeMillis()-GetTickCount();
	}
