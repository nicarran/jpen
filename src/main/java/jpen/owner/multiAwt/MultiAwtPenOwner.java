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
import java.util.logging.Logger;
import jpen.event.PenListener;
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

	final ComponentPool componentPool=new ComponentPool();
	private ActiveComponentInfo activeComponentInfo;
	static class ActiveComponentInfo{
		final Component component;
		final PenListener[] penListeners;
		ActiveComponentInfo(Component component, PenListener[] penListeners){
			this.component=component;
			this.penListeners=penListeners;
		}
	}

	private final PenListener penMulticaster=new PenListener(){
				private PenListener[] listeners;

				//@Override
				public void penKindEvent(PKindEvent ev){
					updateListeners(ev);
					for(int i=0, size=listeners.length; i<size; i++)
						listeners[i].penKindEvent(ev);
				}

				private void updateListeners(PenEvent ev){
					ActiveComponentInfo activeComponentInfo=(ActiveComponentInfo)penManagerHandle.retrievePenEventTag(ev);
					listeners=activeComponentInfo.penListeners;
				}

				//@Override
				public void penLevelEvent(PLevelEvent ev){
					updateListeners(ev);
					for(int i=0, size=listeners.length; i<size; i++)
						listeners[i].penLevelEvent(ev);
				}
				//@Override
				public void penButtonEvent(PButtonEvent ev){
					updateListeners(ev);
					for(int i=0, size=listeners.length; i<size; i++)
						listeners[i].penButtonEvent(ev);
				}
				//@Override
				public void penScrollEvent(PScrollEvent ev){
					updateListeners(ev);
					for(int i=0, size=listeners.length; i<size; i++)
						listeners[i].penScrollEvent(ev);
				}
				
				private static final long NANOS_TO_MILLIS_DIV=1000000l;
				//@Override
				public void penTock(long availableMillis){
					long spentTimeMillis=0;
					for(int i=0, size=listeners.length; i<size; i++){
						long startTimeNanos=System.nanoTime();
						listeners[i].penTock(availableMillis-=spentTimeMillis);
						spentTimeMillis=(System.nanoTime()-startTimeNanos)/NANOS_TO_MILLIS_DIV;
					}
					listeners=null;
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
		return activeComponentInfo==null? null: activeComponentInfo.component;
	}

	//@Override
	public void pointerComponentChanged(Component component){
		activate(component);
	}

	private void activate(Component component){
		synchronized(penManagerHandle.getPenSchedulerLock()){
			if(component==null){
				if(!startDraggingOut()){
					pause();
					activeComponentInfo=new ActiveComponentInfo(null, componentPool.getPenListeners(null));
				}
			}
			else{
				if(isDraggingOut()){
					if(component==getActiveComponent())
						if(!stopDraggingOut())
							throw new AssertionError();
				}else{
					activeComponentInfo=new ActiveComponentInfo(component,
							componentPool.getPenListeners(component));
					unpauser.enable();
				}
			}
		}
	}

	@Override
	protected void draggingOutDisengaged(){
		super.draggingOutDisengaged();
		Component pointerComponent=componentPool.getPointerComponent();
		if(pointerComponent!=null
			 && pointerComponent!=getActiveComponent())
			activate(pointerComponent);
	}

	@Override
	public ActiveComponentInfo evalPenEventTag(PenEvent ev){
		return activeComponentInfo;
	}

}