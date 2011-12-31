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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Queue;
import javax.swing.SwingUtilities;
import jpen.event.PenListener;
import jpen.internal.ThreadUtils;
import jpen.internal.ThrowableUtils;

public class Pen extends PenState {
	private static final Logger L=Logger.getLogger(Pen.class.getName());
	//static{L.setLevel(Level.ALL);}

	public static final int DEFAULT_FREQUENCY=60; // TODO: 50 is a better default or less??

	public final PenManager penManager;
	private int frequency;
	private volatile MyThread thread;

	/** Tail of event queue. */
	private PenEvent lastDispatchedEvent=new PenEvent.Dummy();
	final PenScheduler scheduler;
	public final PenState lastScheduledState;
	private final List<PenListener> listeners=new ArrayList<PenListener>();
	private PenListener[] listenersArray;
	private boolean firePenTockOnSwing;
	public final PLevelEmulator levelEmulator;

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

			boolean waitForNewEvent() throws InterruptedException{
				if(lastDispatchedEvent.next!=null)
					return false;
				synchronized(this){
					if(lastDispatchedEvent.next!=null)
						return false;
					if(!stopRunning)
						wait(0);
					return true;
				}
			}
			synchronized void notifyNewEvent(){
				notify();
			}
		}

		MyThread(Thread oldThread){
			periodMillis=1000/Pen.this.frequency;
			this.oldThread=oldThread;
			setName("jpen-Pen-["+periodMillis+"ms]");
			AccessController.doPrivileged(new PrivilegedAction<Object>(){
						//@Override
						public Object run(){
							setDaemon(true);
							return null;
						}
					});
		}
		private final Runnable penTockFirer=new Runnable(){
					//@Override
					public void run(){
						//System.out.println("firing tocks "+System.currentTimeMillis());
						for(PenListener l:getListenersArray()){
							//System.out.println("firing pentock, procTime="+evalCurrentProcTime()+", l="+l);
							l.penTock(availablePeriodLeft());
						}
					}
				};

		public void run() {
			try {
				L.finest("v");
				if(oldThread!=null)
					oldThread.join();
				oldThread=null;
				while(!stopRunning) {
					waitedNewEvents=waiter.waitForNewEvent();
					beforeTime=System.currentTimeMillis();
					if(waitedNewEvents)
						waitTime=0;
					boolean eventDispatched=false;
					while((event=lastDispatchedEvent.next)!=null && event.getTime()<=beforeTime) {
						event.copyTo(Pen.this);
						event.dispatch();
						lastDispatchedEvent.next=null;
						lastDispatchedEvent=event;
						eventDispatched=true;
					}
					//System.out.println("after event dispatching, procTime="+evalCurrentProcTime());
					availablePeriod=periodMillis+waitTime; // waitTime here is always <=0, if it is <0 then the whole processing of the previous round took longer than the time available.
					//System.out.println("going to fire tock "+System.currentTimeMillis());
					if(eventDispatched)
						firePenTock();
					//System.out.println("after penTock, procTime="+evalCurrentProcTime());
					waitTime=availablePeriodLeft();// the same:  (periodMillis-evalCurrentProcTime())+waitTime;
					if(waitTime>0) {
						//System.out.println("going to wait: "+waitTime);
						ThreadUtils.sleepUninterrupted(waitTime);
						//waiter.doWait(waitTime); // in some cases, this (instead of sleepUninterrupted ^ ) gives better overall performance (wintab-pulling, jpen demo). We can put this as an alternate behavior if  needed.
						waitTime=0;
					}
				}
			} catch(Exception ex) {
				L.severe("jpen-Pen thread threw an exception: "+ThrowableUtils.evalStackTraceString(ex));
				exception=ex;
			}
			L.finest("^");
		}

		private long evalCurrentProcTime(){
			return System.currentTimeMillis()-beforeTime;
		}

		private long availablePeriodLeft(){
			return availablePeriod-evalCurrentProcTime();
		}

		private void firePenTock() throws InterruptedException, InvocationTargetException{
			if(getListenersArray().length==0)
				return;
			if(firePenTockOnSwing)
				SwingUtilities.invokeAndWait(penTockFirer);
			else
				penTockFirer.run();
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

	Pen(PenManager penManager) {
		this.penManager=penManager;
		this.scheduler=new PenScheduler(this);
		this.lastScheduledState=scheduler.lastScheduledState;
		this.levelEmulator=new PLevelEmulator(this);
		setFrequencyLater(DEFAULT_FREQUENCY);
	}

	void processNewEvents(){
		thread.waiter.notifyNewEvent();
	}

	PenEvent getLastDispatchedEvent(){
		return lastDispatchedEvent;
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
	Changes the event firing frequency. The pen collects device (tablet) data points and stores them in a buffer. The data  points are taken from this buffer and fired as {@link PenEvent}s at this frequency.<p> 

	This method returns immediately, the change of frequency will happen after all the pending events are processed.

	@see #addListener(PenListener) 
	@see #removeListener(PenListener)
	*/
	public void setFrequencyLater(int frequency){
		setFrequency(frequency, false);
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

	public int getFrequency() {
		return frequency;
	}

	public int getPeriodMillis(){
		return thread.periodMillis;
	}

	public synchronized Exception getThreadException(){
		return thread.exception;
	}

	/**
	Adds a {@link PenListener} for {@link PenEvent}s fired by this pen.
	*/
	public void addListener(PenListener l) {
		synchronized(listeners) {
			listeners.add(l);
			listenersArray=null;
		}
	}

	/**
	Removes a {@link PenListener} previously added using {@link #addListener(PenListener)}.
	*/
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