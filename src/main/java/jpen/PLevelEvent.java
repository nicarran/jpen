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

import java.util.Arrays;
import java.util.Set;
import jpen.event.PenListener;

public class PLevelEvent
			extends PenEvent
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public final PLevel[] levels;
	private final long deviceTime;
	private final byte deviceId;

	PLevelEvent(Pen pen, PLevel[] levels, byte deviceId, long deviceTime) {
		super(pen);
		this.levels=levels;
		this.deviceTime=deviceTime;
		this.deviceId=deviceId;
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
	Returns the id of the {@link PenDevice} which generated this event.
	*/
	public byte getDeviceId(){
		return deviceId;
	}
	
	@Override
	void copyTo(PenState penState){
		penState.levels.setValues(this);
	}

	@Override
	void dispatch() {
		for(PenListener l:pen.getListenersArray())
			l.penLevelEvent(this);
	}

	public boolean containsLevelOfType(Set<PLevel.Type> levelTypes){
		for(int i=levels.length; --i>=0;)
			if(levelTypes.contains(levels[i].getType()))
				return true;
		return false;
	}

	public boolean isMovement() {
		return containsLevelOfType(PLevel.Type.MOVEMENT_TYPES);
	}

	@Override
	public String toString() {
		return "[PLevelEvent: time="+time+", levels="+Arrays.asList(levels)+", deviceId="+deviceId+", deviceTime="+deviceTime+"]";
	}

}
