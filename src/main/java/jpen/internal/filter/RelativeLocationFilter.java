/* [{
Copyright 2010 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.internal.filter;

import java.awt.geom.Point2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.logging.Logger;
import jpen.PenDevice;
import jpen.PenProvider;
import jpen.PenState;
import jpen.PLevel;

public final class RelativeLocationFilter{
	private static final Logger L=Logger.getLogger(RelativeLocationFilter.class.getName());
	//static { L.setLevel(Level.ALL); }


	private PenDevice penDevice;
	private State state=State.UNDEFINED;
	public enum State{
		/**
		The filter tries to see if the values are absolute or relative and sets the state if possible.
		*/
		UNDEFINED,
		/**
		The filter considers the sample values to be in absolute mode.
		*/
		ABSOLUTE,
		/**
		The filter considers the sample values to be in relative mode and replace them with the reference (mouse pointer info).
		*/
		RELATIVE,
		/**
		The filter does nothing. This state is used when there is not mouse pointer info available on the system.
		*/
		OFF;
	}
	final Point2D.Float reference=new Point2D.Float();
	final SamplePoint samplePoint=new SamplePoint();
	static class SamplePoint
		implements Cloneable{
		PLevel levelX, levelY;
		boolean isComplete;

		boolean reset(Collection<PLevel> sample){
			levelX=levelY=null;
			int valuesCount=0;
		out:
			for(PLevel level: sample){
				switch(level.getType()){
				case X:
					valuesCount++;
					levelX=level;
					if(levelY!=null)
						break out;
					break;
				case Y:
					valuesCount++;
					levelY=level;
					if(levelX!=null)
						break out;
					break;
				}
			}
			isComplete=valuesCount==2;
			return valuesCount>0;
		}

		private void set(float x, float y){
			set(x, y, null);
		}

		private void set(float x, float y, Collection<PLevel> sample){
			if(levelX!=null)
				levelX.value=x;
			else if(sample!=null)
				sample.add(new PLevel(PLevel.Type.X, x));
			if(levelY!=null)
				levelY.value=y;
			else if(sample!=null)
				sample.add(new PLevel(PLevel.Type.Y, y));
		}

		@Override
		public SamplePoint clone(){
			try{
				SamplePoint clone=(SamplePoint)super.clone();
				clone.levelX=new PLevel(levelX);
				clone.levelY=new PLevel(levelY);
				return clone;
			}catch(CloneNotSupportedException ex){
				throw new AssertionError(ex);
			}
		}
	}

	final Point2D.Float deviation=new Point2D.Float();
	final Point2D.Float absDeviation=new Point2D.Float();
	private final Rule[] rules=new Rule[]{
				//new LogToFileRule(),
				new AbsoluteLocationRule(),
				new AbsoluteOnARowRule(),
				new RelativeOnSlopesRule(),
			};
	interface Rule{
		void reset();
		State evalFilterNextState(RelativeLocationFilter filter);
	}

	public void reset(){
		penDevice=null;
		resetRules();
	}

	private void resetRules(){
		for(Rule rule: rules){
			rule.reset();
		}
	}

	/**
	@return {@code true} if the state changed to a definitive value.
	*/
	public boolean filter(PenState penState, PenDevice penDevice, Collection<PLevel> sample, boolean levelsOnScreen){
		if(!levelsOnScreen) // only levelsOnScreen is supported
			return false;
		if(state.equals(State.OFF))
			return false;
		if(this.penDevice!=penDevice){
			this.penDevice=penDevice;
			state=State.UNDEFINED;
			resetRules();
		}
		if(state.equals(State.ABSOLUTE))
			return false;
		if(evalIsSystemMouseDevice(penDevice)) // the system device is trusted... no need to filter.
			return false;
		if(!samplePoint.reset(sample))
			return false;
		if(!setupReference())
			return true;

		boolean stateChanged=false;
		if(state.equals(State.UNDEFINED)){
			setupDeviation();
			stateChanged=evalStateFromRules();
		}
		switch(state){
		case ABSOLUTE:
			break;
		case RELATIVE:
			samplePoint.set(reference.x, reference.y, sample);
			break;
		case UNDEFINED:
			samplePoint.set(penState.getLevelValue(PLevel.Type.X),
											penState.getLevelValue(PLevel.Type.Y)); // then it won't cause a level event because movement
			break;
		default:
		}
		return stateChanged;
	}

	private static boolean evalIsSystemMouseDevice(PenDevice device){
		return device.getProvider().getConstructor().getPenManager().isSystemMouseDevice(device);
	}

	private boolean setupReference(){
		PointerInfo pointerInfo=AccessController.doPrivileged(getPointerInfoAction);
		if(pointerInfo==null){
			L.warning("No mouse found. Can not correct devices on relative (mouse) mode.");
			state=State.OFF;
			return false;
		}
		reference.setLocation(pointerInfo.getLocation());
		return true;
	}

	private final PrivilegedAction<PointerInfo> getPointerInfoAction=new PrivilegedAction<PointerInfo>(){
				//@Override
				public PointerInfo run(){
					return MouseInfo.getPointerInfo();
				}
			};

	private void setupDeviation(){
		if(samplePoint.levelX!=null){
			deviation.x=samplePoint.levelX.value-reference.x;
			absDeviation.x=Math.abs(deviation.x);
		}
		if(samplePoint.levelY!=null){
			deviation.y=samplePoint.levelY.value-reference.y;
			absDeviation.y=Math.abs(deviation.y);
		}
	}

	private boolean evalStateFromRules(){
		for(Rule rule: rules){
			State nextState=rule.evalFilterNextState(this);
			if(nextState!=null){
				this.state=nextState;
				return true;
			}
		}
		return false;
	}

	public State getState(){
		return state;
	}
}