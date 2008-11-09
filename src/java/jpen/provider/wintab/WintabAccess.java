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
package jpen.provider.wintab;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jpen.PLevel;
import jpen.provider.Utils;

class WintabAccess {
	private static long bootTimeUtc=-1;
	
	/**
	This must be like E_csrTypes enumeration.
	*/
	public enum CursorType{UNDEF, PENTIP, PUCK, PENERASER;
	    public static final List<CursorType> VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	                      }

	private final int cellIndex;

	WintabAccess() throws Exception {
		this.cellIndex=create();
		if(cellIndex==-1)
			throw new Exception(getError());
	}

	private static native int create();
	private static native String getError();

	int getValue(PLevel.Type levelType) {
		// tilt data is really azimuth and altitude and must be transformed!
		return getValue(cellIndex, levelType.ordinal());
	}

	private static native int getValue(int cellIndex, int levelTypeOrdinal);

	public boolean nextPacket() {
		return nextPacket(cellIndex);
	}

	private static native boolean nextPacket(int cellIndex);


	public boolean getEnabled() {
		return getEnabled(cellIndex);
	}

	private static native boolean getEnabled(int cellIndex);

	public void setEnabled(boolean enabled) {
		setEnabled(cellIndex, enabled);
	}

	private static native void setEnabled(int cellIndex, boolean enabled);

	public PLevel.Range getLevelRange(PLevel.Type levelType) {
		int[] minMax=getLevelRange(cellIndex, levelType.ordinal());
		return new PLevel.Range(minMax[0], minMax[1]);
	}

	private static native int[] getLevelRange(int cellIndex, int levelTypeOrdinal);

	public int getCursor() {
		return getCursor(cellIndex);
	}

	private static native int getCursor(int cellIndex);
	
	public long getTime(){
		return getTime(cellIndex);
	}
	
	private static native long getTime(int cellIndex);
	
	public long getTimeUtc(){
		return getBootTimeUtc()+getTime();
	}

	public int getButtons() {
		return getButtons(cellIndex);
	}

	private static native int getButtons(int cellIndex);

	public static CursorType getCursorType(int cursor) {
		return CursorType.VALUES.get(getCursorTypeOrdinal(cursor));
	}

	private static native int getCursorTypeOrdinal(int cursor);

	public int getFirstCursor() {
		return getFirstCursor(cellIndex);
	}

	private static native int getFirstCursor(int cellIndex);

	public int getCursorsCount() {
		return getCursorsCount(cellIndex);
	}

	private static native int getCursorsCount(int cellIndex);

	public static native boolean getCursorActive(int cursor);

	public static native String getCursorName(int cursor);

	public static native long getCursorId(int cursor);

	public static native int getCursorMode(int cursor);

	public String getDeviceName() {
		return getDeviceName(cellIndex);
	}

	private static native String getDeviceName(int cellIndex);

	public static native boolean getDefCtxSysMode();

	public boolean getDDCtxSysMode(){
		return getDDCtxSysMode(cellIndex);
	}

	private static native boolean getDDCtxSysMode(int cellIndex);

	@Override
	protected void finalize() {
		if(cellIndex!=-1)
			destroy(cellIndex);
	}

	private static native int destroy(int cellIndex);

	public static native int[] getButtonMap(int cursor);

	public int getStatus(){
		return getStatus(cellIndex);
	}

	public static native int getStatus(int cellIndex);

	public boolean getTiltExtSupported() {
		return getTiltExtSupported(cellIndex);
	}

	private static native boolean getTiltExtSupported(int cellIndex);


	public boolean getLcSysMode(){
		return getLcSysMode(cellIndex);
	}

	private static native boolean getLcSysMode(int cellIndex);
	
	public long getBootTimeUtc(){
		if(bootTimeUtc==-1)
			bootTimeUtc=getBootTimeUtc(cellIndex);
		return bootTimeUtc;
	}
	
	private static native long getBootTimeUtc(int cellIndex);

	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("WintabAccess:[values=(");
		for(PLevel.Type levelType: PLevel.Type.values()) {
			sb.append(levelType);
			sb.append("=");
			sb.append(getValue(levelType));
			sb.append(",");
		}
		sb.append(") levelRanges=( ");
		for(PLevel.Type levelType: PLevel.Type.values()) {
			sb.append(levelType);
			sb.append("=");
			sb.append(getLevelRange(levelType));
			sb.append(" ");
		}
		sb.append("), cursor=");
		sb.append(getCursor());
		sb.append(", cursorType=");
		sb.append(getCursorType(getCursor()));
		sb.append(", id="+getCursorId(getCursor()));
		sb.append(", buttons=");
		sb.append(getButtons());
		sb.append(", defCtxSysMode="+getDefCtxSysMode());
		sb.append(", DDCtxSysMode="+getDDCtxSysMode());
		sb.append(", lcSysMode="+getLcSysMode());
		sb.append(", status="+getStatus());
		sb.append("]");
		return sb.toString();
	}
}
