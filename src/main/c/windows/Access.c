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

static int Access_refreshLc(SAccess *pAccess);
static int Access_queueIsEmpty(SAccess *pAccess);
static void Access_fillPacketQueue(SAccess *pAccess);

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
	pAccess->lc.lcMoveMask = PACKETDATA;// use lcPktData?
	pAccess->lc.lcBtnUpMask = pAccess->lc.lcBtnDnMask;
	pAccess->lc.lcSysMode=FALSE;
	pAccess->ctx=WTOpen(hWnd, &(pAccess->lc), FALSE);
	if(!pAccess->ctx) {
		Access_setError("Couldn't open default context.");
		return errorState;
	}
	
	// assertion:
	if(pAccess->queueSize!=0 || pAccess->queueConsumableIndex!=0){
		Access_setError("assertion failed... pointer to access is not clean.");
		return errorState;
	}
	
	// Set queue size on wintab:
	int wintabQueueSize=MAX_WINTAB_QUEUE_SIZE;
	for(; wintabQueueSize>=MIN_WINTAB_QUEUE_SIZE; wintabQueueSize-=16)
		if(WTQueueSizeSet(pAccess->ctx, wintabQueueSize))
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

int Access_getEnabled(SAccess *pAccess) {
	if(Access_refreshLc(pAccess)){
		Access_appendError(" Couldn't get status.");
		return errorState;
	}
	return !(pAccess->lc.lcStatus&CXS_DISABLED);
}

static int Access_refreshLc(SAccess *pAccess){
	if(!WTGet(pAccess->ctx, &(pAccess->lc))) {
		Access_setError("Couldn't get LOGCONTEXT info.");
		return errorState;
	}
	return cleanState;
}

void Access_setEnabled(SAccess *pAccess, int enabled) {
	if(enabled==Access_getEnabled(pAccess))
		return;
	WTEnable(pAccess->ctx, enabled);
	// flush queue:
	while(WTPacketsGet(pAccess->ctx, 10, NULL))
		;
	pAccess->queueSize=pAccess->queueConsumableIndex=0;
	WTOverlap(pAccess->ctx, enabled);
}

int Access_preDestroy(SAccess *pAccess) {
	if(pAccess->ctx)
		WTClose(pAccess->ctx);
	pAccess->ctx=NULL;
	return cleanState;
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

static void Access_fillPacketQueue(SAccess *pAccess) {
	if(Access_queueIsEmpty(pAccess)) {
		pAccess->queueConsumableIndex=0;
		pAccess->queueSize=WTPacketsGet(pAccess->ctx, QUEUE_SIZE, pAccess->queue);
		//printf("C: new queueSize: %i\n", pAccess->queueSize);
	}
}

static int Access_queueIsEmpty(SAccess *pAccess) {
	return pAccess->queueSize==pAccess->queueConsumableIndex;
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
