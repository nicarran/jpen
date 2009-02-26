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

void Device_setIsListening(SDevice *pDevice, int isListening) {
	if(pDevice->isListening==isListening)
		return;
	struct Bus *pBus=Bus_getP(pDevice->busCellIndex);
	if(isListening) {
		XEventClass eventClasses[E_EventType_size];
		int eventClassesSize=0;
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

		/* Better grab the device to avoid loosing events
		XSelectInput(pBus->pDisplay,DefaultRootWindow(pBus->pDisplay),0x00FFFFFF ^ PointerMotionHintMask);
		XSelectExtensionEvent(pBus->pDisplay,
		    DefaultRootWindow(pBus->pDisplay),
		    eventClasses,
		    eventClassesSize);*/

		XGrabDevice(pBus->pDisplay, pDevice->pXdevice, DefaultRootWindow(pBus->pDisplay),
		            0,
		            eventClassesSize,
		            eventClasses,
		            GrabModeAsync,
		            GrabModeAsync,
		            CurrentTime
		           );
	}else{
		XUngrabDevice(pBus->pDisplay, pDevice->pXdevice, CurrentTime);
		XSync(pBus->pDisplay, 1);
	}
	// TODO: error handling?
	pDevice->isListening=isListening;
}

int Device_preDestroy(SDevice *pDevice) {
	SBus *pBus=Bus_getP(pDevice->busCellIndex);
	if(pDevice->pXdevice) { // may be uninitialized
		Device_setIsListening(pDevice, false);
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
			for(;i<pValuatorInfo->num_axes; i++) {
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

static int Device_getNumAxes(struct Device *pDevice){
	SBus *pBus=Bus_getP(pDevice->busCellIndex);
	XDeviceInfo deviceInfo=pBus->pDeviceInfo[pDevice->index];
	XAnyClassPtr pAnyClassInfo = deviceInfo.inputclassinfo;
	int j=deviceInfo.num_classes;
	while(--j>=0) {
		if(pAnyClassInfo->class==ValuatorClass) {
			XValuatorInfo *pValuatorInfo=(XValuatorInfo *) pAnyClassInfo;
			return pValuatorInfo->num_axes;
		}
		pAnyClassInfo=(XAnyClassPtr)((char*)pAnyClassInfo+pAnyClassInfo->length);
	}
	return 0;
}

int Device_init(SDevice *pDevice, SBus *pBus, int deviceIndex) {
	pDevice->busCellIndex=pBus->cellIndex;
	pDevice->index=deviceIndex;
	XDeviceInfo deviceInfo=pBus->pDeviceInfo[pDevice->index];
	if(deviceInfo.use<IsXExtensionDevice) { // 4 is isXExtensionPointer, 3 is isXExtensionKeyboard; these are a new  (since 2006/07/18) inputproto 1.4.1 (xorg 7.3 includes inputproto 1.4.2.1). (http://gitweb.freedesktop.org/?p=xorg/proto/inputproto.git;a=summary). Warning: sometimes isXExtensionKeyboard is a tablet device : S
		Device_setError("Not an X extension device.");
		return errorState;
	}
	if(XInternAtom(pBus->pDisplay, XI_MOUSE, 0)==deviceInfo.type) {
		Device_setError("Mouse not supported as device.");
		return errorState;
	}
	if(Device_getNumAxes(pDevice)<3){
		Device_setError("Not enough axis data on device."); // TODO: change this criteria when supporting tablet buttons. 
		return errorState;
	}
	
	pDevice->pXdevice=XOpenDevice(pBus->pDisplay, deviceInfo.id);
	if(!pDevice->pXdevice) {
		Device_setError("Couldn't open the device.");
		Device_appendError(xerror);
		return errorState;
	}
	Device_refreshValuatorRanges(pDevice);
	//Device_setIsListening(pDevice, true);
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

/**
@return 1 if an event was received, 0 otherwise.
*/
int Device_nextEvent(struct Device *pDevice ) {
	struct Bus *pBus=Bus_getP(pDevice->busCellIndex);
	register int i;
	pDevice->lastEventType=-1;
	pDevice->lastEventTime=-1;
	for(i=E_EventType_size; --i>=0;) { // check all event types to get only the first (in time) event
		if(XCheckTypedEvent(pBus->pDisplay,
		        pDevice->eventTypeIds[i],
		        &pDevice->event)) {
			switch(i) {
			case E_EventType_ButtonPress:
			case E_EventType_ButtonRelease:
				{
					XDeviceButtonEvent *pEvent = (XDeviceButtonEvent *)&pDevice->event;
					pDevice->eventTime=pEvent->time;
				}
				break;
			case E_EventType_MotionNotify:
				{
					XDeviceMotionEvent *pEvent=(XDeviceMotionEvent *)&pDevice->event;
					pDevice->eventTime=pEvent->time;
				}
				break;
			default:
				printf("unhandled event!!\n");
			}
			//printf(" event timestamp: %li \n", pDevice->eventTime);
			if(pDevice->lastEventTime==-1 || pDevice->eventTime<pDevice->lastEventTime){
				pDevice->lastEventType=i;
				if(pDevice->lastEventTime!=-1)
					XPutBackEvent(pBus->pDisplay, &pDevice->lastEvent);
				pDevice->lastEventTime=pDevice->eventTime;
				pDevice->lastEvent=pDevice->event;
			}else{
				XPutBackEvent(pBus->pDisplay, &pDevice->event);
			}
		}
	}
	if(pDevice->lastEventType==-1)
		return false;
	switch(pDevice->lastEventType){
	case E_EventType_ButtonPress:
	case E_EventType_ButtonRelease:
		{
			XDeviceButtonEvent *pEvent = (XDeviceButtonEvent *)&pDevice->lastEvent;
			pDevice->lastEventButton=pEvent->button;
		}
		break;
	case E_EventType_MotionNotify:
		{
			XDeviceMotionEvent *pEvent=(XDeviceMotionEvent *)&pDevice->lastEvent;
			Device_refreshValuatorValues(pDevice, pEvent->first_axis, pEvent->axes_count, pEvent->axis_data);
		}
		break;
	}
	return true;
}
