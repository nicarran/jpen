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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import jpen.Pen;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PLevel;
import jpen.provider.AbstractPenProvider;
import jpen.provider.Utils;
import jpen.provider.VirtualScreenBounds;

public class WintabProvider
	extends AbstractPenProvider {
	private static final Logger L=Logger.getLogger(WintabProvider.class.getName());
	public static final int PERIOD=10;
	final WintabAccess wintabAccess;
	private final Map<Integer, WintabDevice> cursorToDevice=new HashMap<Integer, WintabDevice>();
	private final PLevel.Range[] levelRanges=new PLevel.Range[PLevel.Type.values().length];
	final VirtualScreenBounds screenBounds=VirtualScreenBounds.getInstance();
	private final Thread thread;
	private boolean paused=true;
	//final MouseLocator mouseLocator;

	/*
	class MouseLocator
		extends MouseMotionAdapter{
		private final float[] coords=new float[PLevel.Type.values().length];
		private final boolean[] comparedCoords=new boolean[PLevel.Type.values().length];
		private boolean isMouseMode=false;

		{
			getPenManager().component.addMouseMotionListener(this);
		}

		private void reset(){
			comparedCoords[PLevel.Type.X.ordinal()]=false;
			comparedCoords[PLevel.Type.Y.ordinal()]=false;
			isMouseMode=false;
		}

		@SuppressWarnings("fallthrough")
		float getCorrectedLocation(PLevel.Type levelType, float penLocation){
			switch(levelType){
			case X:
			case Y:
				if(getIsMouseMode(levelType, penLocation))
					return getCoord(levelType);
			default:
				return penLocation;
			}
		}

		private boolean getIsMouseMode(PLevel.Type levelType, float penLocation){
			if(!comparedCoords[levelType.ordinal()]){
				isMouseMode= isMouseMode ||
										 Math.abs(penLocation-getCoord(levelType))>3;
				if(isMouseMode)
					L.fine("mouse mode detected");
				comparedCoords[levelType.ordinal()]=true;
			}
			return isMouseMode;
		}

		synchronized float getCoord(PLevel.Type levelType){
			return coords[levelType.ordinal()];
		}

		@Override
		public synchronized void mouseMoved(MouseEvent ev){
			coords[PLevel.Type.X.ordinal()]= ev.getX();
			coords[PLevel.Type.Y.ordinal()]= ev.getY();
		}
}*/

	public static class Constructor
		implements PenProvider.Constructor {
		//@Override
		public String getName() {
			return "Wintab";
		}
		//@Override
		public boolean constructable() {
			return System.getProperty("os.name").toLowerCase().contains("windows");
		}

		//@Override
		public PenProvider construct(PenManager pm) throws ConstructionException {
			try {
				Utils.loadLibrary();
				WintabAccess wintabAccess=new WintabAccess();
				return new WintabProvider(pm, this, wintabAccess);
			} catch(Throwable t) {
				throw new ConstructionException(t);
			}
		}
	}



	private WintabProvider(PenManager penManager, Constructor constructor, WintabAccess wintabAccess) {
		super(penManager, constructor);
		L.fine("start");
		this.wintabAccess=wintabAccess;
		//this.mouseLocator=new MouseLocator();

		for(PLevel.Type levelType: PLevel.Type.values())
			levelRanges[levelType.ordinal()]=wintabAccess.getLevelRange(levelType);

		thread=new Thread("jpen-WintabProvider") {
			       public synchronized void run() {
				       try {
					       while(true) {
						       processQuedEvents();
						       wait(PERIOD);
						       while(paused){
							       L.fine("going to wait...");
							       wait();
							       L.fine("notified");
						       }
					       }
				       } catch(InterruptedException ex) { throw new Error(ex);}
			       }
		       }
		       ;
		thread.setDaemon(true);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();

		L.fine("end");
	}

	PLevel.Range getLevelRange(PLevel.Type type) {
		return levelRanges[type.ordinal()];
	}

	private void processQuedEvents() {
		L.finer("start");
		while(wintabAccess.nextPacket()) {
			WintabDevice device=getDevice(wintabAccess.getCursor());
			L.finer("device: ");
			L.fine(device.getName());
			device.scheduleEvents();
		}
		L.finer("end");
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

	private void clearEventQueues(){
		while(wintabAccess.nextPacket())
			;
	}

	void setPaused(boolean paused) {
		L.fine("start");
		if(paused==this.paused)
			return;
		this.paused=paused;
		if(!paused){
			L.fine("false paused value");
			//mouseLocator.reset();
			screenBounds.reset();
			clearEventQueues();
			synchronized(thread) {
				L.fine("going to notify all...");
				thread.notifyAll();
				L.fine("done notifying ");
			}
		}
		wintabAccess.setEnabled(!paused);
		L.fine("end");
	}
}
