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
		public final int levelType;
		public final float onPressValue;
		public final float onReleaseValue;

		public ButtonTriggerPolicy(PLevel.Type levelType, float onPressValue, float onReleaseValue){
			this(levelType.ordinal(), onPressValue, onReleaseValue);
		}

		public ButtonTriggerPolicy(int levelType, float onPressValue, float onReleaseValue){
			this.levelType=levelType;
			this.onPressValue=onPressValue;
			this.onReleaseValue=onReleaseValue;
		}

		@Override
		public String toString(){
			return "( levelType="+levelType+", onPressValue="+onPressValue+", onReleaseValue="+onReleaseValue+" )";
		}
	}

	final Pen pen;

	PLevelEmulator(Pen pen){
		this.pen=pen;
	}

	private final List<List<ButtonTriggerPolicy>> kindTypeToButtonTypeToButtonTriggerPolicy=new ArrayList<List<ButtonTriggerPolicy>>();
	private final List<ButtonTriggerPolicy> activeButtonTriggerPolicies=new ArrayList<ButtonTriggerPolicy>();
	private final BitSet activeLevelTypes=new BitSet();
	private final List<ButtonTriggerPolicy> kindTypeToAlwaysActiveButtonTriggerPolicy=new ArrayList<ButtonTriggerPolicy>();

	public void setPressureTriggerForLeftCursorButton(float pressure){
		setTriggerForLeftCursorButton(new ButtonTriggerPolicy(PLevel.Type.PRESSURE.ordinal(),pressure,0f));
	}

	public void setTriggerForLeftCursorButton(ButtonTriggerPolicy triggerPolicy){
		setTrigger(PKind.Type.CURSOR.ordinal(), PButton.Type.LEFT.ordinal(), triggerPolicy, true);
	}

	public void setTrigger(PKind.Type kindType, PButton.Type buttonType, ButtonTriggerPolicy triggerPolicy){
		setTrigger(kindType.ordinal(), buttonType.ordinal(), triggerPolicy);
	}

	public synchronized void setTrigger(int kindType, int buttonType, ButtonTriggerPolicy triggerPolicy){
		setTrigger(kindType, buttonType, triggerPolicy, false);
	}

	public synchronized void setTrigger(int kindType, int buttonType, ButtonTriggerPolicy triggerPolicy, boolean alwaysActiveOnKind){
		List<ButtonTriggerPolicy> buttonTypeToButtonTriggerPolicy=getButtonTypeToButtonTriggerPolicy(kindType);
		ensureListSize(buttonTypeToButtonTriggerPolicy, buttonType);
		ButtonTriggerPolicy oldPolicy=buttonTypeToButtonTriggerPolicy.set(buttonType, triggerPolicy);

		ensureListSize(kindTypeToAlwaysActiveButtonTriggerPolicy, kindType);
		if(oldPolicy!=null){
			if(kindTypeToAlwaysActiveButtonTriggerPolicy.get(kindType)==oldPolicy)
				kindTypeToAlwaysActiveButtonTriggerPolicy.set(kindType, null);
		}
		if(triggerPolicy!=null && alwaysActiveOnKind){
			kindTypeToAlwaysActiveButtonTriggerPolicy.set(kindType, triggerPolicy);
		}
	}

	public ButtonTriggerPolicy getButtonTriggerPolicy(PKind.Type kindType, PButton.Type buttonType){
		return getButtonTriggerPolicy(kindType, buttonType);
	}

	public synchronized ButtonTriggerPolicy getButtonTriggerPolicy(int kindType, int buttonType){
		List<ButtonTriggerPolicy> buttonTypeToButtonTriggerPolicy=getButtonTypeToButtonTriggerPolicy(kindType);
		ensureListSize(buttonTypeToButtonTriggerPolicy, buttonType);
		return buttonTypeToButtonTriggerPolicy.get(buttonType);
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
		if(emulatedLevel!=null){
			pen.penManager.scheduleLevelEvent(pen.penManager.emulationDevice, buttonEvent.time, Collections.singleton(emulatedLevel));
		}
	}

	private PLevel emulateOnPress(int buttonType){
		PenState lastScheduledState=pen.lastScheduledState;
		ButtonTriggerPolicy triggerPolicy=getButtonTriggerPolicy(
					lastScheduledState.getKind().typeNumber,
					buttonType);
		if(L.isLoggable(Level.FINE)) L.fine("triggerPolicy: "+triggerPolicy+", buttonType: "+buttonType);
		if(triggerPolicy!=null){
			setActiveButtonTriggerPolicy(buttonType, triggerPolicy);
			if(lastScheduledState.getLevelValue(triggerPolicy.levelType)==
				 triggerPolicy.onPressValue)
				return null;
			return new PLevel(triggerPolicy.levelType, triggerPolicy.onPressValue);
		}
		return null;
	}

	private void setActiveButtonTriggerPolicy(int buttonType, ButtonTriggerPolicy policy){
		ensureListSize(activeButtonTriggerPolicies, buttonType);
		ButtonTriggerPolicy oldPolicy=activeButtonTriggerPolicies.set(buttonType, policy);
		if(oldPolicy!=null){
			activeLevelTypes.set(oldPolicy.levelType, false);
		}
		if(policy!=null){
			activeLevelTypes.set(policy.levelType, true);
		}
	}

	private PLevel emulateOnRelease(int buttonType){
		ensureListSize(activeButtonTriggerPolicies, buttonType);
		ButtonTriggerPolicy triggerPolicy=getActiveButtonTriggerPolicy(buttonType);
		if(L.isLoggable(Level.FINE)) L.fine("triggerPolicy: "+triggerPolicy+", buttonType: "+buttonType);
		if(triggerPolicy!=null){
			setActiveButtonTriggerPolicy(buttonType, null);
			PLevel pLevel=new PLevel(triggerPolicy.levelType, triggerPolicy.onReleaseValue);
			return pLevel;
		}
		return null;
	}

	private ButtonTriggerPolicy getActiveButtonTriggerPolicy(int buttonType){
		ensureListSize(activeButtonTriggerPolicies, buttonType);
		ButtonTriggerPolicy buttonTriggerPolicy=activeButtonTriggerPolicies.get(buttonType);
		return buttonTriggerPolicy;
	}

	boolean onActivePolicy(int kindType, int levelType){
		return onAlwaysActivePolicy(kindType, levelType) || activeLevelTypes.get(levelType);
	}

	private boolean onAlwaysActivePolicy(int kindType, int levelType){
		ensureListSize(kindTypeToAlwaysActiveButtonTriggerPolicy, kindType);
		ButtonTriggerPolicy alwaysActivePolicy=kindTypeToAlwaysActiveButtonTriggerPolicy.get(kindType);
		return alwaysActivePolicy==null ? false :
					 alwaysActivePolicy.levelType==levelType;
	}
}