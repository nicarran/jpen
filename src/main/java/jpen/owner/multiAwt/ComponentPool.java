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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jpen.event.PenListener;

final class ComponentPool{
	private final MultiAwtPenOwner multiAwtPenOwner;
	private final Map<Component, PenListener[]> componentToPenListeners=Collections.synchronizedMap(new HashMap<Component, PenListener[]>());

	ComponentPool(MultiAwtPenOwner multiAwtPenOwner){
		this.multiAwtPenOwner=multiAwtPenOwner;
	}

	private final MouseListener mouseL=new MouseAdapter(){
				@Override
				public void mouseEntered(MouseEvent ev){
					setPointerComponent(ev.getComponent());
				}
				@Override
				public void mouseExited(MouseEvent ev){
					setPointerComponent(null);
				}

			};
	private final HierarchyListener hierarchyL=new HierarchyListener(){
				//@Override
				public void hierarchyChanged(HierarchyEvent ev){
					if(ev.getID() == ev.HIERARCHY_CHANGED &&
						 (ev.getChangeFlags() &  ev.DISPLAYABILITY_CHANGED)>0 &&
						 !ev.getComponent().isDisplayable())
						fireComponentUndisplayable(ev.getComponent());
				}
			};

	/**
	Component under the mouse pointer.
	*/
	private Component pointerComponent;

	static interface Listener{
		void pointerComponentChanged(Component pointerComponent);
		void pointerComponentPenListenersChanged(Component pointerComponent);
		void componentRemoved(Component component);
		void componentUndisplayable(Component component);
	}
	private Listener listener;

	Component getPointerComponent(){
		synchronized(multiAwtPenOwner.getPenSchedulerLock()){
			return pointerComponent;
		}
	}

	private void setPointerComponent(Component pointerComponent){
		synchronized(multiAwtPenOwner.getPenSchedulerLock(pointerComponent)){
			if(pointerComponent==this.pointerComponent)
				return;
			Component oldPointerComponent=this.pointerComponent;
			this.pointerComponent=pointerComponent;
			firePointerComponentChanged();
		}
	}

	private void firePointerComponentChanged(){
		Listener listener=this.listener;
		if(listener!=null)
			listener.pointerComponentChanged(pointerComponent);
	}

	void setListener(Listener listener){
		this.listener=listener;
	}

	void addPenListener(Component component, PenListener penListener){
		synchronized(multiAwtPenOwner.getPenSchedulerLock()){
			if(component==null || penListener==null)
				throw new NullPointerException();
			PenListener[] penListeners=componentToPenListeners.get(component);
			if(penListeners==null){
				componentToPenListeners.put(component, new PenListener[]{penListener});
				component.addMouseListener(mouseL);
				component.addHierarchyListener(hierarchyL);
				if(component.getMousePosition()!=null){
					setPointerComponent(component);
				}
			}else{
				Set<PenListener> newPenListeners=new LinkedHashSet<PenListener>(Arrays.asList(penListeners));
				if(newPenListeners.add(penListener)){
					componentToPenListeners.put(component, newPenListeners.toArray(new PenListener[newPenListeners.size()]));
					firePointerComponentPenListenersChanged(component);
				}
			}
		}
	}

	private void firePointerComponentPenListenersChanged(Component component){
		if(pointerComponent!=component)
			return;
		Listener listener=this.listener;
		if(listener!=null)
			listener.pointerComponentPenListenersChanged(component);
	}

	void removePenListener(Component component, PenListener penListener){
		synchronized(multiAwtPenOwner.getPenSchedulerLock(component)){
			PenListener[] penListeners=componentToPenListeners.get(component);
			if(penListeners==null)
				return;
			List<PenListener> newPenListeners=new ArrayList<PenListener>(Arrays.asList(penListeners));
			if(newPenListeners.remove(penListener)){
				if(newPenListeners.isEmpty()){
					componentToPenListeners.remove(component);
					component.removeMouseListener(mouseL);
					component.removeHierarchyListener(hierarchyL);
					if(pointerComponent==component)
						setPointerComponent(null);
					fireComponentRemoved(component);
				}else{
					componentToPenListeners.put(component, newPenListeners.toArray(new PenListener[newPenListeners.size()]));
					firePointerComponentPenListenersChanged(component);
				}
			}
		}
	}

	private void fireComponentRemoved(Component component){
		Listener listener=this.listener;
		if(listener!=null)
			listener.componentRemoved(component);
	}

	private void fireComponentUndisplayable(Component component){
		Listener listener=this.listener;
		if(listener!=null)
			listener.componentUndisplayable(component);
	}

	private static final PenListener[] emptyPenListeners=new PenListener[0];

	PenListener[] getPenListeners(Component component){
		if(component==null)
			return emptyPenListeners;
		PenListener[] penListeners=componentToPenListeners.get(component);
		return penListeners==null? emptyPenListeners: penListeners;
	}
}