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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class PButton
			extends TypedValuedClass<PButton.Type, Boolean>
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public enum Type{
		LEFT(TypeGroup.MOUSE), CENTER(TypeGroup.MOUSE), RIGHT(TypeGroup.MOUSE),
		/**
		Used whenever the {@link Pen} {@link PLevel.Type#PRESSURE} level value changes from/to 0.
		*/
		ON_PRESSURE(TypeGroup.LEVEL),

		CONTROL(TypeGroup.MODIFIER), SHIFT(TypeGroup.MODIFIER), ALT(TypeGroup.MODIFIER),
		/* nicarran: experimental keyboard buttons support:
		VK_1(TypeGroup.VK_NUMBER), VK_2(TypeGroup.VK_NUMBER), VK_3(TypeGroup.VK_NUMBER), VK_4(TypeGroup.VK_NUMBER), VK_5(TypeGroup.VK_NUMBER), VK_6(TypeGroup.VK_NUMBER), VK_7(TypeGroup.VK_NUMBER), VK_8(TypeGroup.VK_NUMBER), VK_9(TypeGroup.VK_NUMBER), VK_0(TypeGroup.VK_NUMBER),
		*/

		CUSTOM(TypeGroup.UNDEFINED);
		
		private final TypeGroup group;

		Type(TypeGroup group){
			this.group=group;
			group.types.add(this);
		}
		
		public TypeGroup getGroup(){
			return group;
		}

		public static final List<Type> ALL_VALUES=Collections.unmodifiableList(Arrays.asList(values()));
		public static final List<Type> VALUES=TypedClass.createStandardTypes(ALL_VALUES);
	}

	public enum TypeGroup{
		/**
		Standard mouse buttons.
		*/
		MOUSE,
		/**
		Level condition buttons.
		*/
		LEVEL,
		/**
		Modifier buttons.
		*/
		MODIFIER,
		/* nicarran: experimental keyboard buttons support:
		/**
		Virtual key number buttons.
		VK_NUMBER,
		*/
		UNDEFINED;

		private Collection<Type> types=new ArrayList<Type>();
		private Collection<Type> typesAccess;

		public Collection<Type> getTypes(){
			if(typesAccess==null){
				types=EnumSet.copyOf(types);
				typesAccess=Collections.unmodifiableCollection(types);
			}
			return typesAccess;
		}
	}

	public PButton(Type type, Boolean value){
		this(type.ordinal(), value);
	}

	public PButton(int typeNumber, Boolean value) {
		super(typeNumber, value);
	}

	@Override
	final List<Type> getAllTypes(){
		return Type.ALL_VALUES;
	}
}