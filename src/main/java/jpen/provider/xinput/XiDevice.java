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
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PLevel;
import jpen.provider.Utils;

final class XiDevice{
	enum EventType{
		BUTTON_PRESS, BUTTON_RELEASE, MOTION_NOTIFY;
		public static final List<EventType> VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	}

	final int cellIndex;
	final XiBus xiBus;
	final int xiDeviceIndex;

	XiDevice(XiBus xiBus, int cellIndex, int xiDeviceIndex) {
		XinputProvider.loadLibrary();
		this.cellIndex=cellIndex;
		this.xiBus=xiBus;
		this.xiDeviceIndex=xiDeviceIndex;
	}

	public String getName() {
		return xiBus.getXiDeviceName(xiDeviceIndex);
	}
	
	public boolean getIsListening(){
		synchronized(xiBus){ // x server is not thread safe... requests must be made one at a time (per connectionx) 
			return getIsListening(cellIndex);
		}
	}

	private static native boolean getIsListening(int cellIndex);

	public void setIsListening(boolean isListening){
		synchronized(xiBus){
			setIsListening(cellIndex, isListening);
		}
	}

	private static native void setIsListening(int cellIndex, boolean isListening);

	public PLevel.Range getLevelRange(PLevel.Type levelType) {
		synchronized(xiBus){
			int typeIndex=getLevelTypeValueIndex(levelType);
			return new PLevel.Range(getLevelRangeMin(cellIndex, typeIndex), getLevelRangeMax(cellIndex, typeIndex));
		}
	}

	private static int getLevelTypeValueIndex(PLevel.Type levelType){
		return levelType.ordinal();
	}

	private static native int getLevelRangeMin(int cellIndex, int levelTypeOrdinal);
	private static native int getLevelRangeMax(int cellIndex, int levelTypeOrdinal);

	public int getValue(PLevel.Type levelType) {
		synchronized(xiBus){
			return getValue(cellIndex, getLevelTypeValueIndex(levelType));
		}
	}

	private static native int getValue(int cellIndex, int valueIndex);

	@Override
	protected void finalize() {
		synchronized(xiBus){
			if(cellIndex!=-1)
				destroy(cellIndex);
		}
	}
	private static native int destroy(int cellIndex);
	private static native String getError();

	public boolean nextEvent() {
		synchronized(xiBus){
			//if(xiBus.getXiDevice()!=this)
			//throw new IllegalStateException("This device is not the xiBus owner.");
			return nextEvent(cellIndex);
		}
	}

	private static native boolean nextEvent(int cellIndex);

	private synchronized static native void stopWaitingNextEvent(int cellIndex);
	
	public boolean waitNextEvent(){
		synchronized(xiBus){
			return waitNextEvent(cellIndex);
		}
	}
	
	private static native boolean waitNextEvent(int cellIndex);
	
	public void stopWaitingNextEvent(){
			stopWaitingNextEvent(cellIndex);
	}
	
	public long getLastEventTime(){
		synchronized(xiBus){
			return getLastEventTime(cellIndex);
		}
	}

	private static native long getLastEventTime(int cellIndex);

	public EventType getLastEventType() {
		synchronized(xiBus){
			int lastEventTypeOrdinal=getLastEventType(cellIndex);
			if(lastEventTypeOrdinal<0)
				return null;
			return EventType.VALUES.get(lastEventTypeOrdinal);
		}
	}

	private static native int getLastEventType(int cellIndex);

	public int getLastEventButton() {
		synchronized(xiBus){
			return getLastEventButton(cellIndex);
		}
	}

	private static native int getLastEventButton(int cellIndex);

	public void refreshLevelRanges(){
		synchronized(xiBus){
			refreshLevelRanges(cellIndex);
		}
	}

	private static native void refreshLevelRanges(int cellIndex);

	@Override
	public String toString() {
		synchronized(xiBus){
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
			sb.append("}");
			return sb.toString();
		}
	}
}