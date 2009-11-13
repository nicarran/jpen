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

	public final PenManager penManager;
	private int frequency;
	private volatile MyThread thread;

	/** Tail of event queue. */
	private PenEvent lastDispatchedEvent=new PenEvent(this) {
		    public static final long serialVersionUID=1l;
		    @Override
		    void dispatch() { }
		    @Override
		    void copyTo(PenState penState){}
	    };
	final PenScheduler scheduler=new PenScheduler(this);
	public final PenState lastScheduledState=scheduler.lastScheduledState;
	private final List<PenListener> listeners=new ArrayList<PenListener>();
	private PenListener[] listenersArray;
	private boolean firePenTockOnSwing;
	public final PLevelEmulator levelEmulator;
	private PLevelFilter levelFilter=PLevelFilter.AllowAll.INSTANCE;

	private final class MyThread
		extends Thread {
		final int periodMillis;
		long beforeTime;
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
				if(!stopRunning)
					wait(timeout);
			}
			synchronized void doNotify(){
				notify();
			}
		}

		MyThread(Thread oldThread){
			periodMillis=1000/Pen.this.frequency;
			this.oldThread=oldThread;
			setName("jpen-Pen-"+periodMillis+"ms");
		}
		private final Runnable penTockFirer=new Runnable(){
			    //@Override
			    public void run(){
				    for(PenListener l:getListenersArray())
					    l.penTock( availablePeriod - evalCurrentProcTime());
			    }
		    };

		public void run() {
			try {
				L.finest("v");
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
					while((event=lastDispatchedEvent.next)!=null && event.getTime()<=beforeTime) {
						event.copyTo(Pen.this);
						event.dispatch();
						lastDispatchedEvent.next=null;
						lastDispatchedEvent=event;
					}
					availablePeriod=periodMillis+waitTime;
					firePenTock();
					waitTime=periodMillis-evalCurrentProcTime();
					if(waitTime>0) {
						//System.out.println("going to wait: "+waitTime);
						Utils.sleepUninterrupted(waitTime);
						waitTime=0;
					}
				}
			} catch(Exception ex) {
				L.warning("jpen-Pen thread threw an exception: "+Utils.evalStackTrace(ex));
				exception=ex;
			}
			L.finest("^");
		}
		
		private long evalCurrentProcTime(){
			return System.currentTimeMillis()-beforeTime;
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

	Pen(){
		this(null, null);
	}

	Pen(PenManager penManager, PLevelEmulator levelEmulator) {
		this.penManager=penManager;
		this.levelEmulator=levelEmulator;
		setFrequencyLater(DEFAULT_FREQUENCY);
	}
	
	void processNewEvents(){
		thread.processNewEvents();
	}
	
	PenEvent getLastDispatchedEvent(){
		return lastDispatchedEvent;
	}

	public PLevelFilter getLevelFilter(){
		return levelFilter;
	}

	public void setLevelFilter(PLevelFilter levelFilter){
		this.levelFilter=levelFilter;
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
		L.finest("v");
		MyThread oldThread=this.thread;
		if(oldThread!=null){
			oldThread.stop(wait);
		}
		this.frequency=frequency;
		thread=new MyThread(oldThread);
		thread.start();
		L.finest("^");
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
	
	public int getPeriodMillis(){
		return thread.periodMillis;
	}
	
	public synchronized Exception getThreadException(){
		return thread.exception;
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
		synchronized(listeners){
			if(listenersArray==null)
				listenersArray=listeners.toArray(new PenListener[listeners.size()]);
			return listenersArray;
		}
	}
}
