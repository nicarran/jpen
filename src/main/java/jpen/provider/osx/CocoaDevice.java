/* [{
Copyright 2008 Brien Colwell

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
package jpen.provider.osx;

import jpen.PKind;
import jpen.provider.AbstractPenDevice;


class CocoaDevice extends AbstractPenDevice {
	private final PKind.Type type;

	public CocoaDevice(final CocoaProvider _cocoaProvider, final PKind.Type _type) {
		super(_cocoaProvider);
		type = _type;
		setKindTypeNumber(type.ordinal());
		super.setEnabled(true);
	}
	
	public String getName() {
		switch (type) {
		case CURSOR: return "Cocoa Cursor";
		case ERASER: return "Cocoa Eraser";
		case STYLUS: return "Cocoa Stylus";
		default:
			return "UNKNOWN";
		}
	}

	/**
	 * @return the type
	 */
	public PKind.Type getType() {
		return type;
	}
}
