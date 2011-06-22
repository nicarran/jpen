package jpen.owner.awt;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.Window;
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
		private Component myActiveComponent;

		public synchronized void enable(){
			if(enabled)
				return;
			myActiveComponent=getActiveComponent();
			myActiveComponent.addMouseMotionListener(unpauser); // unpauses only when mouse motion is detected.
			enabled=true;
		}

		synchronized void disable(){
			if(!enabled)
				return;
			myActiveComponent.removeMouseMotionListener(unpauser);
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
			synchronized(penManagerHandle.getPenSchedulerLock()){
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
}