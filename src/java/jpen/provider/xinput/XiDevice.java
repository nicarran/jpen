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

import jpen.PLevel;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.provider.Utils;

class XiDevice{
	static{
		Utils.loadLibraryOrFail();
	}

	enum EventType{
		BUTTON_PRESS, BUTTON_RELEASE, MOTION_NOTIFY;
		private static final EventType[] VALUES=EventType.values();
	}

	final int cellIndex;
	final XiBus bus;
	final int deviceIndex;

	XiDevice(XiBus bus, int cellIndex, int deviceIndex) {
		this.bus=bus;
		this.cellIndex=cellIndex;
		this.deviceIndex=deviceIndex;
	}

	public String getName() {
		return bus.getDeviceName(deviceIndex);
	}

	public PLevel.Range getLevelRange(PLevel.Type levelType) {
		return new PLevel.Range(getLevelRangeMin(cellIndex, levelType.ordinal()), getLevelRangeMax(cellIndex, levelType.ordinal()));
	}

	private static native int getLevelRangeMin(int cellIndex, int levelTypeOrdinal);
	private static native int getLevelRangeMax(int cellIndex, int levelTypeOrdinal);

	public int getValue(PLevel.Type levelType) {
		return getValue(cellIndex, levelType.ordinal());
	}

	private static native int getValue(int cellIndex, int dataTypeOrdinal);

	@Override
	protected void finalize() {
		if(cellIndex!=-1)
			destroy(cellIndex);
	}
	private static native int destroy(int cellIndex);
	private static native String getError();

	public boolean nextEvent() {
		if(bus.getDevice()!=this)
			throw new IllegalStateException("This device is not the bus owner.");
		return nextEvent(cellIndex);
	}

	private static native boolean nextEvent(int cellIndex);

	public EventType getLastEventType() {
		int lastEventTypeOrdinal=getLastEventType(cellIndex);
		if(lastEventTypeOrdinal<0)
			return null;
		return EventType.VALUES[lastEventTypeOrdinal];
	}

	private static native int getLastEventType(int cellIndex);

	public int getLastEventButton() {
		return getLastEventButton(cellIndex);
	}

	private static native int getLastEventButton(int cellIndex);

	@Override
	public String toString() {
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
