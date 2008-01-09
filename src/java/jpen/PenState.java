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

import java.util.HashMap;
import java.util.Map;

public class PenState {
	private int kindTypeNumber;
	private final float[] levelValues=new float[PLevel.Type.values().length];
	private final Map<Integer, Float> extLevelTypeNumberToValue=new HashMap<Integer, Float>();
	private final int[] buttonValues=new int[PButton.Type.values().length];
	private final Map<Integer, Integer> extButtonTypeNumberToValue=new HashMap<Integer, Integer>();

	public int getKindTypeNumber() {
		return kindTypeNumber;
	}

	public PKind.Type getKindType() {
		return PKind.Type.valueOf(kindTypeNumber);
	}

	void setKindTypeNumber(int kindTypeNumber) {
		this.kindTypeNumber=kindTypeNumber;
	}

	public float getLevelValue(PLevel.Type levelType) {
		return getLevelValue(levelType.ordinal());
	}

	public float getLevelValue(int levelTypeNumber) {
		if(levelTypeNumber>=levelValues.length)
			return getExtLevelValue(levelTypeNumber);
		return levelValues[levelTypeNumber];
	}

	private float getExtLevelValue(int extLevelTypeNumber) {
		Float levelValue=extLevelTypeNumberToValue.get(extLevelTypeNumber);
		return levelValue==null? 0f: levelValue;
	}

	void setLevelValue(PLevel level) {
		if(level.typeNumber>=levelValues.length) {
			setExtLevelValue(level);
			return;
		}
		levelValues[level.typeNumber]=level.value;
	}

	private void setExtLevelValue(PLevel level) {
		extLevelTypeNumberToValue.put(level.typeNumber, level.value);
	}

	public boolean getButtonValue(PButton.Type buttonType) {
		return getButtonValue(buttonType.ordinal());
	}

	public boolean getButtonValue(int  buttonTypeNumber) {
		return (buttonTypeNumber>=buttonValues.length? getExtButtonValue(buttonTypeNumber): buttonValues[buttonTypeNumber]) > 0;
	}

	private int getExtButtonValue(int buttonTypeNumber) {
		Integer buttonValue=extButtonTypeNumberToValue.get(buttonTypeNumber);
		return buttonValue==null? 0: buttonValue;
	}

	boolean setButtonValue(PButton button) {
		boolean oldValue=getButtonValue(button.typeNumber);
		if(button.typeNumber>=buttonValues.length)
			setExtButtonValue(button);
		else {
			if(button.value)
				buttonValues[button.typeNumber]++;
			else
				buttonValues[button.typeNumber]=0;
		}
		return oldValue!=getButtonValue(button.typeNumber);
	}

	private void setExtButtonValue(PButton button) {
		if(button.value)
			extButtonTypeNumberToValue.put(button.typeNumber, getExtButtonValue(button.typeNumber)+1);
		else
			extButtonTypeNumberToValue.put(button.typeNumber, 0);
	}
}
