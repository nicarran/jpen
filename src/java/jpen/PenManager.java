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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import jpen.event.PenAdapter;
import jpen.event.PenListener;
import jpen.event.PenManagerListener;
import jpen.provider.system.SystemProvider;
import jpen.provider.Utils;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

public final class PenManager {
	private static final Logger L=Logger.getLogger(PenManager.class.getName());
	public final Pen  pen=new Pen();
	public final Component component;
	private final Map<PenProvider.Constructor, PenProvider> constructorToProvider=new HashMap<PenProvider.Constructor, PenProvider>();
	private final Set<PenProvider.Constructor> constructors=new HashSet<PenProvider.Constructor>();
	private final Set<PenProvider.Constructor> constructorsA=Collections.unmodifiableSet(constructors);
	private final Map<PenProvider.Constructor, PenProvider.ConstructionException> constructorToException=new HashMap<PenProvider.Constructor, PenProvider.ConstructionException>();
	private volatile boolean paused;
	private final List<PenManagerListener> listeners=new ArrayList<PenManagerListener>();

	// TODO: decide if DragOutMode and its machinery should be public... wait for comments on jpen.sf.net.
	enum DragOutMode{
	  /**
	  Dragging outside the component stops firing events. This is the default {@link PenManager#getDragOutMode()}.
	  */
	  DISABLED,
	  /**
	  Dragging outside the {@link #component} is enabled and causes event firing. <b>Warning</b>: If the dragging travels above other windows, digitized events may stop firing.
	  */
	  ENABLED;
	  //  implement NO_LEVELS: dragging enabled but no levels are fired?
	}
	private DragOutMode dragOutMode=DragOutMode.ENABLED; 

	private class Pauser
		extends MouseAdapter{
		private boolean isDraggingOut; // state: dragging outside the component
		private final PenListener penListener=new PenAdapter(){
			    @Override
			    public void penButtonEvent(PButtonEvent ev){
				    synchronized(Pauser.this){
					    if(!ev.button.value)
						    if(!pen.hasPressedButtons()){
							    if(isDraggingOut){
								    stopDraggingOut();
								    setPaused(true); // causes button release schedule but there may be level events still to be processed in the pen event queue (here I'm in the queue processing thread).
							    }
							    else
								    throw new AssertionError();
						    }
				    }
			    }
		    };
		private Window componentWindow;
		private boolean pauseOnWindowDeactivation=true;
		private final WindowListener windowListener=new WindowAdapter(){
			    @Override
			    public void windowDeactivated(WindowEvent ev){
				    if(pauseOnWindowDeactivation)
					    synchronized(Pauser.this){
						    stopDraggingOut();
						    setPaused(true);
					    }
			    }
		    };

		{
			updateComponentWindow();
		}
		
		private boolean waitingMotionToPlay;

		@Override
		public synchronized void mouseEntered(MouseEvent ev) {
			if(isDraggingOut)
				stopDraggingOut();
			else
				setWaitingMotionToPlay(true);
		}
		
		private synchronized void setWaitingMotionToPlay(boolean waitingMotionToPlay){
			if(this.waitingMotionToPlay==waitingMotionToPlay)
				return;
			this.waitingMotionToPlay=waitingMotionToPlay;
			if(waitingMotionToPlay)
				component.addMouseMotionListener(this);
			else
				component.removeMouseMotionListener(this);
		}
		
		@Override
		public synchronized void mouseMoved(MouseEvent ev){
			if(!paused)
				throw new AssertionError();
			setPaused(false);
		}
		
		private synchronized void stopDraggingOut(){
			if(!isDraggingOut)
				return;
			isDraggingOut=false;
			pen.removeListener(penListener);
		}

		@Override
		public synchronized void mouseExited(MouseEvent ev) {
			if(isDraggingOut) // TEST
				throw new AssertionError();
			startDraggingOut();
			if(!isDraggingOut)
				setPaused(true);
		}

		private synchronized void startDraggingOut(){
			if(dragOutMode.equals(DragOutMode.DISABLED) || !pen.hasPressedButtons())
				return;
			if(componentWindow==null){
				L.warning("Disabled dragging out capability: component window found.");
				return;
			}
			if(!componentWindow.isActive()){
				L.info("Dragging out on inactive window is not supported.");
				return;
			}
			isDraggingOut=true;
			pen.addListener(penListener);
		}

		private void updateComponentWindow(){
			if(component==null)
				return;
			if(componentWindow!=null)
				componentWindow.removeWindowListener(windowListener); // may be already removed
			componentWindow=Utils.getLocationOnScreen(component, null);
			if(componentWindow!=null)
				componentWindow.addWindowListener(windowListener);
		}

		private synchronized void setPaused(boolean paused) {
			if(PenManager.this.paused==paused)
				return;
			//if(!paused && pauseOnWindowDeactivation && componentWindow!=null && !componentWindow.isActive())
				//return;
			PenManager.this.paused=paused;
			setWaitingMotionToPlay(paused);
			if(paused)
				pen.scheduleButtonReleasedEvents();
			updateComponentWindow();
			for(PenProvider provider: constructorToProvider.values())
				provider.penManagerPaused(paused);
		}
	}

	private final Pauser pauser=new Pauser();


	public PenManager(Component component) {
		this.component=component;
		component.addMouseListener(pauser);
		addProvider(new SystemProvider.Constructor());
		addProvider(new XinputProvider.Constructor());
		addProvider(new WintabProvider.Constructor());
		pauser.setPaused(true);
	}

	// TODO: set this public?
	void setDragOutMode(DragOutMode dragOutMode){
		this.dragOutMode=dragOutMode;
	}

	// TODO: set this public?
	DragOutMode getDragOutMode(){
		return dragOutMode;
	}

	// TODO: set this public?
	void setPauseOnWindowDeactivation(boolean pauseOnWindowDeactivation){
		pauser.pauseOnWindowDeactivation=pauseOnWindowDeactivation;
	}

	/**
	Constructs and adds provider if {@link PenProvider.Constructor#constructable()} is true.
	*/
	public void addProvider(PenProvider.Constructor constructor) {
		if(constructor.constructable()) {
			try {
				constructors.add(constructor);
				PenProvider provider=constructor.construct(this);
				constructorToProvider.put(constructor, provider);
			} catch(PenProvider.ConstructionException ex) {
				constructorToException.put(constructor, ex);
			}
		}
	}

	public void addListener(PenManagerListener l) {
		synchronized(listeners) {
			listeners.add(l);
		}
	}

	public void removeListener(PenManagerListener l) {
		synchronized(listeners) {
			listeners.remove(l);
		}
	}

	public void firePenDeviceAdded(PenProvider.Constructor constructor, PenDevice device) {
		synchronized(listeners) {
			for(PenManagerListener l: listeners)
				l.penDeviceAdded(constructor, device);
		}
	}

	public void firePenDeviceRemoved(PenProvider.Constructor constructor, PenDevice device) {
		synchronized(listeners) {
			for(PenManagerListener l: listeners)
				l.penDeviceRemoved(constructor, device);
		}
	}

	public Set<PenProvider.Constructor> getConstructors() {
		return constructorsA;
	}

	public PenProvider getProvider(PenProvider.Constructor constructor) {
		return constructorToProvider.get(constructor);
	}

	public PenProvider.ConstructionException getConstructionException(PenProvider.Constructor constructor) {
		return constructorToException.get(constructor);
	}

	public boolean getPaused() {
		return paused;
	}

	@SuppressWarnings("deprecation")
	public void scheduleButtonEvent(PButton button) {
		if(paused)
			return;
		pen.scheduleButtonEvent(button);
	}

	@SuppressWarnings("deprecation")
	public void scheduleScrollEvent(PScroll scroll) {
		if(paused)
			return;
		pen.scheduleScrollEvent(scroll);
	}

	@SuppressWarnings("deprecation")
	public boolean scheduleLevelEvent(PenDevice device, Collection<PLevel> levels) {
		if(paused)
			return false;
		switch(dragOutMode){
		case DISABLED:
			return pen.scheduleLevelEvent(device, levels, 0, component.getWidth(), 0, component.getHeight());
		case ENABLED:
			return pen.scheduleLevelEvent(device, levels,  -Integer.MAX_VALUE, Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE);
		default:
			throw new AssertionError();
		}
	}
}
