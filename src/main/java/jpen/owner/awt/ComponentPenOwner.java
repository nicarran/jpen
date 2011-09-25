/* [{
Copyright 2007-2011 Nicolas Carranza <nicarran at gmail.com>

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
import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.Window;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.SwingUtilities;
import jpen.internal.ActiveWindowProperty;
import jpen.owner.AbstractPenOwner;
import jpen.owner.PenClip;
import jpen.PenProvider;
import jpen.provider.osx.CocoaProvider;
import jpen.provider.system.SystemProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

public abstract class ComponentPenOwner
	extends AbstractPenOwner{

	private final ComponentPenClip componentPenClip=new ComponentPenClip(this);

	public abstract Component getActiveComponent();

	//@Override
	public final Collection<PenProvider.Constructor> getPenProviderConstructors(){
		return Arrays.asList(
						 new PenProvider.Constructor[]{
							 new SystemProvider.Constructor(),
							 new XinputProvider.Constructor(),
							 new WintabProvider.Constructor(),
							 new CocoaProvider.Constructor(),
						 }
					 );
	}

	//@Override
	public final PenClip getPenClip(){
		return componentPenClip;
	}

	protected final Unpauser unpauser=new Unpauser();

	protected final class Unpauser
		implements MouseMotionListener{

		private volatile boolean enabled;
		private WeakReference<Component> myActiveComponentRef;

		public synchronized void enable(){
			if(enabled)
				return;
			Component myActiveComponent=getActiveComponent();
			myActiveComponentRef=new WeakReference<Component>(myActiveComponent);
			myActiveComponent.addMouseMotionListener(unpauser); // unpauses only when mouse motion is detected.
			enabled=true;
		}

		synchronized void disable(){
			if(!enabled)
				return;
			Component myActiveComponent=myActiveComponentRef.get();
			if(myActiveComponent!=null){
				myActiveComponent.removeMouseMotionListener(unpauser);
				myActiveComponent=null;
			}
			enabled=false;
		}

		//@Override
		public void mouseMoved(MouseEvent ev){
			//L.fine("v");
			unpause();
		}

		//@Override
		public void mouseDragged(MouseEvent ev){
		}

		void unpause(){
			synchronized(penManagerHandle.getPenSchedulerLock()){
				if(enabled){
					activeWindowPL.setEnabled(true);
					penManagerHandle.setPenManagerPaused(false);
					disable();
				}
			}
		}
	}

	private final ActiveWindowPL activeWindowPL=new ActiveWindowPL();

	private class ActiveWindowPL
		implements ActiveWindowProperty.Listener{

		private boolean enabled;
		private ActiveWindowProperty activeWindowP;

		void setEnabled(boolean enabled){
			if(activeWindowP==null)
				activeWindowP=new ActiveWindowProperty(this);
			this.enabled=enabled;
		}

		//@Override
		public void activeWindowChanged(Window activeWindow){
			if(!enabled)
				return;
			synchronized(getPenSchedulerLock(activeWindow)){
				if(activeWindow==null){
					// if there is no active window on this application, on MS Windows the mouse stops sending events.
					pauseAMoment();
					return;
				}
				Window componentWindow=SwingUtilities.getWindowAncestor(getActiveComponent());
				if(componentWindow==null)
					return;
				if(activeWindow!=componentWindow &&
					 activeWindow instanceof Dialog){
					//	A modal dialog stops sending events from other windows when shown... then here we honor this behavior
					Dialog activeDialog=(Dialog)activeWindow;
					if(activeDialog.isModal())
						pauseAMoment();
				}
			}
		}

		private void pauseAMoment(){
			pause();
			unpauser.enable();
		}
	}

	protected final void pause(){
		unpauser.disable();
		activeWindowPL.setEnabled(false);
		penManagerHandle.setPenManagerPaused(true);
	}

	@Override
	protected void draggingOutDisengaged(){
		pause();
	}

	/**
	Checks if the given {@link Component} holds the {@link Component#getTreeLock()} before actually getting and returning the {@link jpen.owner.PenOwner.PenManagerHandle#getPenSchedulerLock()}. Prefer using this method instead of {@link jpen.owner.PenOwner.PenManagerHandle#getPenSchedulerLock()} to be shure you  aren't causing deadlocks because the {@link ComponentPenClip} methods hold the {@link Component#getTreeLock()}.
	*/
	protected Object getPenSchedulerLock(Component component){
		if(component!=null && Thread.currentThread().holdsLock(component.getTreeLock()))
			throw new AssertionError("tryed to hold penSchedulerLock while holding Component's treeLock");
		return penManagerHandle.getPenSchedulerLock();
	}
}