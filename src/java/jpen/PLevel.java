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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import static java.lang.Math.*;

public class PLevel
			extends TypedValuedClass<PLevel.Type, Float>
	implements java.io.Serializable {
	private static final Logger L=Logger.getLogger(PLevel.class.getName());
	public static final long serialVersionUID=1l;

	public enum Type{
		/**
		X axis value in pixels. The X axis points to the right of the screen. It's a left handed coordinate system: the Z axis points upside.  
		*/
	  X,
		/**
		Y axis value in pixels. The Y axis points to the bottom of the screen.
		*/
		Y,  
		/**
		Range: 0 to 1.
		*/
		PRESSURE,
	  /**
		Angle between the Z axis and the projection of the pen against the X-Z plane. Range: -pi/2 to pi/2 (radians). 
	  */
	  TILT_X,
	  /**
		Angle between the Z axis and the projection of the pen against the Y-Z plane. Range: -pi/2 to pi/2.
	  */
	  TILT_Y;

	  public static final List<Type> VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	  public static final Set<Type> MOVEMENT_TYPES=Collections.unmodifiableSet(EnumSet.of(X, Y));
	  public static final Set<Type> TILT_TYPES=Collections.unmodifiableSet(EnumSet.of(TILT_X, TILT_Y));
		
		/**
		Evaluates the azimuthX and altitude given the tilt values of the pen.
		
		@see #evalAzimuthXAndAltitude(double[], double tiltX, double tiltY)
		*/
		public static void evalAzimuthXAndAltitude(double[] azimuthXAndAltitude, PenState pen){
			evalAzimuthXAndAltitude(azimuthXAndAltitude, pen.getLevelValue(TILT_X), pen.getLevelValue(TILT_Y));
		}
		
		/**
		Evaluates the azimuthX and the altitude given the tilt ({@link #TILT_X}, {@link #TILT_Y}) values. Where:<p>
		{@code azimuthX} is the angle between the X axis and the projection of the pen against the X-Y plane. Clockwise direction. Range: -pi/2 and 3*pi/2 <p>
		And {@code altitude} is the angle between the pen and the projection of the pen against the X-Y plane. Range: 0 to pi/2.
		*/
	  public static void evalAzimuthXAndAltitude(double[] azimuthXAndAltitude, double tiltX, double tiltY){
		  if(tiltX<0)
			  azimuthXAndAltitude[0]=PI;
		  else if(tiltX==0 && tiltY==0){
			  azimuthXAndAltitude[0]=0;
			  azimuthXAndAltitude[1]=PI/2;
			  return;
		  } else
			  azimuthXAndAltitude[0]=0;
		  double tanTiltY=tan(tiltY);
		  azimuthXAndAltitude[0]+=atan(tanTiltY/tan(tiltX));
		  azimuthXAndAltitude[1]=azimuthXAndAltitude[0]==0?
		      PI/2-tiltX:
		      Math.abs(atan(sin(azimuthXAndAltitude[0])/tanTiltY));
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
	List<Type> getTypes() {
		return Type.VALUES;
	}

	public boolean isMovement() {
		return Type.MOVEMENT_TYPES.contains(getType());
	}

	public static final float getCoordinateValueForComponent( Dimension componentSize, Point2D.Float componentLocation, PLevel.Type coordinate, float coordinateValue) {
		if(L.isLoggable(Level.FINE)){
			L.fine("componentSize="+componentSize+", componentLocation="+componentLocation+", coordinate="+coordinate+", coordinateValue="+coordinateValue);
		}
		switch(coordinate) {
		case X:
			coordinateValue-=componentLocation.x;
			break;
		case Y:
			coordinateValue-=componentLocation.y;
			break;
		}
		return coordinateValue;
	}

	/**
	@deprecated No replacement for this method in the new API. There is a similar method: {@link #getCoordinateValueForComponent(Dimension componentSize, Point2D.Float componentLocation, PLevel.Type coordinate, float coordinateValue)}.
	*/
	@Deprecated
	public static final float getCoordinateValueInsideComponent( Dimension componentSize, Point2D.Float componentLocation, PLevel.Type coordinate, float coordinateValue) {
		if(L.isLoggable(Level.FINE)){
			L.fine("componentSize="+componentSize+", componentLocation="+componentLocation+", coordinate="+coordinate+", coordinateValue="+coordinateValue);
		}
		switch(coordinate) {
		case X:
			coordinateValue-=componentLocation.x;
			//if(coordinateValue>componentSize.width)
			//return -1;
			break;
		case Y:
			coordinateValue-=componentLocation.y;
			//if(coordinateValue>componentSize.height)
			//return -1;
			break;
		}
		return coordinateValue;
	}

}
