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
package jpen;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import jpen.event.PenListener;
import jpen.internal.AccessibleField;

public abstract class PenEvent
	implements java.io.Serializable {
	public static final long serialVersionUID=2l;

	protected long time=-1;
	transient volatile PenEvent next;
	public final transient Pen pen;
	private final byte deviceId;
	private final long deviceTime;
	private transient Object penOwnerTag;

	PenEvent(PenDevice device, long deviceTime) {
		this(device.getProvider().getConstructor().getPenManager().pen,
				 device.getId(),
				 deviceTime);
	}

	private PenEvent(Pen pen, byte deviceId, long deviceTime){
		this.pen=pen;
		this.deviceId=deviceId;
		this.deviceTime=deviceTime;
	}

	static final class Dummy
		extends PenEvent{
		Dummy(){
			super(null, (byte)0, 0);
		}
		@Override
		void copyTo(PenState penState){}
		@Override
		void dispatch(){}
	}

	/**
	Returns the time in milliseconds of when this event was scheduled by the {@link Pen}.
	*/
	public long getTime() {
		return time;
	}

	abstract void copyTo(PenState penState);

	abstract void dispatch();

	/**
	Returns the id of the {@link PenDevice} which generated this event.

	@see #getDevice()
	@see PenManager#getDevice(byte)
	*/
	public byte getDeviceId(){
		return deviceId;
	}

	/**
	@throws IllegalStateException if the {@link #pen} is null. {@code pen} is null after deserialization.
	*/
	public PenDevice getDevice(){
		if(pen==null)
			throw new IllegalStateException();
		return pen.penManager.getDevice(deviceId);
	}

	/**
	Returns the timestamp in milliseconds of when this event ocurred in the {@link PenDevice}. The value returned was measured since some fixed but arbitrary time imposed by the {@link PenDevice}.

	@see #getDeviceId()
	@see #getTime()
	*/
	public long getDeviceTime(){
		return deviceTime;
	}
	
	/**
	@see PenOwner#evalPenEventTag(PenEvent)
	*/
	void setPenOwnerTag(Object penOwnerTag){
		this.penOwnerTag=penOwnerTag;
	}
	
	Object getPenOwnerTag(){
		return penOwnerTag;
	}

	@Override
	public String toString(){
		return "[PenEvent: deviceId="+deviceId+", deviceTime="+deviceTime+", time="+time+"]";
	}

	private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException {
		try{
			ObjectInputStream.GetField fields = in.readFields();
			time=fields.get("time", 0l);
			//v Backwards compatibility:
			//vv deviceId and deviceTime are new, provide better defaults:
			//if(fields.defaulted("deviceId"))
			//deviceIdField.getField().set(this, fields.get("deviceId", (byte)0));
			if(fields.defaulted("deviceTime"))
				deviceTimeField.getField().set(this, fields.get("deviceTime", time));
			//^^
			//^
		}catch(IllegalAccessException ex){
			throw new AssertionError(ex);
		}
	}

	static final AccessibleField deviceIdField=new AccessibleField(PenEvent.class, "deviceId");
	static final AccessibleField deviceTimeField=new AccessibleField(PenEvent.class, "deviceTime");
}