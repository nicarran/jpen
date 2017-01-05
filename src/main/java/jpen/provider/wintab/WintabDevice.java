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
package jpen.provider.wintab;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import jpen.PButton;
import jpen.PButtonEvent;
import jpen.Pen;
import jpen.PKind;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.provider.AbstractPenDevice;
import static java.lang.Math.*;

@SuppressWarnings("deprecation")
class WintabDevice
	extends AbstractPenDevice {
	private static final Logger L=Logger.getLogger(WintabDevice.class.getName());
	//static { L.setLevel(Level.ALL);	}
	
	private static final String PRINT_ALTITUDE_AND_AZIMUTH_SYSTEM_PROPERTY="jpen.provider.wintab.WintabDevice.printAltitudeAndAzimuth";
	private static final boolean PRINT_ALTITUDE_AND_AZIMUTH=Boolean.valueOf(
				System.getProperty(PRINT_ALTITUDE_AND_AZIMUTH_SYSTEM_PROPERTY));

	final WintabProvider wintabProvider;
	public final int cursor;
	private int lastButtonsValues;
	private final Point2D.Float componentLocation=new Point2D.Float();
	private final Dimension componentSize=new Dimension();
	private boolean useFractionalMovement=true;

	WintabDevice(WintabProvider wintabProvider, int cursor) {
		super(wintabProvider);
		L.fine("start");
		this.wintabProvider=wintabProvider;
		this.cursor=cursor;
		setKindTypeNumber(getDefaultKindTypeNumber());
		setEnabled(true);
		L.fine("end");
	}

	@Override
	public final boolean getUseFractionalMovements(){
		return useFractionalMovement;
	}

	@Override
	public void penManagerSetUseFractionalMovements(boolean useFractionalMovement){
		this.useFractionalMovement=useFractionalMovement;
	}


	@Override
	protected String evalPhysicalId(){
		return  WintabAccess.getRawCursorType(cursor)+"."
						+WintabAccess.getPhysicalId(cursor)+"@"
						+wintabProvider.getConstructor().getName();
	}

	private int getDefaultKindTypeNumber() {
		WintabAccess.CursorType cursorType=WintabAccess.getCursorType(cursor);
		switch(cursorType) {
		case PENTIP:
			return PKind.Type.STYLUS.ordinal();
		case PENERASER:
			return PKind.Type.ERASER.ordinal();
		case PUCK:
		case UNDEF:
			String deviceName=getName().toLowerCase();
			if(deviceName.contains("stylus"))
				return PKind.Type.STYLUS.ordinal();
			if(deviceName.contains("eraser"))
				return PKind.Type.ERASER.ordinal();
			return cursorType.equals(WintabAccess.CursorType.PUCK)?
						 PKind.Type.CURSOR.ordinal():
						 PKind.Type.STYLUS.ordinal();
		default:
			throw new AssertionError();
		}
	}

	public String getName() {
		return WintabAccess.getCursorName(cursor).trim();
	}

	void scheduleEvents() {
		if(!getEnabled()) {
			L.fine("disabled");
			return;
		}
		//if(L.isLoggable(Level.FINE))
		//L.fine(wintabProvider.wintabAccess.toString());
		scheduleLevelEvent();
		// scheduleButtonEvents(); nicarran:  TODO use this to support extra buttons?
	}

	/* nicarran:  TODO use this to support extra buttons?
	private void scheduleButtonEvents() {
		int newButtonsValues=wintabProvider.wintabAccess.getButtons();
		if(newButtonsValues==lastButtonsValues)
			return;
		if(L.isLoggable(Level.FINE))
			L.fine("newButtonsValues="+newButtonsValues);
		for(PButton.Type buttonType:PButton.Type.values()) {
			boolean value=getButtonState(newButtonsValues, getButtonIndex(buttonType));
			getPenManager().scheduleButtonEvent(this, wintabProvider.wintabAccess.getTime(),  new PButton(buttonType.ordinal(), value));
		}
		lastButtonsValues=newButtonsValues;
	}

	private boolean getButtonState(int buttonsState, int buttonIndex) {
		return (buttonsState&(1<<buttonIndex))>0;
	}
	*/

	private final List<PLevel> changedLevels=new ArrayList<PLevel>();
	private void scheduleLevelEvent() {
		for(int i=PLevel.Type.VALUES.size(); --i>=0;) {
			PLevel.Type levelType=PLevel.Type.VALUES.get(i);
			float value=getMultRangedValue(levelType);
			changedLevels.add(new PLevel(levelType, value));
		}
		getPenManager().scheduleLevelEvent(this, wintabProvider.wintabAccess.getTime(), changedLevels, true);
		changedLevels.clear();
	}

	private static final float PI_2=(float)(Math.PI*2);
	private static final double PI_over_2=Math.PI/2;
	private static final double PI_over_2_over_900=PI_over_2/900; // (/10) and (/90)

	private float getMultRangedValue(PLevel.Type type) {
		if(PLevel.Type.TILT_TYPES.contains(type)) {
			double altitude=wintabProvider.wintabAccess.getValue(PLevel.Type.TILT_Y);
			if(altitude<0)
				altitude=-altitude; // when using the eraser the altitude is upside down.
			double rangedAltitude=altitude/wintabProvider.getLevelRange(PLevel.Type.TILT_Y).max; // 0 (0deg) <= rangedAltitude <= 1 (90deg)
			if(rangedAltitude==1){ // optimization
				if(PRINT_ALTITUDE_AND_AZIMUTH)
					System.out.println("rangedAlt: 1");
				return 0;
			}
			double betha=rangedAltitude*PI_over_2; // see tiltOnWintab.xoj to understand the meaning of betha and theta
			
			double azimuth=wintabProvider.wintabAccess.getValue(PLevel.Type.TILT_X);
			double rangedAzimuth=azimuth/getCorrectAzimuthRangeMax(); // 0 (0deg) <= rangedAzimuth < 1 (360deg)
			double theta=rangedAzimuth*PI_2-PI_over_2;
			
			if(PRINT_ALTITUDE_AND_AZIMUTH)
				System.out.printf("alt: %4.0f - az: %4.0f | rangedAlt: %1.3f - rangedAz: %1.3f \n", altitude, azimuth, rangedAltitude, rangedAzimuth);
			
			switch(type) {
			case TILT_X:
				return (float)atan(cos(theta)/tan(betha));
			case TILT_Y:
				return (float)atan(sin(theta)/tan(betha));
			default:
				throw new AssertionError();
			}
		}

		float rangedValue=wintabProvider.getLevelRange(type).getRangedValue(
					wintabProvider.wintabAccess.getValue(type));

		if(PLevel.Type.MOVEMENT_TYPES.contains(type)){
			if(type.equals(PLevel.Type.Y))
				rangedValue=1f-rangedValue;
			rangedValue=wintabProvider.screenBounds.getLevelRangeOffset(type)+
									rangedValue*wintabProvider.screenBounds.getLevelRangeMult(type);
		}

		if(PLevel.Type.ROTATION.equals(type)){			
                        rangedValue= 1 - rangedValue; // invert direction to be the same as on linux and like written in the PLevel javadoc
                        rangedValue*=PI_2;
                }

		return rangedValue;
	}
	
	/**
	The maximum value for the azimuth must be corrected to be the equivalent of 360deg. On Wacom Intuos the values is the corresponding Range.max but on other tablet it is assumed that the value could less than  360 (1 less than 360 given its resolution). So we simply add to the Range.max until we get a multiple of 360. See bug https://sourceforge.net/tracker/?func=detail&aid=3603044&group_id=209997&atid=1011964 . 
	*/
	private float getCorrectAzimuthRangeMax(){
		if(correctAzimuthRangeMax!=-1)
			return correctAzimuthRangeMax;
		correctAzimuthRangeMax=wintabProvider.getLevelRange(PLevel.Type.TILT_X).max;
		while(correctAzimuthRangeMax%360!=0)
			correctAzimuthRangeMax++;
		//System.out.println("correctAzimuthRangeMax=" + ( correctAzimuthRangeMax ));
		return correctAzimuthRangeMax;
	}
	
	private float correctAzimuthRangeMax=-1;
}