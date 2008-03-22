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
import java.util.logging.Level;
import java.util.logging.Logger;

public class PLevel
			extends TypedValuedClass<PLevel.Type, Float>
	implements java.io.Serializable {
	private static final Logger L=Logger.getLogger(PLevel.class.getName());
	public static final long serialVersionUID=1l;

	public enum Type{
		X(true, false),  Y(true, false),  PRESSURE(false, false),
		/**
		Tilt along the X axis in radians.
		*/
		TILT_X(false, true),
		/**
		Tilt along the Y axis in radians.
		*/
		TILT_Y(false, true);
		public final boolean isMovement;
		public final boolean isTilt;
		Type(boolean isMovement, boolean isTilt) {
			this.isMovement=isMovement;
			this.isTilt=isTilt;
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
		if(L.isLoggable(Level.FINE)){
			L.fine("componentSize="+componentSize+", componentLocation="+componentLocation+", coordinate="+coordinate+", coordinateValue="+coordinateValue);
		}
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
