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
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpen.PButton;
import jpen.PKind;

public final class PLevelEmulator{
	static final Logger L=Logger.getLogger(PLevelEmulator.class.getName());
	//static { L.setLevel(Level.ALL); }

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

		@Override
		public String toString(){
			return "( levelTypeNumber="+levelTypeNumber+", onPressValue="+onPressValue+", onReleaseValue="+onReleaseValue+" )";
		}
	}

	final PenManager penManager;

	PLevelEmulator(PenManager penManager){
		this.penManager=penManager;
	}

	private final List<List<ButtonTriggerPolicy>> kindTypeToButtonTypeToButtonTriggerPolicy=new ArrayList<List<ButtonTriggerPolicy>>();
	private final List<ButtonTriggerPolicy> activeButtonTriggerPolicies=new ArrayList<ButtonTriggerPolicy>();
	private final BitSet activeLevelTypes=new BitSet();

	public void setPressureTriggerForLeftCursorButton(float pressure){
		setTriggerForLeftCursorButton(new ButtonTriggerPolicy(PLevel.Type.PRESSURE,pressure,0f));
	}

	public void setTriggerForLeftCursorButton(ButtonTriggerPolicy triggerPolicy){
		setTrigger(PKind.Type.CURSOR, PButton.Type.LEFT, triggerPolicy);
	}

	public void setTrigger(PKind.Type kindType, PButton.Type buttonType, ButtonTriggerPolicy triggerPolicy){
		setTrigger(kindType.ordinal(), buttonType.ordinal(), triggerPolicy);
	}

	public synchronized void setTrigger(int kindTypeNumber, int buttonTypeNumber, ButtonTriggerPolicy triggerPolicy){
		List<ButtonTriggerPolicy> buttonTypeToButtonTriggerPolicy=getButtonTypeToButtonTriggerPolicy(kindTypeNumber);
		ensureListSize(buttonTypeToButtonTriggerPolicy, buttonTypeNumber);
		buttonTypeToButtonTriggerPolicy.set(buttonTypeNumber, triggerPolicy);
	}

	public ButtonTriggerPolicy getButtonTriggerPolicy(PKind.Type kindType, PButton.Type buttonType){
		return getButtonTriggerPolicy(kindType, buttonType);
	}

	public synchronized ButtonTriggerPolicy getButtonTriggerPolicy(int kindTypeNumber, int buttonTypeNumber){
		List<ButtonTriggerPolicy> buttonTypeToButtonTriggerPolicy=getButtonTypeToButtonTriggerPolicy(kindTypeNumber);
		ensureListSize(buttonTypeToButtonTriggerPolicy, buttonTypeNumber);
		return buttonTypeToButtonTriggerPolicy.get(buttonTypeNumber);
	}

	private List<ButtonTriggerPolicy> getButtonTypeToButtonTriggerPolicy(int kindType){
		ensureListSize(kindTypeToButtonTypeToButtonTriggerPolicy, kindType);
		List<ButtonTriggerPolicy> buttonTypeToButtonTriggerPolicy=kindTypeToButtonTypeToButtonTriggerPolicy.get(kindType);
		if(buttonTypeToButtonTriggerPolicy==null){
			kindTypeToButtonTypeToButtonTriggerPolicy.set(kindType, buttonTypeToButtonTriggerPolicy=new ArrayList<ButtonTriggerPolicy>());
		}
		return buttonTypeToButtonTriggerPolicy;
	}

	private static void ensureListSize(List<?> list, int index){
		while(list.size()<=index)
			list.add(null);
	}

	void scheduleEmulatedEvent(PButtonEvent buttonEvent){
		PLevel emulatedLevel=null;
		if(buttonEvent.button.value)
			emulatedLevel=emulateOnPress(buttonEvent.button.typeNumber);
		else
			emulatedLevel=emulateOnRelease(buttonEvent.button.typeNumber);
		if(emulatedLevel!=null)
			penManager.scheduleLevelEvent(null, Collections.singleton(emulatedLevel), buttonEvent.time);
	}

	private PLevel emulateOnPress(int buttonTypeNumber){
		PenState lastScheduledState=penManager.pen.lastScheduledState;
		ButtonTriggerPolicy triggerPolicy=getButtonTriggerPolicy(
		      lastScheduledState.getKind().typeNumber,
		      buttonTypeNumber);
		if(L.isLoggable(Level.FINE)) L.fine("triggerPolicy: "+triggerPolicy);
		if(triggerPolicy!=null){
			if(lastScheduledState.getLevelValue(triggerPolicy.levelTypeNumber)==
			        triggerPolicy.onPressValue)
				return null;
			setActiveButtonTriggerPolicy(buttonTypeNumber, triggerPolicy);
			return new PLevel(triggerPolicy.levelTypeNumber, triggerPolicy.onPressValue);
		}
		return null;
	}

	private void setActiveButtonTriggerPolicy(int buttonTypeNumber, ButtonTriggerPolicy policy){
		ensureListSize(activeButtonTriggerPolicies, buttonTypeNumber);
		ButtonTriggerPolicy oldValue=activeButtonTriggerPolicies.set(buttonTypeNumber, policy);
		if(oldValue!=null)
			activeLevelTypes.set(oldValue.levelTypeNumber, false);
		if(policy!=null){
			activeLevelTypes.set(policy.levelTypeNumber, true);
		}
	}

	private PLevel emulateOnRelease(int buttonTypeNumber){
		ensureListSize(activeButtonTriggerPolicies, buttonTypeNumber);
		ButtonTriggerPolicy triggerPolicy=getActiveButtonTriggerPolicy(buttonTypeNumber);
		if(triggerPolicy!=null){
			setActiveButtonTriggerPolicy(buttonTypeNumber, null);
			return new PLevel(triggerPolicy.levelTypeNumber, triggerPolicy.onReleaseValue);
		}
		return null;
	}

	private ButtonTriggerPolicy getActiveButtonTriggerPolicy(int buttonTypeNumber){
		ensureListSize(activeButtonTriggerPolicies, buttonTypeNumber);
		return activeButtonTriggerPolicies.get(buttonTypeNumber);
	}

	boolean isActiveLevel(int levelTypeNumber){
		return activeLevelTypes.get(levelTypeNumber); 
	}
}
