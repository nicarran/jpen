/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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

import java.util.ArrayList;
import java.util.List;
import jpen.PButton;
import jpen.PKind;

public final class PLevelEmulator{

	public static class ButtonTriggerPolicy{
		public final int levelTypeNumber;
		public final float onPressValue;
		public final float onReleaseValue;

		public ButtonTriggerPolicy(PLevel.Type levelType, float onPressValue, float onReleaseValue){
			this(levelType.ordinal(), onPressValue, onReleaseValue);
		}

		public ButtonTriggerPolicy(int levelTypeNumber, float onPressValue, float onReleaseValue){
			this.levelTypeNumber=levelTypeNumber;
			this.onPressValue=onPressValue;
			this.onReleaseValue=onReleaseValue;
		}
	}

	PLevelEmulator(){
	}

	private final List<List<ButtonTriggerPolicy>> kindTypeToButtonTypeToButtonTriggerPolicy=new ArrayList<List<ButtonTriggerPolicy>>();

	public void setPressureTriggerForLeftCursorButton(float pressure){
		setTriggerForLeftCursorButton(new ButtonTriggerPolicy(PLevel.Type.PRESSURE,pressure,0f));
	}

	public void setTriggerForLeftCursorButton(ButtonTriggerPolicy triggerPolicy){
		setTrigger(PKind.Type.CURSOR, PButton.Type.LEFT, triggerPolicy);
	}

	public void setTrigger(PKind.Type kindType, PButton.Type buttonType, ButtonTriggerPolicy triggerPolicy){
		setTrigger(kindType.ordinal(), buttonType.ordinal(), triggerPolicy);
	}

	public void setTrigger(int kindTypeNumber, int buttonTypeNumber, ButtonTriggerPolicy triggerPolicy){
		List<ButtonTriggerPolicy> buttonTypeToButtonTriggerPolicy=getButtonTypeToButtonTriggerPolicy(kindTypeNumber);
		ensureListSize(buttonTypeToButtonTriggerPolicy, buttonTypeNumber);
		buttonTypeToButtonTriggerPolicy.set(buttonTypeNumber, triggerPolicy);
	}

	public ButtonTriggerPolicy getButtonTriggerPolicy(PKind.Type kindType, PButton.Type buttonType){
		return getButtonTriggerPolicy(kindType, buttonType);
	}

	public ButtonTriggerPolicy getButtonTriggerPolicy(int kindTypeNumber, int buttonTypeNumber){
		List<ButtonTriggerPolicy> buttonTypeToButtonTriggerPolicy=getButtonTypeToButtonTriggerPolicy(kindTypeNumber);
		ensureListSize(buttonTypeToButtonTriggerPolicy, buttonTypeNumber);
		return buttonTypeToButtonTriggerPolicy.get(buttonTypeNumber);
	}
	
	void scheduleEmulatedEvent(PButtonEvent buttonEvent){
		PLevel level=emulateLevel(buttonEvent);
		if(level==null)
			return;
		if(buttonEvent.pen.getLevelFilter().filterPenLevel(level))
			return;
		PLevelEvent levelEvent=new PLevelEvent(buttonEvent.pen,
		    new PLevel[]{level},
		    (byte)-1,
		    buttonEvent.time);
		buttonEvent.pen.lastScheduledState.levels.setValue(level.typeNumber, level.value);
		buttonEvent.pen.schedule(levelEvent);
	}

	private PLevel emulateLevel(PButtonEvent buttonEvent){
		PenState lastScheduledState=buttonEvent.pen.lastScheduledState;
		PLevelEmulator.ButtonTriggerPolicy triggerPolicy=getButtonTriggerPolicy(
		      lastScheduledState.getKind().typeNumber,
		      buttonEvent.button.typeNumber);
		if(triggerPolicy!=null){
			float levelValue=buttonEvent.button.value? triggerPolicy.onPressValue: triggerPolicy.onReleaseValue;
			if(lastScheduledState.getLevelValue(triggerPolicy.levelTypeNumber)==
			        levelValue)
				return null;
			return new PLevel(triggerPolicy.levelTypeNumber, levelValue);
		}
		return null;
	}

	private static void ensureListSize(List<?> list, int index){
		while(list.size()<=index)
			list.add(null);
	}

	private List<ButtonTriggerPolicy> getButtonTypeToButtonTriggerPolicy(int kindType){
		ensureListSize(kindTypeToButtonTypeToButtonTriggerPolicy, kindType);
		List<ButtonTriggerPolicy> buttonTypeToButtonTriggerPolicy=kindTypeToButtonTypeToButtonTriggerPolicy.get(kindType);
		if(buttonTypeToButtonTriggerPolicy==null){
			kindTypeToButtonTypeToButtonTriggerPolicy.set(kindType, buttonTypeToButtonTriggerPolicy=new ArrayList<ButtonTriggerPolicy>());
		}
		return buttonTypeToButtonTriggerPolicy;
	}

}
