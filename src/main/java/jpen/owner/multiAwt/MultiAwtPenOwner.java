/* [{
Copyright 2011 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.owner.multiAwt;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import jpen.event.PenListener;
import jpen.internal.WeakChain;
import jpen.owner.awt.ComponentPenOwner;
import jpen.PButtonEvent;
import jpen.PenEvent;
import jpen.PKindEvent;
import jpen.PLevelEvent;
import jpen.PScrollEvent;


final class MultiAwtPenOwner
			extends ComponentPenOwner
	implements ComponentPool.Listener{
	private static final Logger L=Logger.getLogger(MultiAwtPenOwner.class.getName());
	//static { L.setLevel(Level.ALL); }

	final ComponentPool componentPool=new ComponentPool(this);
	private ActiveComponentInfo activeComponentInfo=new ActiveComponentInfo(null);
	class ActiveComponentInfo{
		private final WeakReference<Component>	componentRef;
		private final WeakChain<PenListener> penListenersChain=new WeakChain<PenListener>();

		ActiveComponentInfo(Component component){
			this.componentRef=new WeakReference<Component>(component);
			for(PenListener penListener: componentPool.getPenListeners(component))
				penListenersChain.add(penListener);
		}

		Component getComponent(){
			return componentRef.get();
		}

		void getPenListeners(Collection<PenListener> penListeners){
			penListenersChain.snapshot(penListeners);
		}
	}

	private final PenListener penMulticaster=new PenListener(){

				private final Listeners listeners=new Listeners();

				class Listeners
					extends ArrayList<PenListener>{

					private ActiveComponentInfo activeComponentInfo;

					void setActiveComponentInfo(ActiveComponentInfo activeComponentInfo){
						if(this.activeComponentInfo==activeComponentInfo)
							return;
						this.activeComponentInfo=activeComponentInfo;
						clear();
						if(activeComponentInfo!=null)
							activeComponentInfo.getPenListeners(listeners);
					}
				}


				//@Override
				public void penKindEvent(PKindEvent ev){
					updateListeners(ev);
					for(PenListener listener: listeners)
						listener.penKindEvent(ev);
				}

				private void updateListeners(PenEvent ev){
					listeners.setActiveComponentInfo(
						(ActiveComponentInfo)penManagerHandle.retrievePenEventTag(ev)
					);
				}

				//@Override
				public void penLevelEvent(PLevelEvent ev){
					updateListeners(ev);
					for(PenListener listener: listeners)
						listener.penLevelEvent(ev);
				}
				//@Override
				public void penButtonEvent(PButtonEvent ev){
					updateListeners(ev);
					for(PenListener listener: listeners)
						listener.penButtonEvent(ev);
				}
				//@Override
				public void penScrollEvent(PScrollEvent ev){
					updateListeners(ev);
					for(PenListener listener: listeners)
						listener.penScrollEvent(ev);
				}

				private static final long NANOS_TO_MILLIS_DIV=1000000l;
				//@Override
				public void penTock(long availableMillis){
					long spentTimeMillis=0;
					for(PenListener listener: listeners){
						long startTimeNanos=System.nanoTime();
						listener.penTock(availableMillis-=spentTimeMillis);
						spentTimeMillis=(System.nanoTime()-startTimeNanos)/NANOS_TO_MILLIS_DIV;
					}
					listeners.setActiveComponentInfo(null);
				}
			};

	MultiAwtPenOwner(){}

	PenManagerHandle getPenManagerHandle(){
		return penManagerHandle;
	}

	@Override
	public boolean enforceSinglePenManager(){
		return true;
	}

	@Override
	protected void init(){
		componentPool.setListener(this);
		penManagerHandle.getPenManager().pen.addListener(penMulticaster);
	}

	@Override
	public Component getActiveComponent() {
		return activeComponentInfo==null? null: activeComponentInfo.getComponent();
	}

	//@Override
	public void pointerComponentChanged(Component component){
		setActiveComponent(component);
	}

	private void setActiveComponent(Component component){
		synchronized(getPenSchedulerLock(component)){
			if(component==null){
				if(!startDraggingOut()){
					pause();
					activeComponentInfo=new ActiveComponentInfo(null);
				}
			}
			else{
				if(isDraggingOut()){
					if(component==getActiveComponent())
						if(!stopDraggingOut())
							throw new AssertionError();
				}else{
					activeComponentInfo=new ActiveComponentInfo(component);
					unpauser.enable();
				}
			}
		}
	}

	//@Override
	public void componentRemoved(Component component){
		System.out.println(Thread.currentThread().holdsLock(component.getTreeLock()));
		stopDraggingOutAndPause(component);
	}

	private void stopDraggingOutAndPause(Component component){
		synchronized(getPenSchedulerLock(component)){
			if(getActiveComponent()==component && stopDraggingOut())
				pause();
		}
	}

	// Override
	public void componentUndisplayable(final Component component){
		// we are holing component's treeLock here... we have to schedule stopDraggingOutAndPause for later:
		SwingUtilities.invokeLater(new Runnable(){
					//@Override
					public void run(){
						stopDraggingOutAndPause(component);
					}
				});
	}

	//@Override
	public void pointerComponentPenListenersChanged(Component pointerComponent){
		synchronized(getPenSchedulerLock()){
			if(pointerComponent==getActiveComponent())
				activeComponentInfo=new ActiveComponentInfo(pointerComponent);
		}
	}

	@Override
	protected void draggingOutDisengaged(){
		super.draggingOutDisengaged();
		Component pointerComponent=componentPool.getPointerComponent();
		if(pointerComponent!=null
			 && pointerComponent!=getActiveComponent())
			setActiveComponent(pointerComponent);
	}

	@Override
	public ActiveComponentInfo evalPenEventTag(PenEvent ev){
		return activeComponentInfo;
	}

	public Object getPenSchedulerLock(){
		return this.getPenSchedulerLock(null);
	}

	public Object getPenSchedulerLock(Component component){
		return super.getPenSchedulerLock(component);
	}
}