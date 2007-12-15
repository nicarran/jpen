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
package jpen;

import java.util.Arrays;
import jpen.event.PenListener;

public class PLevelEvent
	extends PenEvent {
	public final PLevel[] levels;

	PLevelEvent(Pen pen, PLevel[] levels) {
		super(pen);
		this.levels=levels;
	}

	@Override
	void dispatch() {
		for(PLevel level:levels)
			pen.setLevelValue(level);
		for(PenListener l:pen.getListenersArray())
			l.penLevelEvent(this);
	}

	public boolean isMovement() {
		for(int i=levels.length; --i>=0;)
			if(levels[i].isMovement())
				return true;
		return false;
	}

	@Override
	public String toString() {
		return "[PLevelEvent: time="+time+", levels="+Arrays.asList(levels)+"]";
	}
}
