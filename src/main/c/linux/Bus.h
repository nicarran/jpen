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
#ifndef Bus_h
#define Bus_h

#include "macros.h"
#include <X11/extensions/XInput.h>
#include <X11/Xatom.h>
#include <sys/time.h>
#include <jni.h>

extern char *xerror;
extern int xerrorHandler(Display *pDisplay, XErrorEvent *pEvent);

struct Bus {
	int cellIndex;
	Display *pDisplay;
	XDeviceInfo *pDeviceInfo;
	int deviceInfoSize;
	int deviceCellIndex;
	int displayConnectionNumber;
};
m_declareRow(Bus);
extern int Bus_setDevice(SBus *pBus, int deviceIndex);
extern int Bus_refreshDeviceInfo(SBus *pBus);
extern void Bus_printXNextRequestSerial(SBus *pBus);
#endif
