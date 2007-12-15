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
package jpen.provider.wintab;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jpen.PButton;
import jpen.PButtonEvent;
import jpen.Pen;
import jpen.PKind;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.provider.AbstractPenDevice;
import jpen.provider.Utils;

class WintabDevice
	extends AbstractPenDevice {
	final WintabProvider wintabProvider;
	public final int cursor;
	private int lastButtonsValues;
	private final Point2D.Float componentLocation=new Point2D.Float();
	private final Dimension componentSize=new Dimension();

	WintabDevice(WintabProvider wintabProvider, int cursor) {
		super(wintabProvider);
		this.wintabProvider=wintabProvider;
		this.cursor=cursor;
		setKindTypeNumber(getDefaultKindTypeNumber());
		setEnabled(true);
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
		return wintabProvider.wintabAccess.getCursorName(cursor);
	}

	void scheduleEvents() {
		if(!getEnabled())
			return;
		scheduleLevelEvent();
		scheduleButtonEvents();
	}

	private void scheduleButtonEvents() {
		int newButtonsValues=wintabProvider.wintabAccess.getButtons();
		if(newButtonsValues==lastButtonsValues)
			return;
		for(PButton.Type buttonType:PButton.Type.values()) {
			boolean value=getButtonState(newButtonsValues, getButtonIndex(buttonType));
			getPen().scheduleButtonEvent(new PButton(buttonType.ordinal(), value));
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
		for(PLevel.Type levelType:PLevel.Type.values()) {
			float value=PLevel.getCoordinateValueInsideComponent(
			              getComponent().getSize(componentSize), componentLocation,  levelType,  getMultRangedValue(levelType));
			if(value<0) {
				wintabProvider.setPaused(true);
				changedLevels.clear();
				return;
			}
			changedLevels.add(new PLevel(levelType.ordinal(), value));
		}

		if(getPen().scheduleLevelEvent(this, changedLevels))
			wintabProvider.moveMouseToLastScheduledLocation(componentLocation);
		changedLevels.clear();
	}

	private float getRangedValue(PLevel.Type type) {
		float rangedValue=wintabProvider.getLevelRange(type).getRangedValue(
		                    wintabProvider.wintabAccess.getValue(type));
		return type.equals(PLevel.Type.Y)? 1f-rangedValue: rangedValue;
	}

	private float getMultRangedValue(PLevel.Type type) {
		return getRangedValue(type)*wintabProvider.getLevelRangeMult(type);
	}
}
