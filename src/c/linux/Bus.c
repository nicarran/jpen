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
#include "Bus.h"
#include "Device.h"

char *xerror;

int xerrorHandler(Display *pDisplay, XErrorEvent *pEvent) {
	char chBuf[128];
	XGetErrorText(pDisplay, pEvent->error_code, chBuf, sizeof(chBuf));
	m_newstr(xerror, "X error: ");
	m_concat(xerror, chBuf);
	printf("--- %s\n", xerror);
	return cleanState;
}

m_implementRow(Bus);

int Bus_preCreate(SBus *pBus) {
	XSetErrorHandler(&xerrorHandler);
	pBus->pDisplay = XOpenDisplay(NULL);
	if (!pBus->pDisplay) {
		Bus_setError("Failed to connect to X server; ");
		Bus_appendError(xerror);
		return errorState;
	}
	//XSynchronize(pBus->pDisplay,1);
	int iMajor, iFEV, iFER;
	if (!XQueryExtension(pBus->pDisplay,INAME,&iMajor,&iFEV,&iFER)) {
		Bus_setError("Server does not support XInput extension.");
		XCloseDisplay(pBus->pDisplay);
		return errorState;
	}
	
	return Bus_refreshDeviceInfo(pBus);
}

int Bus_refreshDeviceInfo(SBus *pBus){
	if(pBus->pDeviceInfo)
		XFreeDeviceList(pBus->pDeviceInfo);
	pBus->pDeviceInfo = XListInputDevices(pBus->pDisplay, &pBus->deviceInfoSize);
	if (!pBus->pDeviceInfo) {
		Bus_setError("Failed to get input device information; ");
		Bus_appendError(xerror);
		XCloseDisplay(pBus->pDisplay);
		return errorState;
	}
	return cleanState;
}

/**
Relations and entire structure map freeing is done by the java side finalization mechanism. Is important to reflect all relations in the java side to avoid garbage collection of live structures.
*/
int Bus_preDestroy(SBus *pBus) {
	XCloseDisplay(pBus->pDisplay);
	XFreeDeviceList(pBus->pDeviceInfo);
	return cleanState;
}

/**
remember to destroy the current device on the java side when setting this.
*/
int Bus_setDevice(SBus *pBus, int deviceIndex) {
	int deviceCellIndex=Device_create();
	if(deviceCellIndex==errorState) {
		Bus_setError(Device_row.error);
		return errorState;
	}
	SDevice *pDevice=Device_getP(deviceCellIndex);
	if(Device_init(pDevice, pBus, deviceIndex)==errorState) {
		Bus_setError("Device init failed; ");
		Bus_appendError(Device_row.error);
		if(Device_destroy(deviceCellIndex))
			Bus_appendError(Device_row.error);
		return errorState;
	}
	return pBus->deviceCellIndex=deviceCellIndex;
}
