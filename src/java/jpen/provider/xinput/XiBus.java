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
package jpen.provider.xinput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jpen.PenManager;
import jpen.provider.Utils;

class XiBus {
	static{
		Utils.loadLibraryOrFail();
	}
	private final int cellIndex;
	private XiDevice device;

	XiBus() throws Exception {
		this.cellIndex=create();
		if(cellIndex==-1)
			throw new Exception(getError());
	}

	private static native int create();
	private static native  String getError();

	public int getDevicesSize() {
		return getDevicesSize(cellIndex);
	}

	private static native int getDevicesSize(int cellIndex);

	public String getDeviceName(int deviceIndex) {
		return getDeviceName(cellIndex, deviceIndex);
	}

	private static native String getDeviceName(int cellIndex, int deviceIndex);

	public XiDevice getDevice() {
		return device;
	}

	public void setDevice(int deviceIndex) throws Exception {
		if(deviceIndex==-1) {
			device=null;
			return;
		}
		int deviceCellIndex=setDevice(cellIndex, deviceIndex);
		if(deviceCellIndex<0)
			throw new Exception(getError());
		device=new XiDevice(this, deviceCellIndex, deviceIndex);
	}

	private static native int setDevice(int cellIndex, int deviceIndex);
	
	public void refreshDeviceInfo(){
		if(refreshDeviceInfo(cellIndex)!=0)
			throw new IllegalStateException(getError());
	}
	
	private static native int refreshDeviceInfo(int cellIndex);


	@Override
	protected void finalize() {
		if(cellIndex!=-1)
			destroy(cellIndex);
	}
	static native int destroy(int cellIndex);
	
	@Override
	public String toString() {
		return "{Bus: device="+device+"}";
	}
}
