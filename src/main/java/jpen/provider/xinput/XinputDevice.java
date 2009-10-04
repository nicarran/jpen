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
package jpen.provider.xinput;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpen.PButton;
import jpen.PButtonEvent;
import jpen.Pen;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PKind;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.provider.AbstractPenDevice;
import jpen.provider.Utils;
import jpen.provider.VirtualScreenBounds;
import jpen.PScroll;
import jpen.PScrollEvent;
import static jpen.provider.xinput.XiDevice.*;

class XinputDevice extends AbstractPenDevice {
	private static final Logger L=Logger.getLogger(XinputDevice.class.getName());
	//static{L.setLevel(Level.ALL);}

	public final XiDevice device;
	private final PLevel.Range[] levelRanges;
	private final XinputProvider xinputProvider;
	private final Point2D.Float componentLocation=new Point2D.Float();
	private final Dimension componentSize=new Dimension();

	XinputDevice(XinputProvider xinputProvider, XiDevice device) {
		super(xinputProvider);
		this.device=device;
		this.xinputProvider=xinputProvider;
		levelRanges=new PLevel.Range[PLevel.Type.VALUES.size()];
		resetLevelRanges();
		setKindTypeNumber(getDefaultKindTypeNumber());
		setEnabled(true);
	}

	//@Override
	public String getName() {
		return device.getName();
	}

	void resetLevelRanges(){
		device.refreshLevelRanges();
		for(int i=PLevel.Type.VALUES.size(); --i>=0; ){
			PLevel.Type levelType=PLevel.Type.VALUES.get(i);
			levelRanges[levelType.ordinal()]=device.getLevelRange(levelType);
		}
	}

	void reset(){
		while(device.nextEvent())
			;
		resetLevelRanges();
	}

	private int getDefaultKindTypeNumber() {
		String lowerCaseName=getName().toLowerCase();
		if(lowerCaseName.indexOf("eraser")!=-1)
			return PKind.Type.ERASER.ordinal();
		else if(lowerCaseName.indexOf("cursor")!=-1)
			return PKind.Type.CURSOR.ordinal();
		else if(lowerCaseName.indexOf("pad")!=-1)
			return PKind.Type.IGNORE.ordinal();
		else
			return PKind.Type.STYLUS.ordinal();
	}

	void processQuedEvents() {
		if(!getEnabled())
			return;
		while(device.nextEvent()) {
			EventType eventType=device.getLastEventType();
			switch(eventType) {
			case BUTTON_PRESS:
				int lastEventButton=device.getLastEventButton();
				if( lastEventButton ==4 || lastEventButton ==5 ){
					//scheduleScrollEvent(lastEventButton); nicarran: the mouse provider catches this.
				}
				else
					scheduleButtonEvent(lastEventButton-1, true);
				break;
			case BUTTON_RELEASE:
				lastEventButton=device.getLastEventButton();
				if( lastEventButton !=4 && lastEventButton !=5)
					scheduleButtonEvent(lastEventButton, false);
				break;
			case MOTION_NOTIFY:
				scheduleLevelEvent();
			}
		}
	}

	private void scheduleScrollEvent(int number) {
		getPenManager().scheduleScrollEvent(this, new PScroll(number==5? PScroll.Type.DOWN.ordinal(): PScroll.Type.UP.ordinal(),1));
	}

	private final List<PLevel> changedLevels=new ArrayList<PLevel>();
	private void scheduleLevelEvent() {
		for(int i=PLevel.Type.VALUES.size(); --i>=0;) {
			PLevel.Type levelType=PLevel.Type.VALUES.get(i);
			float value=getMultRangedValue(levelType);
			changedLevels.add(new PLevel(levelType.ordinal(), value));
		}
		getPenManager().scheduleLevelEvent(this, changedLevels, device.getLastEventTime(), true);
		changedLevels.clear();
	}

	void scheduleButtonEvent(int number, boolean state) {
		/*
		it fires different numbers for press and release. : (  TODO: support the pad buttons.
		if(L.isLoggable(Level.FINE))
			L.fine("scheduling button event: number="+number+", state="+state);
		getPenManager().scheduleButtonEvent(new PButton(number, state));*/
	}

	private static final float RADS_PER_DEG=(float)(Math.PI/180);

	private final float getMultRangedValue(PLevel.Type levelType) {
		float devValue=device.getValue(levelType);

		if(PLevel.Type.TILT_TYPES.contains(levelType))
			return devValue*RADS_PER_DEG;

		devValue=levelRanges[levelType.ordinal()].getRangedValue(devValue);

		if(PLevel.Type.MOVEMENT_TYPES.contains(levelType))
			devValue=xinputProvider.screenBounds.getLevelRangeOffset(levelType)+
			         devValue*xinputProvider.screenBounds.getLevelRangeMult(levelType);

		return devValue;
	}
}