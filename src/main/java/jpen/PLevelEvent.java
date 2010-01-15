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
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.Set;
import jpen.event.PenListener;
import jpen.internal.AccessibleField;

public class PLevelEvent
			extends PenEvent
	implements java.io.Serializable {
	public static final long serialVersionUID=2l;

	public final PLevel[] levels;

	public PLevelEvent(PenDevice device, long deviceTime, PLevel[] levels) {
		super(device, deviceTime);
		this.levels=levels;
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
		return "[PLevelEvent: super="+super.toString()+", levels="+Arrays.asList(levels)+"]";
	}

	private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException {
		try{
			ObjectInputStream.GetField fields = in.readFields();
			levelsField.getField().set(this, fields.get("levels", null));
			//v Backwards compatibility:
			//vv deviceId and deviceTime were moved to the super class PenEvent:
			ObjectStreamClass objectStreamClass=fields.getObjectStreamClass();
			if(objectStreamClass.getField("deviceId")!=null)
				PenEvent.deviceIdField.getField().set(this, fields.get("deviceId", (byte)0));
			if(objectStreamClass.getField("deviceTime")!=null)
				PenEvent.deviceTimeField.getField().set(this, fields.get("deviceTime", 0l));
			//^^
			//^
		}catch(IllegalAccessException ex){
			throw new AssertionError(ex);
		}
	}

	private static final AccessibleField levelsField=new AccessibleField(PLevelEvent.class, "levels");

}