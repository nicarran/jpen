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
import jpen.provider.Utils;
import static java.lang.Math.*;

class WintabDevice
	extends AbstractPenDevice {
	private static final Logger L=Logger.getLogger(WintabDevice.class.getName());
	//static { L.setLevel(Level.ALL);	}

	final WintabProvider wintabProvider;
	public final int cursor;
	private int lastButtonsValues;
	private final Point2D.Float componentLocation=new Point2D.Float();
	private final Dimension componentSize=new Dimension();

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
	protected String evalPhysicalId(){
		return  wintabProvider.wintabAccess.getCursorTypeOrdinal(cursor)+"-"
		        +wintabProvider.wintabAccess.getPhysicalId(cursor)+"@"
		        +wintabProvider.getConstructor().getName();
	}

	private int getDefaultKindTypeNumber() {
		WintabAccess.CursorType cursorType=wintabProvider.wintabAccess.getCursorType(cursor);
		switch(cursorType) {
		case PENTIP:
			return PKind.Type.STYLUS.ordinal();
		case PUCK:
			return PKind.Type.CURSOR.ordinal();
		case PENERASER:
			return PKind.Type.ERASER.ordinal();
		}
		return PKind.Type.CURSOR.ordinal();
	}

	public String getName() {
		return wintabProvider.wintabAccess.getCursorName(cursor).trim();
	}

	void scheduleEvents() {
		if(!getEnabled()) {
			L.fine("disabled");
			return;
		}
		//if(L.isLoggable(Level.FINE))
		//L.fine(wintabProvider.wintabAccess.toString());
		scheduleLevelEvent();
		// scheduleButtonEvents(); nicarran:  TODO use this to support extra buttons.
	}

	private void scheduleButtonEvents() {
		int newButtonsValues=wintabProvider.wintabAccess.getButtons();
		if(newButtonsValues==lastButtonsValues)
			return;
		if(L.isLoggable(Level.FINE))
			L.fine("newButtonsValues="+newButtonsValues);
		for(PButton.Type buttonType:PButton.Type.values()) {
			boolean value=getButtonState(newButtonsValues, getButtonIndex(buttonType));
			getPenManager().scheduleButtonEvent(new PButton(buttonType.ordinal(), value));
		}
		lastButtonsValues=newButtonsValues;
	}


	private int getButtonIndex(PButton.Type type) {
		switch(type) {
		case LEFT:
			return 0;
		case RIGHT:
			return 1;
		case CENTER:
			return 2;
		default:
			throw new AssertionError();
		}
	}

	private boolean getButtonState(int buttonsState, int buttonIndex) {
		return (buttonsState&(1<<buttonIndex))>0;
	}

	private final List<PLevel> changedLevels=new ArrayList<PLevel>();
	private void scheduleLevelEvent() {
		Utils.getLocationOnScreen(getComponent(), componentLocation);
		/*if(L.isLoggable(Level.FINE)) {
			L.fine("componentLocation: "+componentLocation);
			Point p=getComponent().getLocationOnScreen();
			if(!componentLocation.equals(p))
				L.fine("UUUPS! something is wrong with the component location calc, getLocationOnScreen(): "+p);
	}*/
		for(PLevel.Type levelType:PLevel.Type.values()) {
			float value=PLevel.getCoordinateValueForComponent(
			              getComponent().getSize(componentSize), componentLocation,  levelType,  getMultRangedValue(levelType));
			//value=wintabProvider.mouseLocator.getCorrectedLocation(levelType, value);
			/*if(L.isLoggable(Level.FINE)) {
				L.fine("levelType="+levelType+", value="+value);
		}*/
			/*if(levelType.isMovement && value<0) {
				L.fine("negative value... pausing...");
				wintabProvider.setPaused(true);
				changedLevels.clear();
				return;
		}*/
			changedLevels.add(new PLevel(levelType.ordinal(), value));
		}

		getPenManager().scheduleLevelEvent(this, changedLevels, wintabProvider.wintabAccess.getTime());
		changedLevels.clear();
	}

	private static final double PI_over_2=Math.PI/2;
	private static final double PI_over_2_over_900=PI_over_2/900; // (/10) and (/90)

	private float getMultRangedValue(PLevel.Type type) {
		if(PLevel.Type.TILT_TYPES.contains(type)) {
			// see tiltOnWintab.xoj
			int altitude=wintabProvider.wintabAccess.getValue(PLevel.Type.TILT_Y);
			if(altitude<0)
				altitude=-altitude; // when using the eraser the altitude is upside down
			if(altitude==900) // altitude values are given (in deg) multiplied by 10. Always 900 when tilt no supported by tablet.
				return 0;
			double betha=
			  altitude*PI_over_2_over_900;
			double theta=
			  wintabProvider.wintabAccess.getValue(PLevel.Type.TILT_X)*PI_over_2_over_900
			  -PI_over_2;
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

		return rangedValue;
	}
}
