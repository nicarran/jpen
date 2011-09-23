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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PLevel;
import jpen.internal.Range;
import jpen.internal.ThreadUtils;

final class XiDevice {
	static final Logger L=Logger.getLogger(XiDevice.class.getName());
	//static { L.setLevel(Level.ALL); }

	public enum EventType {
		BUTTON_PRESS, BUTTON_RELEASE, MOTION_NOTIFY, PROXIMITY_IN, PROXIMITY_OUT;
		public static final List<EventType> VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	}

	final int cellIndex;
	final XiBus xiBus;
	final int xiDeviceIndex;

	XiDevice(XiBus xiBus, int cellIndex, int xiDeviceIndex) {
		this.cellIndex=cellIndex;
		this.xiBus=xiBus;
		this.xiDeviceIndex=xiDeviceIndex;
	}

	public String getName() {
		return xiBus.getXiDeviceName(xiDeviceIndex);
	}

	public boolean getIsListening(){
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			return getIsListening(cellIndex);
			//}
		}
	}

	private static native boolean getIsListening(int cellIndex);

	public void setIsListening(boolean isListening){
		synchronized(xiBus){
			synchronized(XiBus.macrofLock){
				int attempts=0;
				while(true){
					// TODO: change setIsListening to return true on success to avoid doing a getIsListening... this requires a nativeVersion change.
					setIsListening(cellIndex, isListening);
					if(isListening && !getIsListening()){ // the device couldn't be grabbed
						if(attempts++>20){
							L.severe("the tablet device couldn't be grabbed");
							break;
						}
						ThreadUtils.sleepUninterrupted(40);
					}
					else
						break;
				}
			}
		}
	}

	private static native void setIsListening(int cellIndex, boolean isListening);

	public Range getLevelRange(PLevel.Type levelType) {
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			int typeIndex=getLevelTypeValueIndex(levelType);
			return new Range(getLevelRangeMin(cellIndex, typeIndex), getLevelRangeMax(cellIndex, typeIndex));
			//}
		}
	}

	private static int getLevelTypeValueIndex(PLevel.Type levelType){
		return levelType.ordinal();
	}
	private static native int getLevelRangeMin(int cellIndex, int levelTypeOrdinal);
	private static native int getLevelRangeMax(int cellIndex, int levelTypeOrdinal);

	public int getValue(PLevel.Type levelType) {
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			return getValue(cellIndex, getLevelTypeValueIndex(levelType));
			//}
		}
	}

	private static native int getValue(int cellIndex, int valueIndex);

	public boolean nextEvent() {
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			//if(xiBus.getXiDevice()!=this)
			//throw new IllegalStateException("This device is not the xiBus owner.");
			return nextEvent(cellIndex);
			//}
		}
	}

	private static native boolean nextEvent(int cellIndex);

	public boolean waitNextEvent(){
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			return waitNextEvent(cellIndex);
			//}
		}
	}

	private static native boolean waitNextEvent(int cellIndex);

	/**
	This method opens its own X server connection, it must be synchronized against this XiDevice
	*/
	public synchronized void stopWaitingNextEvent(){
		//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
		stopWaitingNextEvent(cellIndex);
		//}
	}

	private static native void stopWaitingNextEvent(int cellIndex);

	public long getLastEventTime(){
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			return getLastEventTime(cellIndex);
			//}
		}
	}

	private static native long getLastEventTime(int cellIndex);

	public EventType getLastEventType() {
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			int lastEventTypeOrdinal=getLastEventType(cellIndex);
			if(lastEventTypeOrdinal<0)
				return null;
			return EventType.VALUES.get(lastEventTypeOrdinal);
			//}
		}
	}

	private static native int getLastEventType(int cellIndex);

	public int getLastEventButton() {
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			return getLastEventButton(cellIndex);
			//}
		}
	}

	private static native int getLastEventButton(int cellIndex);

	public long getLastEventDeviceState(){
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			return getLastEventDeviceState(cellIndex);
			//}
		}
	}

	private static native long getLastEventDeviceState(int cellIndex);

	public boolean getLastEventProximity() {
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			return getLastEventProximity(cellIndex);
			//}
		}
	}

	private static native boolean getLastEventProximity(int cellIndex);

	public void refreshLevelRanges(){
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			refreshLevelRanges(cellIndex);
			//}
		}
	}

	private static native void refreshLevelRanges(int cellIndex);

	public boolean getIsAbsoluteMode(){
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			return getIsAbsoluteMode(cellIndex);
			//}
		}
	}

	private static native boolean getIsAbsoluteMode(int cellIndex);

	@Override
	public String toString() {
		synchronized(xiBus){
			//synchronized(XiBus.macrofLock){ no macrof unsafe alive functions called
			StringBuffer sb=new StringBuffer();
			sb.append("{Device: name=");
			sb.append(getName());
			sb.append(", levelRanges=( ");
			for(PLevel.Type levelType: PLevel.Type.values()) {
				sb.append(levelType);
				sb.append("=");
				sb.append(getLevelRange(levelType));
				sb.append(" ");
			}
			sb.append(") values=(");
			for(PLevel.Type levelType: PLevel.Type.values()) {
				sb.append(levelType);
				sb.append("=");
				sb.append(getValue(levelType));
				sb.append(",");
			}
			sb.append(") , lastEventType=");
			sb.append(getLastEventType());
			sb.append(", lastEventButton=");
			sb.append(getLastEventButton());
			sb.append(", lastEventDeviceState=");
			sb.append(getLastEventDeviceState());
			sb.append(", lastEventProximity=");
			sb.append(getLastEventProximity());
			sb.append("}");
			return sb.toString();
			//}
		}
	}

	@Override
	protected void finalize() {
		synchronized(xiBus){
			synchronized(XiBus.macrofLock){
				if(cellIndex!=-1)
					destroy(cellIndex);
			}
		}
	}
	private static native int destroy(int cellIndex);
	private static native String getError();
}