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

import java.awt.geom.Point2D;

class RelativeOnSlopesRule
	implements RelativeLocationFilter.Rule{

	private static final int THRESHOLD=3;

	private int missedPoints;
	private int positiveSlopes;
	private final Point2D.Float previousAbsDeviation=new Point2D.Float();

	//@Override
	public void reset(){
		missedPoints=-1;
		positiveSlopes=-1;
	}

	//@Override
	public RelativeLocationFilter.State evalFilterNextState(RelativeLocationFilter filter){
		missedPoints++;
		if(!filter.samplePoint.isComplete)
			return null;
		if(positiveSlopes==-1){
			previousAbsDeviation.setLocation(filter.absDeviation);
			positiveSlopes=0;
		}else{
			if(filter.absDeviation.x<=AbsoluteOnARowRule.THRESHOLD &&
				 filter.absDeviation.y<=AbsoluteOnARowRule.THRESHOLD){
				positiveSlopes=0;
			}
			else if(filter.absDeviation.x>=previousAbsDeviation.x ||
				 filter.absDeviation.y>=previousAbsDeviation.y){
				positiveSlopes++;
			}
		}
		if(positiveSlopes>=THRESHOLD){
				//System.out.println("relative device detected, missedPoints="+missedPoints);
				return RelativeLocationFilter.State.RELATIVE;
			}
		previousAbsDeviation.setLocation(filter.absDeviation);
		return null;
	}
}