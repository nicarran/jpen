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
package jpen.provider.xinput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jpen.PenManager;
import jpen.provider.Utils;

class XiBus {
	static final Object XLIB_LOCK=new Object();

	private final int cellIndex;
	private XiDevice xiDevice;
	private static long bootTimeUtc=-1;

	XiBus() throws Exception {
		synchronized(XLIB_LOCK){
			XinputProvider.loadLibrary();
			this.cellIndex=create();
			if(cellIndex==-1)
				throw new Exception(getError());
		}
	}

	private static native int create();
	private static native  String getError();

	public int getXiDevicesSize() {
		synchronized(XLIB_LOCK){
			return getDevicesSize(cellIndex);
		}
	}

	private static native int getDevicesSize(int cellIndex);

	public String getXiDeviceName(int xiDeviceIndex) {
		synchronized(XLIB_LOCK){
			return getDeviceName(cellIndex, xiDeviceIndex);
		}
	}

	private static native String getDeviceName(int cellIndex, int xiDeviceIndex);

	public XiDevice getXiDevice() {
		synchronized(XLIB_LOCK){
			return xiDevice;
		}
	}

	public void setXiDevice(int xiDeviceIndex) throws Exception {
		synchronized(XLIB_LOCK){
			if(xiDeviceIndex==-1) {
				xiDevice=null;
				return;
			}
			int xiDeviceCellIndex=setDevice(cellIndex, xiDeviceIndex);
			if(xiDeviceCellIndex<0)
				throw new Exception(getError());
			xiDevice=new XiDevice(this, xiDeviceCellIndex, xiDeviceIndex);
		}
	}

	private static native int setDevice(int cellIndex, int deviceIndex);

	public void refreshXiDeviceInfo(){
		synchronized(XLIB_LOCK){
			if(refreshDeviceInfo(cellIndex)!=0)
				throw new IllegalStateException(getError());
		}
	}

	private static native int refreshDeviceInfo(int cellIndex);

	public long getBootTimeUtc(){
		synchronized(XLIB_LOCK){
			if(bootTimeUtc==-1)
				bootTimeUtc=getBootTimeUtc(cellIndex);
			return bootTimeUtc;
		}
	}

	long getBootTimeUtcNotCached(){
		return getBootTimeUtc(cellIndex);
	}

	private static native long getBootTimeUtc(int cellIndex);

	/*public long getBootTimeUtc(){
		if(bootTimeUtc==-1)
			bootTimeUtc=System.currentTimeMillis()-getCurrentServerTime();
		return bootTimeUtc;
}*/


	@Override
	protected void finalize() {
		synchronized(XLIB_LOCK){
			if(cellIndex!=-1)
				destroy(cellIndex);
		}
	}
	static native int destroy(int cellIndex);

	@Override
	public String toString() {
		return "{Bus: xiDevice="+xiDevice+"}";
	}
}
