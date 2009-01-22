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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class TypedClass<T extends Enum<T>>
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;
	public final int typeNumber;

	TypedClass(int typeNumber) {
		if(typeNumber<0)
			throw new IllegalArgumentException();
		this.typeNumber=typeNumber;
	}

	abstract List<T> getAllTypes();

	/**
	@return The enum type matching the {@link #typeNumber}.
	*/
	public final T getType() {
		return getType(typeNumber);
	}

	private final T getType(int typeNumber){
		List<T> types=getAllTypes();
		int customTypeOrdinal=getCustomTypeOrdinal(types);
		if(typeNumber>=customTypeOrdinal)
			return types.get(customTypeOrdinal);
		return types.get(typeNumber);
	}
	
	private static final <T extends Enum<T>> int getCustomTypeOrdinal(Collection<T> types){
		return types.size()-1;// the last is always the CUSTOM
	}

	@SuppressWarnings("unchecked")
	static final <T extends Enum<T>> List<T> createStandardTypes(List<T> allTypes){
		List<T> stdTypes=new ArrayList<T>(allTypes);
		stdTypes.remove(getCustomTypeOrdinal(stdTypes));
		return Collections.unmodifiableList(stdTypes);
	}

	@Override
	public String toString() {
		return "(type="+getType()+")";
	}
}
