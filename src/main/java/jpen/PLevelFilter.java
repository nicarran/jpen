/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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

import java.util.EnumSet;
import java.util.Set;

public interface PLevelFilter{
	boolean filterPenLevel(PLevel level);

	public static final class AllowAll
		implements PLevelFilter{
		public static final PLevelFilter INSTANCE=new AllowAll();
		private AllowAll(){}
		//@Override
		public boolean filterPenLevel(PLevel level){
			return false;
		}
	}

	public static final class FilterByType
		implements PLevelFilter{
		private final Set<PLevel.Type> typesToFilter;

		public FilterByType(Set<PLevel.Type> typesToFilter){
			this.typesToFilter=typesToFilter;
		}

		public static FilterByType createByAllowed(Set<PLevel.Type> allowed){
			Set<PLevel.Type> typesToFilter=EnumSet.allOf(PLevel.Type.class);
			typesToFilter.removeAll(allowed);
			return new FilterByType(typesToFilter);
		}

		//@Override
		public boolean filterPenLevel(PLevel level){
			return typesToFilter.contains(level.getType());
		}
	}
}
