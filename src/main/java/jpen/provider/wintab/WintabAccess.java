/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>
Copyright 2009 Marcello Bastea-Forte <marcello at cellosoft.com>

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
package jpen.provider.wintab;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jpen.PLevel;
import jpen.internal.Range;

final class WintabAccess {
	private static final Object LOCK=new Object();

	/**
	This must correspond to Access.h: E_csrTypes enumeration.
	*/
	public enum CursorType{UNDEF, PENTIP, PUCK, PENERASER;
	    public static final List<CursorType> VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	                      }

	private final int cellIndex;
	
	public WintabAccess() throws Exception {
		synchronized(LOCK){
			this.cellIndex=create();
			if(cellIndex==-1)
				throw new Exception(getError());
		}
	}

	private static native int create();
	private static native String getError();
	
	public static native int getNativeBuild();

	public static final int LEVEL_TYPE_X = 0;
	public static final int LEVEL_TYPE_Y = 1;
	public static final int LEVEL_TYPE_PRESSURE = 2;
	public static final int LEVEL_TYPE_TILT_AZIMUTH = 3;
	public static final int LEVEL_TYPE_TILT_ALTITUDE = 4;
	public static final int LEVEL_TYPE_SIDE_PRESSURE = 5;
	public static final int LEVEL_TYPE_ROTATION = 6;
	
	
	public static final int BUTTON1_MASK = 1<<0;
	public static final int BUTTON2_MASK = 1<<1;
	public static final int BUTTON3_MASK = 1<<2;
	
	// from WINTAB.h
	public static final int PK_CONTEXT				= 0x0001;	/* reporting context */
	public static final int PK_STATUS				= 0x0002;	/* status bits */
	public static final int PK_TIME					= 0x0004;	/* time stamp */
	public static final int PK_CHANGED				= 0x0008;	/* change bit vector */
	public static final int PK_SERIAL_NUMBER		= 0x0010;	/* packet serial number */
	public static final int PK_CURSOR				= 0x0020;	/* reporting cursor */
	public static final int PK_BUTTONS				= 0x0040;	/* button information */
	public static final int PK_X					= 0x0080;	/* x axis */
	public static final int PK_Y					= 0x0100;	/* y axis */
	public static final int PK_Z					= 0x0200;	/* z axis */
	public static final int PK_NORMAL_PRESSURE		= 0x0400;	/* normal or tip pressure */
	public static final int PK_TANGENT_PRESSURE		= 0x0800;	/* tangential or barrel pressure */
	public static final int PK_ORIENTATION			= 0x1000;	/* orientation info: tilts */
	public static final int PK_ROTATION				= 0x2000;	/* rotation info; 1.1 */

	/* unit specifiers (from wintab.h) */
	public static final int TU_NONE			= 0;
	public static final int TU_INCHES		= 1;
	public static final int TU_CENTIMETERS	= 2;
	public static final int TU_CIRCLE		= 3;
	

	/* hardware capabilities flags */
	public static final int HWC_INTEGRATED		= 0x0001;
	public static final int HWC_TOUCH			= 0x0002;
	public static final int HWC_HARDPROX		= 0x0004;
	public static final int HWC_PHYSID_CURSORS	= 0x0008; /* 1.1 */

	public int getValue(PLevel.Type levelType) {
		return getValue(getLevelTypeValueIndex(levelType));
	}
	public int getValue(int type) {
		synchronized(LOCK){
			// tilt data is really azimuth and altitude and must be transformed!
			return getValue(cellIndex, type);
		}
	}


	private static native int getValue(int cellIndex, int valueIndex);

	public boolean nextPacket() {
		synchronized(LOCK){
			return nextPacket(cellIndex);
		}
	}

	private static native boolean nextPacket(int cellIndex);


	public boolean getEnabled() {
		synchronized(LOCK){
			return getEnabled(cellIndex);
		}
	}

	private static native boolean getEnabled(int cellIndex);
	
	private static native int getStatus(int cellIndex);

	public void setEnabled(boolean enabled) {
		synchronized(LOCK){
			setEnabled(cellIndex, enabled);
		}
	}

	private static native void setEnabled(int cellIndex, boolean enabled);
	
	public void enable(boolean enable) {
		synchronized(LOCK){
			enable(cellIndex, enable);
		}
	}

	private static native void enable(int cellIndex, boolean enable);
	
	public Range getLevelRange(PLevel.Type levelType) {
		int[] minMax = getLevelRange(getLevelTypeValueIndex(levelType));

		return new Range(minMax[0], minMax[1]);
	}
	public int[] getLevelRange(int type) {
		synchronized(LOCK){
			return getLevelRange(cellIndex,type);
		}
	}
	static int getLevelTypeValueIndex(PLevel.Type levelType){
		return levelType.ordinal();
	}

	/**
	 * @param cellIndex
	 * @param valueIndex
	 * @return an array of 4 ints, the min, max, unit type (TU_NONE, TU_INCHES, etc.), and unit resolution
	 */
	private static native int[] getLevelRange(int cellIndex, int valueIndex);

	public int getCursor() {
		synchronized(LOCK){
			return getCursor(cellIndex);
		}
	}

	private static native int getCursor(int cellIndex);

	public long getTime(){
		synchronized(LOCK){
			return getTime(cellIndex);
		}
	}

	private static native long getTime(int cellIndex);

	public int getButtons() {
		synchronized(LOCK){
			return getButtons(cellIndex);
		}
	}

	private static native int getButtons(int cellIndex);

	public static CursorType getCursorType(int cursor) {
		synchronized(LOCK){
			return CursorType.VALUES.get(getCursorTypeOrdinal(cursor));
		}
	}

	public static native int getCursorTypeOrdinal(int cursor);

	public static native int getRawCursorType(int cursor);

	public static native String getCursorName(int cursor);

	public int getPacketRate() {
		synchronized(LOCK){
			return getPacketRate(cellIndex);
		}
	}
	private static native int getPacketRate(int cellIndex);
	public String getDeviceName() {
		synchronized(LOCK){
			return getDeviceName(cellIndex);
		}
	}
	private static native String getDeviceName(int cellIndex);

	public int getDeviceHardwareCapabilities() {
		synchronized(LOCK){
			return getDeviceHardwareCapabilities(cellIndex);
		}
	}
	private static native int getDeviceHardwareCapabilities(int cellIndex);
	public static native int getPhysicalId(int cursor);
	public static native int getButtonCount(int cursor);
	public static native String[] getButtonNames(int cursor);
	public static native int getCapabilityMask(int cursor);
	
	@Override
	protected void finalize() throws Throwable {
		synchronized(LOCK){
			if(cellIndex!=-1)
				destroy(cellIndex);
		}
		super.finalize();
	}

	private static native int destroy(int cellIndex);

	public int getStatus(){
		synchronized(LOCK){
			return getStatus(cellIndex);
		}
	}
	
	public boolean getSystemCursorEnabled() {
		synchronized(LOCK){
			return getSystemCursorEnabled(cellIndex);
		}
	}

	private static native boolean getSystemCursorEnabled(int cellIndex);
	
	/**
	@param enabled If <code>true<code> then movement on Wintab devices cause movement on the system pointer. The default value is <code>true<code>. 
	@see WintabProvider#setPointerMovementEnabled(boolean)
	*/
	public void setSystemCursorEnabled(boolean enabled) {
		synchronized(LOCK){
			setSystemCursorEnabled(cellIndex, enabled);
		}
	}

	private static native void setSystemCursorEnabled(int cellIndex, boolean enabled);
	
	@Override
	public String toString() {
		synchronized(LOCK){
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
			sb.append(", id="+getPhysicalId(getCursor()));
			sb.append(", buttons=");
			sb.append(getButtons());
			sb.append(", status="+getStatus());
			sb.append("]");
			return sb.toString();
		}
	}
}
