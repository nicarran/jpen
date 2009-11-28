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

public class PButtonEvent
	extends PenEvent
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public final PButton button;

	PButtonEvent(PenDevice device, long deviceTime, PButton button) {
		super(device, deviceTime);
		this.button=button;
	}

	@Override
	void copyTo(PenState penState){
		penState.setButtonValue(button.typeNumber, button.value);
	}

	@Override
	void dispatch() {
		for(PenListener l:pen.getListenersArray())
			l.penButtonEvent(this);
	}

	@Override
	public String toString() {
		return "[PButtonEvent: super="+super.toString()+", button="+button+"]";
	}
}
