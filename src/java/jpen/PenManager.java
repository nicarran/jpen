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
import java.awt.Point;
import java.awt.Toolkit;
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
import jpen.event.PenListener;
import jpen.event.PenManagerListener;
import jpen.provider.system.SystemProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

public final class PenManager {
	public final Pen  pen=new Pen();
	public final Component component;
	private final Map<PenProvider.Constructor, PenProvider> constructorToProvider=new HashMap<PenProvider.Constructor, PenProvider>();
	private final Set<PenProvider.Constructor> constructors=new HashSet<PenProvider.Constructor>();
	private final Set<PenProvider.Constructor> constructorsA=Collections.unmodifiableSet(constructors);
	private final Map<PenProvider.Constructor, PenProvider.ConstructionException> constructorToException=new HashMap<PenProvider.Constructor, PenProvider.ConstructionException>();
	private volatile boolean paused;
	private final List<PenManagerListener> listeners=new ArrayList<PenManagerListener>();

	private class Pauser
				extends MouseAdapter
		implements PenListener{
		private boolean isDragging;
		@Override
		public synchronized void mouseEntered(MouseEvent ev) {
			if(isDragging){
				isDragging=false;
				pen.removeListener(this);
			}else
				setPaused(false);
		}
		@Override
		public synchronized void mouseExited(MouseEvent ev) {
			if(isDragging)
				throw new AssertionError();
			if(pen.hasAnyButtonPressed()){
				isDragging=true;
				pen.addListener(this);
			}else
				setPaused(true);
		}

		@Override
		public 	void penKindEvent(PKindEvent ev){
		}

		@Override
		public void penLevelEvent(PLevelEvent ev){
		}

		@Override
		public synchronized void penButtonEvent(PButtonEvent ev){
			if(!ev.button.value)
				if(!pen.hasAnyButtonPressed()){
					if(isDragging){
						setPaused(true); // causes button release schedule but there may be level events still to be processed in the pen event queue (here I'm in the queue processing thread).
						isDragging=false;
						pen.removeListener(this);
					}
					else
						throw new AssertionError();
				}
		}

		@Override
		public void penScrollEvent(PScrollEvent ev){
		}

		@Override
		public void penTock(long availableMillis){
		}

	}
	private final Pauser pauser=new Pauser();


	public PenManager(Component component) {
		this.component=component;
		component.addMouseListener(pauser);
		addProvider(new SystemProvider.Constructor());
		addProvider(new XinputProvider.Constructor());
		addProvider(new WintabProvider.Constructor());
		setPaused(true);
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

	private void setPaused(boolean paused) {
		this.paused=paused;
		if(paused)
			pen.scheduleButtonReleasedEvents();
		for(PenProvider provider: constructorToProvider.values())
			provider.penManagerPaused(paused);
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
		return pen.scheduleLevelEvent(device, levels);
	}
}
