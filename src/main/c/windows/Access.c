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

static int Access_registerWintabWindowClass();
static int Access_createWintabWindow();
static int Access_initWintab();
static void Access_wintabWindowEventLoop(SAccess *pAccess, JNIEnv *pEnv, jobject object);
static LRESULT CALLBACK Access_wintabWindowEventCallback(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam);
static void Access_callInitEnded(JNIEnv *pEnv, jobject object);
static int Access_nextPacket(SAccess *pAccess);
static int Access_refreshLc(SAccess *pAccess);

int Access_preCreate(SAccess *pAccess) {
	return cleanState;
}

void Access_init(SAccess *pAccess, JNIEnv *pEnv, jobject object){
	pAccess->initialized=true;
	if(Access_registerWintabWindowClass()
		 ||Access_createWintabWindow(pAccess)
		 || Access_initWintab()){
		pAccess->initialized=false;
	}
	
	Access_callInitEnded(pEnv, object);

	if(pAccess->initialized)
		Access_wintabWindowEventLoop(pAccess, pEnv, object);
}

static void Access_callInitEnded(JNIEnv *pEnv, jobject object){
	jclass cls = (*pEnv)->GetObjectClass(pEnv, object);
	jmethodID mid =
		(*pEnv)->GetMethodID(pEnv, cls, "initEnded", "()V");
	if (mid == NULL) {
		printf("couldn't get initEnded method :( \n");
		return;
	}
	(*pEnv)->CallVoidMethod(pEnv, object, mid);
}

static int Access_registerWintabWindowClass(){
	WNDCLASS     wndclass ;
	wndclass.style=0; //CS_HREDRAW | CS_VREDRAW;
	wndclass.lpfnWndProc=Access_wintabWindowEventCallback;
	wndclass.cbClsExtra=0 ;
	wndclass.cbWndExtra=0 ;
	wndclass.hInstance=GetModuleHandle(NULL);
	wndclass.hIcon=NULL; //LoadIcon (NULL, IDI_APPLICATION) ;
	wndclass.hCursor=NULL; //LoadCursor (NULL, IDC_ARROW) ;
	wndclass.hbrBackground=NULL;//(HBRUSH)GetStockObject(WHITE_BRUSH) ;
	wndclass.lpszMenuName=NULL ;
	wndclass.lpszClassName=TEXT(WINTAB_WINDOW_CLASS) ;
	if(!RegisterClass(&wndclass)){
		printf("couldn't register wintab window class, error code %i\n", GetLastError());
		return errorState;
	}
	//printf("wintab window class registered\n");
	return cleanState;
}

static LRESULT CALLBACK Access_wintabWindowEventCallback(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam){
	return DefWindowProc(hWnd, message, wParam, lParam);
}

static int Access_createWintabWindow(SAccess *pAccess){
	pAccess->hWintabWindow = CreateWindow (
				TEXT(WINTAB_WINDOW_CLASS),                   // window class name
				TEXT (WINTAB_WINDOW_NAME), // window caption
				WS_DISABLED, //WS_OVERLAPPEDWINDOW,        // window style
				0,//CW_USEDEFAULT,              // initial x position
				0,//CW_USEDEFAULT,              // initial y position
				0,//CW_USEDEFAULT,              // initial x size
				0,//CW_USEDEFAULT,              // initial y size
				NULL,                       // parent window handle
				NULL,                       // window menu handle
				GetModuleHandle(NULL),                  // program instance handle
				NULL) ;                     // creation parameters
	if (!pAccess->hWintabWindow){
		printf("couldn't create wintab window, error code %i\n", GetLastError());
		return errorState;
	}
	// the window is never shown
	//ShowWindow(pAccess->hWintabWindow, SW_SHOW);
	//UpdateWindow(pAccess->hWintabWindow);
	//printf("wintab window created\n");
	return cleanState;
}

static int Access_initWintab(SAccess *pAccess){
	WTInfo(WTI_DEFCONTEXT , 0, &(pAccess->lc));
	pAccess->device=pAccess->lc.lcDevice;

	strcpy(pAccess->lc.lcName, "JPen Access");
	pAccess->lc.lcOptions |= CXO_SYSTEM;
	pAccess->lc.lcOptions |= CXO_MESSAGES; // to receive window packet messages
	pAccess->lc.lcPktData = PACKETDATA;
	pAccess->lc.lcPktMode = PACKETMODE;
	pAccess->lc.lcMoveMask = PACKETDATA;
	pAccess->lc.lcBtnUpMask = pAccess->lc.lcBtnDnMask;
	pAccess->lc.lcSysMode=FALSE;
	pAccess->ctx=WTOpen(pAccess->hWintabWindow, &(pAccess->lc), FALSE);
	if(!pAccess->ctx) {
		Access_setError("Couldn't open default context.");
		printf("couldn't open wintab context\n");
		return errorState;
	}
	//printf("wintab initiated\n");
	return cleanState;
}

static void Access_wintabWindowEventLoop(SAccess *pAccess, JNIEnv *pEnv, jobject object){
	//printf("entering event loop...\n");

	jclass cls = (*pEnv)->GetObjectClass(pEnv, object);
	jmethodID packetReadyMethodId =
		(*pEnv)->GetMethodID(pEnv, cls, "packetReady", "()V");
	if (packetReadyMethodId == NULL) {
		printf("couldn't get packetReady method :( \n");
		return;
	}

	MSG msg;
	while (GetMessage(&msg, pAccess->hWintabWindow, WT_PACKET, WT_PACKET)>0)
	{
		//TranslateMessage(&msg);
		//DispatchMessage(&msg);
		// WARNING: Wacom's wintab does not fire events inmediately after being generated. It sends two events almost at the same time... this issue invalidates the whole propose of the pull method!!! (tested on Intuos 3)
		// I'm suspecting that the driver does some magic to make appear a hardware 100fps like a 200fps . device.
		if(msg.message==WT_PACKET){
			WTPacket((HCTX)(msg.lParam), msg.wParam, &pAccess->packet); 
			Access_nextPacket(pAccess);
			(*pEnv)->CallVoidMethod(pEnv, object, packetReadyMethodId);
		}
		//else
			//printf("uninteresting event received\n");
	}
	//printf("leaving event loop\n");
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
}

static int Access_refreshLc(SAccess *pAccess){
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
	//printf("flush queue:\n");
	while(WTPacketsGet(pAccess->ctx, 10, NULL))
		;
	WTOverlap(pAccess->ctx, enabled);
}

int Access_preDestroy(SAccess *pAccess) {
	if(pAccess->ctx)
		WTClose(pAccess->ctx);
	pAccess->ctx=NULL;
	return cleanState;
}

static int Access_nextPacket(SAccess *pAccess) {
	PACKET p=pAccess->packet;
	pAccess->valuatorValues[E_Valuators_x]= p.pkX;
	pAccess->valuatorValues[E_Valuators_y]= p.pkY;
	pAccess->valuatorValues[E_Valuators_press]= p.pkNormalPressure;
	pAccess->valuatorValues[E_Valuators_orAzimuth]=p.pkOrientation.orAzimuth;
	pAccess->valuatorValues[E_Valuators_orAltitude]=p.pkOrientation.orAltitude;
	pAccess->cursor=p.pkCursor;
	pAccess->buttons=p.pkButtons;
	pAccess->status=p.pkStatus;
	pAccess->time=p.pkTime;
	return 1;
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
