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

class AbsoluteOnARowRule
	implements RelativeLocationFilter.Rule{
	static final float THRESHOLD=7f;
	static final int ROW_COUNT_TO_ABSOLUTE=20;
	
	private int missedPoints;
	int rowCount;

	//@Override
	public void reset(){
		missedPoints=-1;
		rowCount=0;
	}

	//@Override
	public RelativeLocationFilter.State evalFilterNextState(RelativeLocationFilter filter){
		missedPoints++;
		if(!filter.samplePoint.isComplete)
			return null;
		rowCount++;
		if(filter.absDeviation.x>THRESHOLD ||
			 filter.absDeviation.y>THRESHOLD){
			rowCount=0;
		}
		if(rowCount>=ROW_COUNT_TO_ABSOLUTE){
			//System.out.println("absolute device detected (row), missedPoints="+missedPoints);
			return RelativeLocationFilter.State.ABSOLUTE;
		}
		return null;
	}
}