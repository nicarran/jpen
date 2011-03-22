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
package jpen;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PenState
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public static class Levels implements java.io.Serializable {
		public static final long serialVersionUID=1l;
		private final float[] values=new float[PLevel.Type.VALUES.size()]; // CUSTOM type does not store value but VALUES does not have the CUSTOM
		private final Map<Integer, Float> extTypeNumberToValue=new HashMap<Integer, Float>();

		public void setValues(PenState penState){
			setValues(penState.levels);
		}

		public void setValues(PenState.Levels levels){
			for(int i=levels.values.length; --i>=0;)
				values[i]=levels.values[i];
			extTypeNumberToValue.clear();
			extTypeNumberToValue.putAll(levels.extTypeNumberToValue);
			for(int i=levels.values.length; i<values.length; i++){ // If a new PLevel.Type is added then transform the ext to this newer type
				Float value=extTypeNumberToValue.remove(i);
				if(value!=null)
					values[i]=value;
			}
		}

		public void setValues(PLevelEvent ev){
			setValues(ev.levels);
		}

		public final void setValues(Collection<PLevel> levels){
			for(PLevel level: levels){
				setValue(level);
			}
		}

		public final void setValues(PLevel[] levels){
			for(int i=levels.length; --i>=0; )
				setValue(levels[i]);
		}

		public final void setValue(PLevel level){
			setValue(level.typeNumber, level.value);
		}

		public final void setValue(PLevel.Type levelType, float value){
			setValue(levelType.ordinal(), value);
		}

		public final void setValue(int levelTypeNumber, float value){
			if(levelTypeNumber<values.length)
				values[levelTypeNumber]=value;
			else
				setExtValue(levelTypeNumber, value);
		}

		public float getValue(int levelTypeNumber) {
			return levelTypeNumber<values.length ?
						 values[levelTypeNumber]: getExtValue(levelTypeNumber);
		}

		public float getValue(PLevel.Type levelType){
			return getValue(levelType.ordinal());
		}

		private float getExtValue(int extLevelTypeNumber) {
			Float value=extTypeNumberToValue.get(extLevelTypeNumber);
			return value==null? 0f: value;
		}

		private final void setExtValue(int levelTypeNumber, float value){
			extTypeNumberToValue.put(levelTypeNumber, value);
		}
		
		@Override
		public String toString(){
			return "(values="+Arrays.toString(values)+", extTypeNumberToValue="+extTypeNumberToValue+")";
		}
	}

	private PKind kind=PKind.valueOf(PKind.Type.CURSOR);
	final Levels levels=new Levels();
	private final int[] buttonValues=new int[PButton.Type.VALUES.size()]; // CUSTOM type does not store value.
	final Map<Integer, Integer> extButtonTypeNumberToValue=new HashMap<Integer, Integer>();
	private int pressedButtonsCount;


	PenState(){}

	public float getLevelValue(PLevel.Type levelType) {
		return levels.getValue(levelType.ordinal());
	}

	public float getLevelValue(int levelTypeNumber) {
		return levels.getValue(levelTypeNumber);
	}

	public PKind getKind(){
		return kind;
	}

	void setKind(PKind kind) {
		this.kind=kind;
	}

	public boolean getButtonValue(PButton.Type buttonType) {
		return getButtonValue(buttonType.ordinal());
	}

	public boolean getButtonValue(int  buttonTypeNumber) {
		return (buttonTypeNumber>=buttonValues.length? getExtButtonValue(buttonTypeNumber): buttonValues[buttonTypeNumber]) > 0;
	}

	public boolean hasPressedButtons(){
		return pressedButtonsCount>0;
	}

	public int getPressedButtonsCount(){
		return pressedButtonsCount;
	}

	private int getExtButtonValue(int buttonTypeNumber) {
		Integer buttonValue=extButtonTypeNumberToValue.get(buttonTypeNumber);
		return buttonValue==null? 0: buttonValue;
	}

	boolean setButtonValue(int buttonTypeNumber, boolean value) {
		boolean oldValue=getButtonValue(buttonTypeNumber);
		if(buttonTypeNumber>=buttonValues.length)
			setExtButtonValue(buttonTypeNumber, value);
		else {
			if(value){
				if(buttonValues[buttonTypeNumber]==0)
					pressedButtonsCount++;
				buttonValues[buttonTypeNumber]++;
			}
			else{
				if(buttonValues[buttonTypeNumber]>0)
					pressedButtonsCount--;
				buttonValues[buttonTypeNumber]=0;
			}
		}
		return oldValue!=getButtonValue(buttonTypeNumber);
	}

	private void setExtButtonValue(int buttonTypeNumber, boolean value) {
		int currentValue=getExtButtonValue(buttonTypeNumber);
		if(value){
			if(currentValue==0)
				pressedButtonsCount++;
			extButtonTypeNumberToValue.put(buttonTypeNumber, getExtButtonValue(buttonTypeNumber)+1);
		}
		else{
			if(currentValue>0)
				pressedButtonsCount--;
			extButtonTypeNumberToValue.put(buttonTypeNumber, 0);
		}
	}

	void setValues(PenState penState){
		levels.setValues(penState.levels);

		for(int i=buttonValues.length; --i>=0;)
			buttonValues[i]=penState.buttonValues[i];
		extButtonTypeNumberToValue.clear();
		extButtonTypeNumberToValue.putAll(penState.extButtonTypeNumberToValue);

		kind=penState.kind;
	}

}