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

final class XiBus {
	/**
	To be thread-safe against the c/utils/macro.c-h framework we synchronize against {@code macrofLock} (private lock). To be thread-safe against the X-Server we synchronize against the XiBus instance (public lock):
	<ul>
	<li>The X server is not thread safe, requests must be made one at a time (per connection). Each XiBus instance holds one X server connection.
	<li>The c/utils/macro.c-h framework is not thread safe, its functions must be serialized (per class-row).
	</ul>

	Sometimes is needed to synchronize against the X-Server but not against the macro.c/f framework (1), sometimes is needed to synchronize against the macro.c/f framework alone (2), and sometimes is needed to synchronize against both (3). The locks must be acquired in this order: XiBus instance first (public lock), {@code macrofLock} later. Trying to hold XiBus instance lock after holding macrofLock will cause dead-locks.
	*/
	static final Object macrofLock=new Object();

	private final int cellIndex;
	private XiDevice xiDevice;

	public XiBus() throws Exception {
		synchronized(macrofLock){
		this.cellIndex=create();
		if(cellIndex==-1)
			throw new Exception(getError());
		}
	}

	private static native int create();
	private static native  String getError();

	/**
	This method does not use functions of the macro.c-h framework and does not use the X server connection, then it is safe to call outside the locks.
	*/
	public static native int getNativeBuild();

	public synchronized  int getXiDevicesSize() {
		return getDevicesSize(cellIndex);
	}

	private static native int getDevicesSize(int cellIndex);

	public synchronized String getXiDeviceName(int xiDeviceIndex) {
		//synchronized(macrofLock){ no macrof unsafe alive functions called
		return getDeviceName(cellIndex, xiDeviceIndex);
		//}
	}

	private static native String getDeviceName(int cellIndex, int xiDeviceIndex);

	public synchronized XiDevice getXiDevice() {
		return xiDevice;
	}

	public synchronized void setXiDevice(int xiDeviceIndex) throws Exception {
		synchronized(macrofLock){
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

	/**
	This method creates the new XiDevice cell.
	*/
	private static native int setDevice(int cellIndex, int deviceIndex);

	public synchronized void refreshXiDeviceInfo(){
		synchronized(macrofLock){
		if(refreshDeviceInfo(cellIndex)!=0)
			throw new IllegalStateException(getError());
		}
	}

	private static native int refreshDeviceInfo(int cellIndex);

	/**
	Prints the next X request serial number on std. out for debugging purposes.
	*/
	synchronized void printXNextRequestSerial(){
		//synchronized(macrofLock){ no macrof unsafe functions called
		printXNextRequestSerial(cellIndex);
		//}
	}

	private static native void printXNextRequestSerial(int cellIndex);

	@Override
	protected synchronized void finalize() {
		synchronized(macrofLock){
		if(cellIndex!=-1)
			destroy(cellIndex);
		}
	}
	static native int destroy(int cellIndex);

	@Override
	public synchronized String toString() {
		return "{Bus: xiDevice="+xiDevice+"}";
	}
}