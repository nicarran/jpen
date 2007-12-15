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
#include "Access.h"

m_implementRow(Access);

int Access_preCreate(SAccess *pAccess) {
	HWND hWnd=GetDesktopWindow();
	if(!hWnd) {
		Access_setError("Couldn't get desktop window.");
		return errorState;
	}

	LOGCONTEXT logCtx;
	WTInfo(WTI_DEFCONTEXT , 0, &logCtx);

	pAccess->device=logCtx.lcDevice;

	strcpy(logCtx.lcName, "nicarran.pointer Access");
	logCtx.lcPktData = PACKETDATA;
	logCtx.lcPktMode = PACKETMODE;
	logCtx.lcMoveMask = PACKETDATA;
	logCtx.lcBtnUpMask = logCtx.lcBtnDnMask;
	pAccess->ctx=WTOpen(hWnd, &logCtx, FALSE);
	if(!pAccess->ctx) {
		Access_setError("Couldn't open default context.");
		return errorState;
	}
	return cleanState;
}

/* must match E_Valuators enumeration */
static UINT axisIndexes[]={
                            DVC_X,
                            DVC_Y,
                            DVC_NPRESSURE,
                          };

void Access_getValuatorRange(SAccess *pAccess, int valuator, jint *pRange) {
	AXIS axis;
	WTInfo(WTI_DEVICES+pAccess->device, axisIndexes[valuator], &axis);
	pRange[0]=axis.axMin;
	pRange[1]=axis.axMax;
}

int Access_getEnabled(SAccess *pAccess) {
	LOGCONTEXT logCtx;
	if(!WTGet(pAccess->ctx, &logCtx)) {
		Access_setError("Couldnt get LOGCONTEXT info.");
		return errorState;
	}
	return !(logCtx.lcStatus&CXS_DISABLED);
}

void Access_setEnabled(SAccess *pAccess, int enabled) {
	if(enabled==Access_getEnabled(pAccess))
		return;
	WTEnable(pAccess->ctx, enabled);
}

int Access_preDestroy(SAccess *pAccess) {
	if(pAccess->ctx)
		WTClose(pAccess->ctx);
	pAccess->ctx=NULL;
	return cleanState;
}

int Access_nextPacket(SAccess *pAccess) {
	PACKET p;
	if (WTPacketsGet(pAccess->ctx, 1, &p)) {
		pAccess->valuatorValues[E_Valuators_x]= p.pkX;
		pAccess->valuatorValues[E_Valuators_y]= p.pkY;
		// ToDo: p.pkOrientation.orAzimuth, p.pkOrientation.orAltitude, p.pkZ
		pAccess->valuatorValues[E_Valuators_press]= p.pkNormalPressure;
		pAccess->cursor=p.pkCursor;
		pAccess->buttons=p.pkButtons;
		return 1;
	}
	return 0;
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

BOOL Access_getCursorActive(int cursor) {
	BOOL r;
	WTInfo( WTI_CURSORS + cursor, CSR_ACTIVE, &r );
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
