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
	private final int cellIndex;
	private XiDevice xiDevice;

	public XiBus() throws Exception {
		this.cellIndex=create();
		if(cellIndex==-1)
			throw new Exception(getError());
	}

	public static native int getNativeBuild();

	private static native int create();
	private static native  String getError();

	public synchronized int getXiDevicesSize() {
		return getDevicesSize(cellIndex);
	}

	private static native int getDevicesSize(int cellIndex);

	public synchronized String getXiDeviceName(int xiDeviceIndex) {
		return getDeviceName(cellIndex, xiDeviceIndex);
	}

	private static native String getDeviceName(int cellIndex, int xiDeviceIndex);

	public synchronized XiDevice getXiDevice() {
		return xiDevice;
	}

	public synchronized void setXiDevice(int xiDeviceIndex) throws Exception {
		if(xiDeviceIndex==-1) {
			xiDevice=null;
			return;
		}
		int xiDeviceCellIndex=setDevice(cellIndex, xiDeviceIndex);
		if(xiDeviceCellIndex<0)
			throw new Exception(getError());
		xiDevice=new XiDevice(this, xiDeviceCellIndex, xiDeviceIndex);
	}

	private static native int setDevice(int cellIndex, int deviceIndex);

	public synchronized void refreshXiDeviceInfo(){
		if(refreshDeviceInfo(cellIndex)!=0)
			throw new IllegalStateException(getError());
	}

	private static native int refreshDeviceInfo(int cellIndex);

	/**
	Prints the next X request serial number on std. out for debugging purposes.
	*/
	synchronized void printXNextRequestSerial(){
		printXNextRequestSerial(cellIndex);
	}

	private static native void printXNextRequestSerial(int cellIndex);

	@Override
	protected synchronized void finalize() {
		if(cellIndex!=-1)
			destroy(cellIndex);
	}
	static native int destroy(int cellIndex);

	@Override
	public String toString() {
		return "{Bus: xiDevice="+xiDevice+"}";
	}
}