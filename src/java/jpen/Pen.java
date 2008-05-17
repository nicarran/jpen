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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Queue;
import jpen.event.PenListener;

public class Pen extends PenState {

	private static final Logger L=Logger.getLogger(Pen.class.getName());
	public static final int DEFAULT_FREQUENCY=60;
	private int frequency;
	private MyThread thread;

	/** Tail of event queue. */
	PenEvent lastDispatchedEvent=new PenEvent(this) {
		    public static final long serialVersionUID=1l;
		    @Override
		    void dispatch() { }
		    @Override
		    void copyTo(PenState penState){}
	    }
	    ;
	/** Head of event queue. */
	private PenEvent lastScheduledEvent=lastDispatchedEvent;
	public final PenState lastScheduledState=new PenState();
	private final List<PenListener> listeners=new ArrayList<PenListener>();
	private PenListener[] listenersArray;

	private class MyThread
		extends Thread {
		final int period=1000/Pen.this.frequency;
		long beforeTime;
		long procTime;
		long waitTime;
		long availablePeriod;
		PenEvent event;
		volatile boolean waitedNewEvents;
		volatile boolean waitingNewEvents;
		{
			setName("jpen-Pen");
		}

		public synchronized void run() {
			try {
				while(Pen.this.thread==this) {
					waitedNewEvents=waitNewEvents();
					beforeTime=System.currentTimeMillis();
					if(waitedNewEvents)
						waitTime=0;
					else
						yield();
					while((event=lastDispatchedEvent.next)!=null) {
						event.copyTo(Pen.this);
						event.dispatch();
						lastDispatchedEvent.next=null;
						lastDispatchedEvent=event;
					}
					availablePeriod=period+waitTime;
					for(PenListener l:getListenersArray())
						l.penTock( availablePeriod - (System.currentTimeMillis()-beforeTime) );

					procTime=System.currentTimeMillis()-beforeTime;

					waitTime=period-procTime;
					if(waitTime>0) {
						wait(waitTime);
						waitTime=0;
					}
				}
			} catch(InterruptedException ex) {
				throw new Error(ex);
			}
		}

		private boolean waitNewEvents() throws InterruptedException {
			if(lastDispatchedEvent.next!=null)
				return false;
			waitingNewEvents=true;
			wait();
			waitingNewEvents=false;
			return true;
		}

		void processNewEvents() {
			if(waitingNewEvents)
				synchronized(this) {
					notify();
				}
		}
	}

	Pen() {
		setFrequency(DEFAULT_FREQUENCY);
	}

	public void setFrequency(int frequency) {
		if(frequency<=0)
			throw new IllegalArgumentException();
		stop();
		this.frequency=frequency;
		thread=new MyThread();
		thread.start();
	}

	private void stop() {
		if(thread!=null) {
			MyThread oldThread=thread;
			thread=null;
			oldThread.processNewEvents(); // may be waiting for new events.
			synchronized(oldThread) {
				oldThread.notify(); // may be waiting the frequency period
				try {
					oldThread.join();
				} catch(InterruptedException ex) {
					throw new Error(ex);
				}
			}
		}
		frequency=-1;
	}

	public int getFrequency() {
		return frequency;
	}

	public void addListener(PenListener l) {
		synchronized(listeners) {
			listeners.add(l);
			listenersArray=null;
		}
	}

	public void removeListener(PenListener l) {
		synchronized(listeners) {
			listeners.remove(l);
			listenersArray=null;
		}
	}

	PenListener[] getListenersArray() {
		if(listenersArray==null)
			synchronized(listeners) {
				listenersArray=listeners.toArray(new PenListener[listeners.size()]);
			}
		return listenersArray;
	}

	private static class PhantomLevelFilter {
		public static int THRESHOLD_PERIOD=200;
		private PenDevice lastDevice; // last device NOT filtered
		private PLevelEvent lastEvent; // last event scheduled
		boolean filteredFirstInSecuence;
		private long time;
		private long firstInSecuenceTime;

		boolean filter(PenDevice device) {
			if(!device.isDigitizer()) {
				time=System.currentTimeMillis();
				if(lastDevice!=null &&
				        lastDevice!=device &&
				        time-lastEvent.time<=THRESHOLD_PERIOD
				  )
					return true;
				if(!filteredFirstInSecuence) {
					L.fine("filtered first in sequence to prioritize digitized input in race");
					filteredFirstInSecuence=true;
					firstInSecuenceTime=System.currentTimeMillis();
					return true;
				}
				if(time-firstInSecuenceTime<=THRESHOLD_PERIOD){
					L.fine("filtering after the first for a period to allow digitized input to come and win in race");
					return true;
				}
				L.fine("digitized input going as event");
			} else
				filteredFirstInSecuence=false;
			lastDevice=device;
			return false;
		}

		void setLastEvent(PLevelEvent event) {
			this.lastEvent=event;
		}

		PLevelEvent getLastEvent() {
			return lastEvent;
		}
	}

	private final PhantomLevelFilter phantomLevelFilter=new PhantomLevelFilter();
	private final List<PLevel> scheduledLevels=new ArrayList<PLevel>();
	
	/**
	@deprecated Use {@link PenManager#scheduleLevelEvent(PenDevice, Collection)}.
	*/
	@Deprecated
	public boolean scheduleLevelEvent(PenDevice device, Collection<PLevel> levels) {
		return scheduleLevelEvent(device,levels, -Integer.MAX_VALUE, Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	final boolean scheduleLevelEvent(PenDevice device, Collection<PLevel> levels, int minX, int maxX, int minY, int maxY) {
		synchronized(scheduledLevels) {
			if(phantomLevelFilter.filter(device))
				return false;
			for(PLevel level:levels) {
				if(level.value==lastScheduledState.getLevelValue(level.typeNumber))
					continue;
				PLevel.Type levelType=level.getType();
				if(levelType!=null)
					switch(levelType){
					case X:
						if(level.value<minX || level.value>maxX)
							continue;
						break;
					case Y:
						if(level.value<minY || level.value>maxY)
							continue;
						break;
					default:
					}
				scheduledLevels.add(level);
				lastScheduledState.levels.setValue(level.typeNumber, level.value);
			}
			if(scheduledLevels.isEmpty())
				return false;
			int newKindTypeNumber=device.getKindTypeNumber();
			if(lastScheduledState.getKind().typeNumber!=newKindTypeNumber) {
				PKind newKind=PKind.valueOf(newKindTypeNumber);
				lastScheduledState.setKind(newKind);
				schedule(new PKindEvent(this, newKind));
			}
			PLevelEvent levelEvent=new PLevelEvent(this,
			    scheduledLevels.toArray(new PLevel[scheduledLevels.size()]));
			phantomLevelFilter.setLastEvent(levelEvent);
			schedule(levelEvent);
			scheduledLevels.clear();
			return true;
		}
	}

	void scheduleButtonReleasedEvents(){
		for(int i=PButton.Type.VALUES.size(); --i>=0;)
			scheduleButtonEvent(new PButton(i, false));
		for(Integer extButtonTypeNumber: lastScheduledState.extButtonTypeNumberToValue.keySet())
			scheduleButtonEvent(new PButton(extButtonTypeNumber, false));
	}

	private final Object buttonsLock=new Object();
	/**
	@deprecated Use {@link PenManager#scheduleButtonEvent(PButton)}.
	*/
	@Deprecated
	public void scheduleButtonEvent(PButton button) {
		synchronized(buttonsLock) {
			if(lastScheduledState.setButtonValue(button.typeNumber, button.value))
				schedule(new PButtonEvent(this, button));
		}
	}

	/**
	@deprecated Use {@link PenManager#scheduleScrollEvent(PScroll)}
	*/
	@Deprecated
	public void scheduleScrollEvent(PScroll scroll) {
		schedule(new PScrollEvent(this, scroll));
	}

	private final void schedule(PenEvent ev) {
		synchronized(lastScheduledEvent) {
			ev.time=System.currentTimeMillis();
			lastScheduledEvent.next=ev;
			lastScheduledEvent=ev;
			if(thread!=null) thread.processNewEvents();
		}
	}
}
