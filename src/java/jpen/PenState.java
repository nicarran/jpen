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

public class PenState
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	static class Levels implements java.io.Serializable {
		public static final long serialVersionUID=1l;
		private final float[] values=new float[PLevel.Type.values().length];
		private final Map<Integer, Float> extTypeNumberToValue=new HashMap<Integer, Float>();

		public void setValues(PenState.Levels levels){
			for(int i=values.length; --i>=0;)
				values[i]=levels.values[i];
			extTypeNumberToValue.clear();
			extTypeNumberToValue.putAll(levels.extTypeNumberToValue);
		}

		public void setValues(PLevelEvent ev){
			for(PLevel level:ev.levels)
				setValue(level);
		}

		public void setValues(PenState penState){
			setValues(penState.levels);
		}

		public float getValue(PLevel.Type levelType) {
			return getValue(levelType.ordinal());
		}

		public float getValue(int levelTypeNumber) {
			if(levelTypeNumber>=values.length)
				return getExtValue(levelTypeNumber);
			return values[levelTypeNumber];
		}

		private float getExtValue(int extLevelTypeNumber) {
			Float value=extTypeNumberToValue.get(extLevelTypeNumber);
			return value==null? 0f: value;
		}

		void setValue(PLevel level) {
			setValue(level.typeNumber, level.value);
		}

		public void setValue(PLevel.Type levelType, float value){
			setValue(levelType.ordinal(), value);
		}

		public final void setValue(int levelTypeNumber, float value){
			if(levelTypeNumber>=values.length) {
				setExtValue(levelTypeNumber, value);
				return;
			}
			values[levelTypeNumber]=value;
		}

		private final void setExtValue(int levelTypeNumber, float value){
			extTypeNumberToValue.put(levelTypeNumber, value);
		}
	}

	private int kindTypeNumber;
	final Levels levels=new Levels();
	private final int[] buttonValues=new int[PButton.Type.values().length];
	private final Map<Integer, Integer> extButtonTypeNumberToValue=new HashMap<Integer, Integer>();

	public PenState(){}

	public PenState(PenState penState){
		setValues(penState);
	}

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
		return levels.getValue(levelType);
	}

	public float getLevelValue(int levelTypeNumber) {
		return levels.getValue(levelTypeNumber);
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

	public void setValues(PenEvent ev){
		ev.copyTo(this);
	}

	public void setValues(PenState penState){
		levels.setValues(penState.levels);

		for(int i=buttonValues.length; --i>=0;)
			buttonValues[i]=penState.buttonValues[i];
		extButtonTypeNumberToValue.clear();
		extButtonTypeNumberToValue.putAll(penState.extButtonTypeNumberToValue);

		kindTypeNumber=penState.kindTypeNumber;
	}
}
