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

import java.util.ArrayList;
import java.util.List;

public class PKind
			extends TypedClass<PKind.Type>
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public enum Type{
		CURSOR, STYLUS, ERASER;

		public static final PKind.Type valueOf(int kindTypeNumber) {
			if(kindTypeNumber>=values().length)
				return null;
			return values()[kindTypeNumber];
		}
	}

	/**
	@deprecated Use {@link #valueOf(int)} instead. (080216)
	*/
	@Deprecated
	public PKind(int typeNumber) {
		super(typeNumber);
	}

	public static final List<PKind> VALUES=new ArrayList<PKind>(Type.values().length);

	public static PKind valueOf(int typeNumber){
		while(VALUES.size()<=typeNumber)
			VALUES.add(new PKind(VALUES.size()));
		return VALUES.get(typeNumber);
	}

	public static PKind valueOf(Type type){
		return valueOf(type.ordinal());
	}

	@Override
	Type[] getTypes() {
		return Type.values();
	}
}
