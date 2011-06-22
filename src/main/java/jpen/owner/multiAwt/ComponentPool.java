package jpen.owner.multiAwt;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jpen.event.PenListener;

class ComponentPool{
	private final Map<Component, PenListener[]> componentToPenListeners=new HashMap<Component, PenListener[]>();

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
	/**
	Component under the mouse pointer.
	*/
	private Component pointerComponent;

	static interface Listener{
		void pointerComponentChanged(Component pointerComponent);
	}
	private Listener listener;

	synchronized Component getPointerComponent(){
		return pointerComponent;
	}

	private synchronized void setPointerComponent(Component pointerComponent){
		if(pointerComponent==this.pointerComponent)
			return;
		Component oldPointerComponent=this.pointerComponent;
		this.pointerComponent=pointerComponent;
		firePointerComponentChanged();
	}

	private void firePointerComponentChanged(){
		Listener listener=this.listener;
		if(listener!=null)
			listener.pointerComponentChanged(pointerComponent);
	}

	void setListener(Listener listener){
		this.listener=listener;
	}

	synchronized void addPenListener(Component component, PenListener penListener){
		if(component==null || penListener==null)
			throw new NullPointerException();
		PenListener[] penListeners=componentToPenListeners.get(component);
		if(penListeners==null){
			componentToPenListeners.put(component, new PenListener[]{penListener});
			component.addMouseListener(mouseL);
		}else{
			Set<PenListener> newPenListeners=new LinkedHashSet<PenListener>(Arrays.asList(penListeners));
			if(newPenListeners.add(penListener)){
				componentToPenListeners.put(component, newPenListeners.toArray(new PenListener[newPenListeners.size()]));
			}
		}
	}

	synchronized void removePenListener(Component component, PenListener penListener){
		PenListener[] penListeners=componentToPenListeners.get(component);
		if(penListeners==null)
			return;
		List<PenListener> newPenListeners=new ArrayList<PenListener>(Arrays.asList(penListeners));
		if(newPenListeners.remove(penListener)){
			if(newPenListeners.isEmpty()){
				componentToPenListeners.remove(component);
				component.removeMouseListener(mouseL);
				if(pointerComponent==component)
					setPointerComponent(null);
			}else{
				componentToPenListeners.put(component, newPenListeners.toArray(new PenListener[newPenListeners.size()]));
			}
		}
	}

	private static final PenListener[] emptyPenListeners=new PenListener[0];

	synchronized PenListener[] getPenListeners(Component component){
		if(component==null)
			return emptyPenListeners;
		PenListener[] penListeners=componentToPenListeners.get(component);
		return penListeners==null? emptyPenListeners: penListeners;
	}
}