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
import java.util.Collections;
import java.util.List;

public class PKind
			extends TypedClass<PKind.Type>
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public enum Type{
	  CURSOR, STYLUS, ERASER;
	  private static final Type[] VALUES=Type.values();

	  public static final PKind.Type valueOf(int kindTypeNumber) {
		  if(kindTypeNumber>=VALUES.length)
			  return null;
		  return VALUES[kindTypeNumber];
	  }
	}

	private PKind(int typeNumber) {
		super(typeNumber);
	}

	@Deprecated // will be private
	public static final List<PKind> VALUES=Collections.unmodifiableList(new ArrayList<PKind>(Type.VALUES.length));

	public static PKind valueOf(int typeNumber){
		while(VALUES.size()<=typeNumber)
			VALUES.add(new PKind(VALUES.size()));
		return VALUES.get(typeNumber);
	}

	public static PKind valueOf(Type type){
		return valueOf(type.ordinal());
	}

	@Override
	Type[] createTypes() {
		return Type.values();
	}
}
