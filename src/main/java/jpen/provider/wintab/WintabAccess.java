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
package jpen.provider.wintab;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import jpen.PLevel;
import jpen.provider.Utils;

final class WintabAccess {
	private static final Object LOCK=new Object();

	private static long bootTimeUtc=-1;

	/**
	This must be like E_csrTypes enumeration.
	*/
	public enum CursorType{UNDEF, PENTIP, PUCK, PENERASER;
			public static final List<CursorType> VALUES=Collections.unmodifiableList(Arrays.asList(values()));
												}

	private final WintabProvider wintabProvider;
	private final int cellIndex;
	private final Thread packetPusherThread;

	WintabAccess(WintabProvider wintabProvider) throws Exception {
		synchronized(LOCK){
			this.wintabProvider=wintabProvider;
			WintabProvider.loadLibrary();
			this.cellIndex=create();
			if(cellIndex==-1)
				throw new Exception(getError());
			packetPusherThread=new Thread("jpen-WintabProvider-Packet Pusher"){
						@Override
						public void run(){
							init(); // calls my initEnded method from the native side
						}
					};
			packetPusherThread.setPriority(Thread.MAX_PRIORITY);
			packetPusherThread.setDaemon(true);
			synchronized(packetPusherThread){
				packetPusherThread.start();
				packetPusherThread.wait();
			}
			if(!getInitialized())
				throw new Exception(getError());
		}
	}

	private static native int create();
	private static native String getError();

	private void init(){
		init(cellIndex);
	}

	/**
	Called by the native side.
	*/
	private void initEnded(){
		synchronized(packetPusherThread){
			packetPusherThread.notify();
		}
	}

	private native void init(int cellIndex);

	private boolean getInitialized(){
		return getInitialized(cellIndex);
	}

	private static native boolean getInitialized(int cellIndex);


	/**
	Called by the native side.
	*/
	private void packetReady(){
		//System.out.println("packeReady at "+System.currentTimeMillis());
		if(wintabProvider==null) // can be null when testing...
			return;
		wintabProvider.packetReady();
	}

	int getValue(PLevel.Type levelType) {
		// tilt data is really azimuth and altitude and must be transformed!
		synchronized(LOCK){
			return getValue(cellIndex, getLevelTypeValueIndex(levelType));
		}
	}

	private static native int getValue(int cellIndex, int valueIndex);

	public boolean getEnabled() {
		synchronized(LOCK){
			return getEnabled(cellIndex);
		}
	}

	private static native boolean getEnabled(int cellIndex);

	public void setEnabled(boolean enabled) {
		synchronized(LOCK){
			setEnabled(cellIndex, enabled);
		}
	}

	private static native void setEnabled(int cellIndex, boolean enabled);

	public PLevel.Range getLevelRange(PLevel.Type levelType) {
		synchronized(LOCK){
			int[] minMax=getLevelRange(cellIndex,getLevelTypeValueIndex(levelType));
			return new PLevel.Range(minMax[0], minMax[1]);
		}
	}

	static int getLevelTypeValueIndex(PLevel.Type levelType){
		return levelType.ordinal();
	}

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

	static native int getCursorTypeOrdinal(int cursor);

	static native String getCursorName(int cursor);

	static native long getPhysicalId(int cursor);

	@Override
	protected void finalize() {
		synchronized(LOCK){
			if(cellIndex!=-1)
				destroy(cellIndex);
		}
	}

	private static native int destroy(int cellIndex);

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
			sb.append("]");
			return sb.toString();
		}
	}
}