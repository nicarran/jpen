/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.owner.awt;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import jpen.owner.AbstractPenOwner;
import jpen.owner.PenClip;
import jpen.PenProvider;
import jpen.provider.osx.CocoaProvider;
import jpen.provider.system.SystemProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

public final class AwtPenOwner
	extends AbstractPenOwner{

	public final Component component;
	final PenClipOnComponent penClipOnComponent;
	private final MouseListener mouseListener=new MouseAdapter(){
		    @Override
		    public void mouseExited(MouseEvent ev) {
			    synchronized(penManagerHandle.getPenSchedulerLock()){
				    if(!startDraggingOut()){
					    pause();
					    unpauser.disable();
					  }
			    }
		    }

		    @Override
		    public void mouseEntered(MouseEvent ev) {
			    synchronized(penManagerHandle.getPenSchedulerLock()){
				    if(!stopDraggingOut())
					    unpauser.enable(); // unpauses when mouse motion is detected.
			    }
		    }
	    };
	Unpauser unpauser=new Unpauser();
	final class Unpauser
		implements MouseMotionListener{

		private volatile boolean enabled;

		void enable(){
			if(enabled)
				return;
			component.addMouseMotionListener(unpauser); // unpauses only when mouse motion is detected.
			enabled=true;
		}

		void disable(){
			if(!enabled)
				return;
			component.removeMouseMotionListener(unpauser);
			enabled=false;
		}

		//@Override
		public void mouseMoved(MouseEvent ev){
			unpause();
		}

		void unpause(){
			synchronized(penManagerHandle.getPenSchedulerLock()){
				if(!penManagerHandle.getPenManager().getPaused())
					return;
				if(enabled){
					keyboardFocusManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
					keyboardFocusManager.addPropertyChangeListener("focusedWindow", focusedWindowListener);
					focusedWindow=keyboardFocusManager.getFocusedWindow();
					penManagerHandle.setPenManagerPaused(false);
					disable();
				}
			}
		}

		//@Override
		public void mouseDragged(MouseEvent ev){
		}
	}

	/*
	The component does not get a mouseExited event when a new JOptionPane dialog appears... jdk bug?
	As workaround a focusedWindowListener is installed and it pauses the penManager. 
	*/
	private KeyboardFocusManager keyboardFocusManager;
	private Window focusedWindow;
	private final PropertyChangeListener focusedWindowListener=new PropertyChangeListener(){
		    //@Override
		    public void propertyChange(PropertyChangeEvent ev){
			    synchronized(penManagerHandle.getPenSchedulerLock()){
				    if(focusedWindow==null){
					    focusedWindow=(Window)ev.getNewValue();
					    return;
				    }
				    pause(); // release buttons
				    unpauser.enable();
			    }
		    }
	    };

	public AwtPenOwner(Component component){
		this.component=component;
		this.penClipOnComponent=new PenClipOnComponent(component);
	}

	//@Override
	public Collection<PenProvider.Constructor> getPenProviderConstructors(){
		return Arrays.asList(
		         new PenProvider.Constructor[]{
		           new SystemProvider.Constructor(),
		           new XinputProvider.Constructor(),
		           new WintabProvider.Constructor(),
		           new CocoaProvider.Constructor(),
		         }
		       );
	}

	private void pause(){
		if(penManagerHandle.getPenManager().getPaused())
			return;
		keyboardFocusManager.removePropertyChangeListener("focusedWindow", focusedWindowListener);
		focusedWindow=null;
		penManagerHandle.setPenManagerPaused(true);
	}

	//@Override
	public PenClip getPenClip(){
		return penClipOnComponent;
	}

	@Override
	protected void init(){
		component.addMouseListener(mouseListener);
	}
}