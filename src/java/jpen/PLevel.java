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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.Point2D;

public class PLevel
	extends TypedValuedClass<PLevel.Type, Float> {
	public enum Type{
	  X(true),  Y(true),  PRESSURE(false);
	  public final boolean isMovement;
	  Type(boolean isMovement) {
		  this.isMovement=isMovement;
	  }
	}

	public static class Range {
		public final float min;
		public final float max;
		public Range(float min, float max) {
			this.min=min;
			this.max=max;
		}
		public final float getRangedValue(float value) {
			return (value-min)/(max-min);
		}

		@Override
		public String toString() {
			return "[PLevel.Range: "+min+", "+max+"]";
		}
	}

	public PLevel(int typeNumber, float value) {
		super(typeNumber, value);
	}

	@Override
	Type[] getTypes() {
		return Type.values();
	}

	public boolean isMovement() {
		Type type=getType();
		return type!=null&& type.isMovement;
	}

	public static final float getCoordinateValueInsideComponent( Dimension componentSize, Point2D.Float componentLocation, PLevel.Type coordinate, float coordinateValue) {
		switch(coordinate) {
		case X:
			coordinateValue-=componentLocation.x;
			if(coordinateValue>componentSize.width)
				return -1;
			break;
		case Y:
			coordinateValue-=componentLocation.y;
			if(coordinateValue>componentSize.height)
				return -1;
			break;
		}
		return coordinateValue;
	}

}
