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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpen.event.PenAdapter;
import jpen.event.PenListener;
import jpen.provider.Utils;

class DragOutHandler
			extends MouseAdapter
			implements MouseMotionListener // in jdk 1.5 MouseAdapter does not implement this interface.
{
	private static final Logger L=Logger.getLogger(DragOutHandler.class.getName());
	{
		//L.setLevel(Level.ALL);
	}

	public final PenManager penManager;
	enum Mode{
	  /**
	  Dragging outside the component stops firing events.
	  */
	  DISABLED,
	  /**
	  Dragging outside the {@link #component} is enabled and causes event firing. <b>Warning</b>: If the dragging travels above other windows, digitized events may stop firing.
	  */
	  ENABLED;
	  //  implement NO_LEVELS: dragging enabled but no levels are fired?
	}
	private Mode mode=Mode.ENABLED;

	private boolean isDraggingOut; // state: dragging outside the component
	private Window componentWindow;
	private boolean pauseOnWindowDeactivation=true;
	private final WindowListener windowListener=new WindowAdapter(){
		    @Override
		    public void windowDeactivated(WindowEvent ev){
			    if(pauseOnWindowDeactivation)
				    synchronized(DragOutHandler.this){
					    stopDraggingOut();
					    setPaused(true);
				    }
		    }
	    };
	private final PenListener penListener=new PenAdapter(){
		    @Override
		    public void penButtonEvent(PButtonEvent ev){
			    synchronized(DragOutHandler.this){
				    if(!ev.button.value)
					    if(!penManager.pen.hasPressedButtons()){
						    if(isDraggingOut){
							    stopDraggingOut();
							    setPaused(true); // causes button release schedule but there may be level events still to be processed in the pen event queue (here I'm in the queue processing thread).
						    }
					    }
			    }
		    }
	    };

	private boolean waitingMotionToPlay;

	DragOutHandler(PenManager penManager){
		this.penManager=penManager;
		penManager.component.addMouseListener(this);
		setPaused(true);
	}

	private void updateComponentWindow(){
		if(penManager.component==null)
			return;
		if(componentWindow!=null)
			componentWindow.removeWindowListener(windowListener); // may be already removed
		componentWindow=Utils.getLocationOnScreen(penManager.component, null);
		if(componentWindow!=null)
			componentWindow.addWindowListener(windowListener);
	}


	// TODO: set this public?
	void setMode(Mode mode){
		this.mode=mode;
	}

	// TODO: set this public?
	Mode getMode(){
		return mode;
	}

	// TODO: set this public?
	void setPauseOnWindowDeactivation(boolean pauseOnWindowDeactivation){
		this.pauseOnWindowDeactivation=pauseOnWindowDeactivation;
	}

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
			penManager.component.addMouseMotionListener(this);
		else
			penManager.component.removeMouseMotionListener(this);
	}

	//@Override
	public synchronized void mouseMoved(MouseEvent ev){
		if(!penManager.getPaused())
			throw new AssertionError();
		setPaused(false);
	}

	//@Override
	public void mouseDragged(MouseEvent ev){
	}

	private synchronized void stopDraggingOut(){
		if(!isDraggingOut)
			return;
		isDraggingOut=false;
		penManager.pen.removeListener(penListener);
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
		if(mode.equals(Mode.DISABLED) || !penManager.pen.hasPressedButtons())
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
		penManager.pen.addListener(penListener);
	}

	private synchronized void setPaused(boolean paused) {
		penManager.setPaused(paused);
		setWaitingMotionToPlay(paused);
		updateComponentWindow();
	}
}
