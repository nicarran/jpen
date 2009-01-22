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

class PenManagerPlayer
			extends MouseAdapter
{
	private static final Logger L=Logger.getLogger(PenManagerPlayer.class.getName());
	//static {L.setLevel(Level.ALL);}

	public final PenManager penManager;
	private boolean isDraggingOut; // state: dragging outside the component
	private Window componentWindow; // may be null
	private boolean pauseOnWindowDeactivation=true; 
	private boolean waitMotionToPlay=true;
	private final WindowListener windowListener=new WindowAdapter(){
		    @Override
		    public void windowDeactivated(WindowEvent ev){
			    if(pauseOnWindowDeactivation)
				    synchronized(PenManagerPlayer.this){
					    stopPlaying();
				    }
		    }
	    };
	private final PenListener draggingOutPenListener=new PenAdapter(){
		    @Override
		    public void penButtonEvent(PButtonEvent ev){
			    synchronized(PenManagerPlayer.this){
				    if(!ev.button.value)
					    if(!penManager.pen.hasPressedButtons()){
						    if(isDraggingOut){// causes button release schedule but there may be level events still to be processed in the pen event queue (here I'm in the queue processing thread).
							    stopPlaying();
						    }
					    }
			    }
		    }
	    };
	private boolean waitingMotionToPlay;
	private final MouseMotionListener waitingMotionMouseListener=new MouseMotionListener(){  // in jdk 1.5 MouseAdapter does not implement this interface.
		    //@Override
		    public synchronized void mouseMoved(MouseEvent ev){
			    if(!penManager.getPaused())
				    throw new AssertionError();
			    setPaused(false);
		    }
		    //@Override
		    public void mouseDragged(MouseEvent ev){
		    }
	    };

	PenManagerPlayer(PenManager penManager){
		this.penManager=penManager;
		penManager.component.addMouseListener(this);
		setPaused(true);
	}

	private synchronized void setPaused(boolean paused) {
		penManager.setPaused(paused);
		setWaitingMotionToPlay(paused);
		updateComponentWindow();
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
	void setPauseOnWindowDeactivation(boolean pauseOnWindowDeactivation){
		this.pauseOnWindowDeactivation=pauseOnWindowDeactivation;
	}

	void setWaitMotionToPlay(boolean waitMotionToPlay){
		this.waitMotionToPlay=waitMotionToPlay;
	}

	@Override
	public synchronized void mouseEntered(MouseEvent ev) {
		if(isDraggingOut)
			stopDraggingOut();
		else
			startPlaying();
	}

	private void startPlaying(){
		if(waitMotionToPlay)
			setWaitingMotionToPlay(true);
		else
			setPaused(false);
	}

	@Override
	public synchronized void mouseExited(MouseEvent ev) {
		//if(isDraggingOut)
			//throw new AssertionError();
		stopPlayingIfNotDragOut();
	}

	private synchronized void setWaitingMotionToPlay(boolean waitingMotionToPlay){
		if(this.waitingMotionToPlay==waitingMotionToPlay)
			return;
		this.waitingMotionToPlay=waitingMotionToPlay;
		if(waitingMotionToPlay)
			penManager.component.addMouseMotionListener(waitingMotionMouseListener);
		else
			penManager.component.removeMouseMotionListener(waitingMotionMouseListener);
	}

	synchronized boolean startDraggingOutIfRequired(){
		if(isDraggingOut)
			return true;
		if(!penManager.pen.hasPressedButtons())
			return false;
		if(componentWindow==null){
			L.warning("Disabled dragging out capability: component window not found.");
			return false;
		}
		if(pauseOnWindowDeactivation && !componentWindow.isActive()){
			L.info("Dragging out on inactive window is not supported.");
			return false;
		}
		isDraggingOut=true;
		penManager.pen.addListener(draggingOutPenListener);
		return true;
	}
	
	boolean stopPlayingIfNotDragOut(){
		if(!startDraggingOutIfRequired()){
			setPaused(true);
			return true;
		}
		return false;
	}

	void stopPlaying(){
		stopDraggingOut();
		setPaused(true);
	}

	private synchronized void stopDraggingOut(){
		if(!isDraggingOut)
			return;
		isDraggingOut=false;
		penManager.pen.removeListener(draggingOutPenListener);
	}
}
