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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Queue;
import javax.swing.SwingUtilities;
import jpen.event.PenListener;

public class Pen extends PenState {
	private static final Logger L=Logger.getLogger(Pen.class.getName());
	//static{L.setLevel(Level.ALL);}

	public static final int DEFAULT_FREQUENCY=60; // TODO: 50 is a better default or less??

	private int frequency;
	private volatile MyThread thread;

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
	private volatile PenListener[] listenersArray;
	private boolean firePenTockOnSwing;
	public final PLevelEmulator levelEmulator=new PLevelEmulator();
	private PLevelFilter levelFilter=PLevelFilter.AllowAll.INSTANCE;

	private final class MyThread
		extends Thread {
		final int period;
		long beforeTime;
		long procTime;
		long waitTime;
		long availablePeriod;
		PenEvent event;
		boolean waitedNewEvents;
		Exception exception;
		private final Waiter waiter=new Waiter();
		volatile boolean stopRunning;
		Thread oldThread;

		final class Waiter
			extends Object{
			synchronized void doWait(long timeout) throws InterruptedException{
				wait(timeout);
			}
			synchronized void doNotify(){
				notify();
			}
		}

		MyThread(Thread oldThread){
			period=1000/Pen.this.frequency;
			this.oldThread=oldThread;
			setName("jpen-Pen-"+period+"ms");
		}
		private final Runnable penTockFirer=new Runnable(){
			    //@Override
			    public void run(){
				    for(PenListener l:getListenersArray())
					    l.penTock( availablePeriod - (System.currentTimeMillis()-beforeTime) );
			    }
		    };

		public void run() {
			try {
				L.fine("v");
				if(oldThread!=null)
					oldThread.join();
				oldThread=null;
				while(!stopRunning) {
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
					firePenTock();
					procTime=System.currentTimeMillis()-beforeTime;
					waitTime=period-procTime;
					if(waitTime>0) {
						waiter.doWait(waitTime);
						waitTime=0;
					}
				}
			} catch(Exception ex) {
				L.warning("jpen-Pen thread threw an exception: "+Utils.evalStackTrace(ex));
				exception=ex;
			}
			L.fine("^");
		}

		private boolean waitNewEvents() throws InterruptedException {
			if(lastDispatchedEvent.next!=null)
				return false;
			waiter.doWait(0);
			return true;
		}

		private void firePenTock() throws InterruptedException, InvocationTargetException{
			if(getListenersArray().length==0)
				return;
			if(firePenTockOnSwing)
				SwingUtilities.invokeAndWait(penTockFirer);
			else
				penTockFirer.run();
		}

		void processNewEvents() {
			waiter.doNotify();
		}

		void stop(boolean join){
			stopRunning=true;
			processNewEvents(); // because it may be waiting for new events.
			if(join)
				try{
					join();
				}
				catch(InterruptedException ex) {
					throw new Error(ex);
				}
		}
	}

	Pen() {
		setFrequencyLater(DEFAULT_FREQUENCY);
	}
	
	public PLevelFilter getLevelFilter(){
		return levelFilter;
	}
	
	public void setLevelFilter(PLevelFilter levelFilter){
		this.levelFilter=levelFilter;
	}

	public synchronized Exception getThreadException(){
		return thread==null? null: thread.exception;
	}

	public boolean getFirePenTockOnSwing() {
		return firePenTockOnSwing;
	}

	/**
	@param firePenTockOnSwing If {@code true} then {@link PenListener#penTock(long)} is called from the event dispatch thread. {@code false} by default.
	*/
	public void setFirePenTockOnSwing(boolean firePenTockOnSwing){
		this.firePenTockOnSwing = firePenTockOnSwing;
	}

	/**
	Changes the frequency and waits for the event queue to dispatch all pending events. Warning: this method can not be called from the event dispatcher thread.

	@throws Error if called from the event dispatcher thread. 
	@deprecated Use {@link #setFrequencyLater(int)}.
	*/
	@Deprecated
	// TODO: for jpen3... setFrequency to be setFrequencyLater and create setFrequencyAndWait?
	public void setFrequency(int frequency){
		setFrequency(frequency, true);
	}

	private synchronized void setFrequency(int frequency, boolean wait) {
		if(frequency<=0)
			throw new IllegalArgumentException();
		if(frequency==this.frequency)
			return;
		if(wait && SwingUtilities.isEventDispatchThread())
			throw new Error("Cannot call setFrequency(int, <true>) from the event dispatcher thread");
		L.fine("v");
		MyThread oldThread=this.thread;
		if(oldThread!=null){
			oldThread.stop(wait);
		}
		this.frequency=frequency;
		thread=new MyThread(oldThread);
		thread.start();
		L.fine("^");
	}

	/**
	Changes the frequency.  This method returns immediately, the change of frequency will happen after all the pending events are processed.
	*/
	public void setFrequencyLater(int frequency){
		setFrequency(frequency, false);
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
		PenListener[] listenersArray=this.listenersArray; // copy to speed up volatile access
		if(listenersArray==null)
			synchronized(listeners) {
				this.listenersArray=listenersArray=listeners.toArray(new PenListener[listeners.size()]);
			}
		return listenersArray;
	}

	private static class PhantomEventFilter {
		public static int THRESHOLD_PERIOD=200;
		private PenDevice lastDevice; // last device NOT filtered
		private PenEvent lastEvent; // last event scheduled
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

		void setLastEvent(PenEvent event) {
			this.lastEvent=event;
		}

		PenEvent getLastEvent() {
			return lastEvent;
		}
	}

	private final PhantomEventFilter phantomLevelFilter=new PhantomEventFilter();
	private final List<PLevel> scheduledLevels=new ArrayList<PLevel>();

	final boolean scheduleLevelEvent(PenDevice device, long penDeviceTime, Collection<PLevel> levels,  int minX, int maxX, int minY, int maxY, PenManagerPlayer penManagerPlayer) {
		synchronized(scheduledLevels) {
			if(phantomLevelFilter.filter(device))
				return false;
			boolean scheduledMovement=false;
			for(PLevel level:levels) {
				if(level.value==lastScheduledState.getLevelValue(level.typeNumber))
					continue;
				if(levelFilter.filterPenLevel(level))
					continue;
				switch(level.getType()){
				case X:
					scheduledMovement=true;
					if(!evalLevelValueIsInRange(level.value, minX, maxX, penManagerPlayer))
						continue;
					break;
				case Y:
					scheduledMovement=true;
					if(!evalLevelValueIsInRange(level.value, minY, maxY, penManagerPlayer))
						continue;
					break;
				default:
				}
				scheduledLevels.add(level);
				lastScheduledState.levels.setValue(level.typeNumber, level.value);
			}
			if(scheduledLevels.isEmpty())
				return false;
			if(scheduledMovement &&
			        lastScheduledState.getKind().typeNumber!=
			        device.getKindTypeNumber()){
				if(device.getKindTypeNumber()!=PKind.Type.IGNORE.ordinal()){
					PKind newKind=PKind.valueOf(device.getKindTypeNumber());
					lastScheduledState.setKind(newKind);
					schedule(new PKindEvent(this, newKind));
				}
			}
			PLevelEvent levelEvent=new PLevelEvent(this,
			    scheduledLevels.toArray(new PLevel[scheduledLevels.size()]), device.getId(), penDeviceTime);
			phantomLevelFilter.setLastEvent(levelEvent);
			schedule(levelEvent);
			scheduledLevels.clear();
			return true;
		}
	}

	private boolean evalLevelValueIsInRange(float levelValue, int min , int max, PenManagerPlayer penManagerPlayer){
		if(levelValue<min || levelValue>max){
			if(penManagerPlayer!=null &&
			        penManagerPlayer.stopPlayingIfNotDragOut())
				return false;
		}
		return true;
	}

	void scheduleButtonReleasedEvents(){
		for(int i=PButton.Type.VALUES.size(); --i>=0;)
			scheduleButtonEvent(new PButton(i, false));
		for(Integer extButtonTypeNumber: lastScheduledState.extButtonTypeNumberToValue.keySet())
			scheduleButtonEvent(new PButton(extButtonTypeNumber, false));
	}

	private final Object buttonsLock=new Object();

	void scheduleButtonEvent(PButton button) {
		synchronized(buttonsLock) {
			if(lastScheduledState.setButtonValue(button.typeNumber, button.value)){
				PButtonEvent buttonEvent=new PButtonEvent(this, button);
				schedule(buttonEvent);
				levelEmulator.scheduleEmulatedEvent(buttonEvent);
			}
		}
	}

	void scheduleScrollEvent(PenDevice device, PScroll scroll) {
		schedule(new PScrollEvent(this, scroll));
	}

	final void schedule(PenEvent ev) {
		synchronized(lastScheduledEvent) {
			ev.time=System.currentTimeMillis();
			lastScheduledEvent.next=ev;
			lastScheduledEvent=ev;
			MyThread thread=this.thread; // copy to speed up volatile access
			if(thread!=null) thread.processNewEvents();
		}
	}
}
