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

import jpen.event.PenListener;
import java.util.Arrays;

public abstract class PenEvent
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	long time=-1;
	transient PenEvent next;
	public final transient Pen pen;
	
	PenEvent(Pen pen) {
		this.pen=pen;
	}
	
	/**
	Returns the time in milliseconds of when this event was scheduled by the {@link Pen}.
	*/
	public long getTime() {
		return time;
	}
	
	abstract void copyTo(PenState penState);

	abstract void dispatch();

}
