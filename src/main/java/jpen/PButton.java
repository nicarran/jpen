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
import java.util.Collections;
import java.util.List;

public class PButton
			extends TypedValuedClass<PButton.Type, Boolean>
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public enum Type{
		LEFT, CENTER, RIGHT,
		/**
		Used whenever the {@link Pen} {@link PLevel.Type#PRESSURE} level value changes from/to 0.
		*/
		ON_PRESSURE, 
		CUSTOM;
		public static final List<Type> ALL_VALUES=Collections.unmodifiableList(Arrays.asList(values()));
		public static final List<Type> VALUES=TypedClass.createStandardTypes(ALL_VALUES);
	}

	public PButton(int typeNumber, Boolean value) {
		super(typeNumber, value);
	}
	
	// TODO: valueOf ?

	@Override
	final List<Type> getAllTypes(){
		return Type.ALL_VALUES;
	}
}
