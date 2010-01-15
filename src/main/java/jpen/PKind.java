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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PKind
			extends TypedClass<PKind.Type>
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public enum Type{
		CURSOR, STYLUS, ERASER, 
		/**
		Used for devices that does not identify the pen, rater extend it, like the pad.
		*/
		IGNORE, 
		CUSTOM;
		
		public static final List<Type> ALL_VALUES=Collections.unmodifiableList(Arrays.asList(values()));
		public static final List<Type> VALUES=TypedClass.createStandardTypes(ALL_VALUES);
	}

	private PKind(int typeNumber) {
		super(typeNumber);
	}

	private static final List<PKind> VALUES_L=new ArrayList<PKind>(Type.VALUES.size());
	private static final List<PKind> VALUES=Collections.unmodifiableList(VALUES_L);
	
	public static PKind valueOf(Type type){
		return valueOf(type.ordinal());
	}

	public static PKind valueOf(int typeNumber){
		while(VALUES_L.size()<=typeNumber)
			VALUES_L.add(new PKind(VALUES_L.size()));
		return VALUES_L.get(typeNumber);
	}

	@Override
	final List<Type> getAllTypes() {
		return Type.ALL_VALUES;
	}
}
