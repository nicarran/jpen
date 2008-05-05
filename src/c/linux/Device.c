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
#include "Device.h"
#include "Bus.h"

m_implementRow(Device);

int Device_preCreate(SDevice *pDevice) {
	return 0;
}

static int EventClassAndOffsets[][2]={
                                       {ButtonClass,_deviceButtonPress},
                                       {ButtonClass,_deviceButtonRelease},
                                       {ValuatorClass, _deviceMotionNotify},
                                     };

static void Device_setListening(struct Device *pDevice, int listening) {
	struct Bus *pBus=Bus_getP(pDevice->busCellIndex);
	XEventClass eventClasses[E_EventType_size];
	int eventClassesSize=0;
	if(listening) {
		int type;
		int classId, offset;
		XEventClass eventClass;
		int i=E_EventType_size;
		while(--i>=0) {
			classId=EventClassAndOffsets[i][0];
			offset= EventClassAndOffsets[i][1];
			FindTypeAndClass(pDevice->pXdevice, type, eventClass, classId, offset);
			if(eventClass) {
				eventClasses[eventClassesSize++]=eventClass;
				pDevice->eventTypeIds[i]=type;
			}
		}
	}
	XSelectExtensionEvent(pBus->pDisplay,
	                      DefaultRootWindow(pBus->pDisplay),
	                      eventClasses,
	                      eventClassesSize);
}

int Device_preDestroy(SDevice *pDevice) {
	SBus *pBus=Bus_getP(pDevice->busCellIndex);
	if(pDevice->pXdevice) { // may be uninitialized
		Device_setListening(pDevice, false);
		XCloseDevice(pBus->pDisplay, pDevice->pXdevice);
	}
	return 0;
}

void Device_refreshValuatorRanges(struct Device *pDevice){
	SBus *pBus=Bus_getP(pDevice->busCellIndex);
	XDeviceInfo deviceInfo=pBus->pDeviceInfo[pDevice->index];
	XAnyClassPtr pAnyClassInfo = deviceInfo.inputclassinfo;
	int j=deviceInfo.num_classes;
	while(--j>=0) {
		if(pAnyClassInfo->class==ValuatorClass) {
			XValuatorInfo *pValuatorInfo=(XValuatorInfo *) pAnyClassInfo;
			int i=0;
			for(;i<=pValuatorInfo->num_axes; i++) {
				if(i==E_Valuators_size)
					break;
				pDevice->valuatorRangeMins[i]=pValuatorInfo->axes[i].min_value;
				pDevice->valuatorRangeMaxs[i]=pValuatorInfo->axes[i].max_value;
			}
			break;
		}
		/*case ButtonClass: {
				XButtonInfo *pButtonInfo=(XButtonInfo *)pAnyClassInfo;
				pDevice->buttonsSize=pButtonInfo->num_buttons;
				if(pDevice->buttonsSize>MAX_BUTTONS)
					pDevice->buttonsSize=MAX_BUTTONS;
			}*/
		pAnyClassInfo=(XAnyClassPtr)((char*)pAnyClassInfo+pAnyClassInfo->length);
	}
}

int Device_init(SDevice *pDevice, SBus *pBus, int deviceIndex) {
	pDevice->busCellIndex=pBus->cellIndex;
	pDevice->index=deviceIndex;
	XDeviceInfo deviceInfo=pBus->pDeviceInfo[pDevice->index];
	if(deviceInfo.use!=IsXExtensionDevice) { // TODO: cover also IsExtensionPointer... new in XInput
		Device_setError("Not an X extension device.");
		return errorState;
	}
	if(XInternAtom(pBus->pDisplay, XI_MOUSE, 0)==deviceInfo.type) {
		Device_setError("Mouse not supported as device.");
		return errorState;
	}
	pDevice->pXdevice=XOpenDevice(pBus->pDisplay, deviceInfo.id);
	if(!pDevice->pXdevice) {
		Device_setError("Couldn't open the device.");
		Device_appendError(xerror);
		return errorState;
	}
	
	Device_refreshValuatorRanges(pDevice);
	Device_setListening(pDevice, true);
	return 0;
}

static void Device_refreshValuatorValues(struct Device *pDevice, char first_axis, char axis_count, int *axisData) {
	register int i=first_axis;
	for(; i<axis_count; i++) {
		if(i==E_Valuators_size)
			break;
		pDevice->valuatorValues[i]=axisData[i];
	}
}

/*static Bool Device_consumeEvent(Display *pDisplay, XEvent *pEvent, char *arg) {
	return True;
}*/

/**
@return 1 if an event was received, 0 otherwise.
*/
int Device_nextEvent(struct Device *pDevice ) {
	struct Bus *pBus=Bus_getP(pDevice->busCellIndex);
	//XNextEvent(pBus->pDisplay, &pDevice->event);
	//XCheckIfEvent(Bus_getP(pDevice->busCellIndex)->pDisplay, &pDevice->event, &Device_consumeEvent, 0));
	pDevice->lastEventType=-1;
	register int i;
	for(i=0; i<E_EventType_size; i++) {
		if(XCheckTypedEvent(pBus->pDisplay,
		                    pDevice->eventTypeIds[i],
		                    &pDevice->event)) {
			/*if(pDevice->eventTypeIds[i]==pDevice->event.type) {*/
			pDevice->lastEventType=i;
			switch(i) {
			case E_EventType_ButtonPress:
			case E_EventType_ButtonRelease: {
					XDeviceButtonEvent *pBEvent = (XDeviceButtonEvent*)&pDevice->event;
					pDevice->lastEventButton=pBEvent->button;
					//Device_refreshValuatorValues(pDevice, pBEvent->first_axis, pBEvent->axes_count, pBEvent->axis_data);
				}
				break;
			case E_EventType_MotionNotify: {
					XDeviceMotionEvent *pMEvent=(XDeviceMotionEvent *) &pDevice->event;
					Device_refreshValuatorValues(pDevice, pMEvent->first_axis, pMEvent->axes_count, pMEvent->axis_data);
				}
				break;
			default:
				printf("unhandled event!!\n");
			}
			return true;
		}
	}
	return false;
}
