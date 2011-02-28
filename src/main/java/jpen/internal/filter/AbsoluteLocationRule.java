/* [{
Copyright 2010 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.internal.filter;

import java.util.logging.Level;
import java.util.logging.Logger;

class AbsoluteLocationRule
	implements RelativeLocationFilter.Rule{
	//private static final Logger L=Logger.getLogger(AbsoluteLocationRule.class.getName());
	//static { L.setLevel(Level.ALL); }

	private int missedPoints;

	//@Override
	public void reset(){
		missedPoints=-1;
	}

	//@Override
	public RelativeLocationFilter.State evalFilterNextState(RelativeLocationFilter filter){
		missedPoints++;
		if(!filter.samplePoint.isComplete)
			return null;
		float maxDeviation=Math.max(filter.absDeviation.x, filter.absDeviation.y);
		if(maxDeviation<1.5f){
			//System.out.println("absolute device detected, missedPoints="+missedPoints);
			return RelativeLocationFilter.State.ABSOLUTE;
		}
		return null;
	}
}