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
package jpen.provider.wintab;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import jpen.internal.BuildInfo;
import jpen.internal.ObjectUtils;
import jpen.internal.Range;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PLevel;
import jpen.provider.AbstractPenProvider;
import jpen.provider.NativeLibraryLoader;
import jpen.provider.VirtualScreenBounds;

public class WintabProvider
	extends AbstractPenProvider {
	private static final Logger L=Logger.getLogger(WintabProvider.class.getName());

	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(new String[]{""},
			new String[]{"64"},
			Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.wintab.nativeVersion")));
	//static{L.setLevel(Level.ALL);}

	static void loadLibrary(){
		LIB_LOADER.load();
	}

	/**
	When this system property is set to true then the provider stops sending events if no AWT events are received after one second.
	*/
	public static final String WAIT_AWT_ACTIVITY_SYSTEM_PROPERTY="jpen.provider.wintab.waitAwtActivity";
	private static final boolean WAIT_AWT_ACTIVITY=Boolean.valueOf(
				System.getProperty(WAIT_AWT_ACTIVITY_SYSTEM_PROPERTY));
	static{
		if(WAIT_AWT_ACTIVITY)
			L.info("WAIT_AWT_ACTIVITY set to true");
	}

	public static final String PERIOD_SYSTEM_PROPERTY="jpen.provider.wintab.period";
	public static final int PERIOD;
	static{
		String periodString=System.getProperty(PERIOD_SYSTEM_PROPERTY, null);
		int periodValue=10;
		if(periodString!=null)
			try{
				periodValue=Integer.valueOf(periodString);
				if(periodValue<=0){
					L.severe("ignored illegal PERIOD value "+periodValue+", period value must be >= 0");
					periodValue=10;
				}else
					L.info("PERIOD set to "+periodValue);
			}catch(NumberFormatException ex){
			}
		PERIOD=periodValue;
	}


	public final WintabAccess wintabAccess;
	private final Map<Integer, WintabDevice> cursorToDevice=new HashMap<Integer, WintabDevice>();
	private final Range[] levelRanges=new Range[PLevel.Type.VALUES.size()];
	final VirtualScreenBounds screenBounds=VirtualScreenBounds.getInstance();
	private final Thread thread;
	private volatile boolean paused=true;
	private boolean systemCursorEnabled=true; // by default the tablet device moves the system pointer (cursor)

	public static class Constructor
		extends AbstractPenProvider.AbstractConstructor{
		//@Override
		public String getName() {
			return "Wintab";
		}
		//@Override
		public boolean constructable(PenManager penManager) {
			return System.getProperty("os.name").toLowerCase().contains("windows");
		}

		@Override
		public PenProvider constructProvider() throws Throwable {
			loadLibrary();
			WintabAccess wintabAccess=new WintabAccess();
			return new WintabProvider(this, wintabAccess);
		}
		@Override
		public int getNativeVersion(){
			return LIB_LOADER.nativeVersion;
		}
		@Override
		public int getNativeBuild(){
			loadLibrary();
			return WintabAccess.getNativeBuild();
		}
		@Override
		public int getExpectedNativeBuild(){
			return Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.wintab.nativeBuild"));
		}
	}

	class MyThread
		extends Thread implements AWTEventListener{

		private long scheduleTime;
		private long awtEventTime;
		private boolean waitingAwtEvent;
		private int inputEventModifiers;
		private boolean awtSleep;
		private final Object awtLock=new Object();

		{
			setName("jpen-WintabProvider");
			setDaemon(true);
			setPriority(Thread.MAX_PRIORITY);
			if(WAIT_AWT_ACTIVITY)
				Toolkit.getDefaultToolkit().addAWTEventListener(this, ~0);
		}

		public void run() {
			try{
				KeyboardFocusManager keyboardFocusManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
				long processingTime;
				long correctPeriod;
				boolean waited=true;
				while(true) {
					processingTime=waited? System.currentTimeMillis(): scheduleTime;
					schedule();
					processingTime=scheduleTime-processingTime;
					correctPeriod=PERIOD-processingTime;
					waited=false;
					synchronized(this){
						if(correctPeriod>0){
							wait(correctPeriod);
							waited=true;
						}
						if(WAIT_AWT_ACTIVITY){
							waitingAwtEvent=scheduleTime-awtEventTime>1000 &&
															(inputEventModifiers==0 || keyboardFocusManager.getActiveWindow()==null);
							if(waitingAwtEvent){
								wait(500);
								waited=true;
							}
						}
						while(paused){
							L.fine("going to wait...");
							wait();
							L.fine("notified");
							waited=true;
						}
					}
				}
			}catch(InterruptedException ex){
				throw new AssertionError(ex);
			}
		}

		private void schedule(){
			processQueuedEvents();
			scheduleTime=System.currentTimeMillis();
		}
		//@Override
		public synchronized void eventDispatched(AWTEvent ev){
			InputEvent inputEvent=ev instanceof InputEvent? (InputEvent)ev: null;
			synchronized(this){
				awtEventTime=System.currentTimeMillis();
				if(inputEvent!=null)
					inputEventModifiers=inputEvent.getModifiersEx();
				if(!paused && waitingAwtEvent)
					notify();
			}
		}
	}

	private WintabProvider(Constructor constructor, WintabAccess wintabAccess) {
		super(constructor);
		L.fine("start");
		this.wintabAccess=wintabAccess;

		for(int i=PLevel.Type.VALUES.size(); --i>=0;){
			PLevel.Type levelType=PLevel.Type.VALUES.get(i);
			levelRanges[levelType.ordinal()]=wintabAccess.getLevelRange(levelType);
		}

		thread=new MyThread();
		thread.start();
		//System.out.println("wintabAccess=" + ( wintabAccess ));
		L.fine("end");
	}

	Range getLevelRange(PLevel.Type type) {
		return levelRanges[type.ordinal()];
	}

	private void processQueuedEvents() {
		//L.finer("start");
		//boolean gotPacket=false;
		while(wintabAccess.nextPacket() && !paused) {
			//gotPacket=true;
			WintabDevice device=getDevice(wintabAccess.getCursor());
			if(L.isLoggable(Level.FINE)){
				L.finer("device: ");
				L.finer(device.getName());
			}
			device.scheduleEvents();
		}
		//System.out.println("gotPacket=" + ( gotPacket ));
		//L.finer("end");
	}

	private WintabDevice getDevice(int cursor) {
		WintabDevice wintabDevice=cursorToDevice.get(cursor);
		if(wintabDevice==null) {
			cursorToDevice.put(cursor, wintabDevice=new WintabDevice(this, cursor));
			devices.clear();
			devices.addAll(cursorToDevice.values());
			getPenManager().firePenDeviceAdded(getConstructor(), wintabDevice);
		}
		return wintabDevice;
	}

	//@Override
	public void penManagerPaused(boolean paused) {
		setPaused(paused);
	}

	synchronized void setPaused(boolean paused) {
		L.fine("start");
		if(paused==this.paused)
			return;
		this.paused=paused;
		if(!paused){
			L.fine("false paused value");
			screenBounds.reset();
			synchronized(thread) {
				L.fine("going to notify all...");
				thread.notifyAll();
				L.fine("done notifying ");
			}
			wintabAccess.enable(true);
		}
		L.fine("end");
	}

	@Override
	public boolean getUseRelativeLocationFilter(){
		return systemCursorEnabled;
	}

	/**
	@param systemCursorEnabled If <code>false<code> then tablet movement on Wintab devices doesn't cause movement on the system mouse pointer. If <code>true<code> then tablet movement on Wintab devices cause movement on the system mouse pointer, this is the default value. 
	*/
	public synchronized void setSystemCursorEnabled(boolean systemCursorEnabled){
		if(this.systemCursorEnabled==systemCursorEnabled)
			return;
		this.systemCursorEnabled=systemCursorEnabled;
		wintabAccess.setSystemCursorEnabled(systemCursorEnabled);
	}

	public synchronized boolean getSystemCursorEnabled(){
		return systemCursorEnabled;
	}
}