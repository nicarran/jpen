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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import jpen.event.PenManagerListener;
import jpen.provider.osx.CocoaProvider;
import jpen.provider.system.SystemProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

public final class PenManager {
	private static final Logger L=Logger.getLogger(PenManager.class.getName());
	public final Pen  pen=new Pen();
	public final Component component;
	private final Map<PenProvider.Constructor, PenProvider> constructorToProvider=new HashMap<PenProvider.Constructor, PenProvider>();
	private final Set<PenProvider.Constructor> constructors=new HashSet<PenProvider.Constructor>();
	private final Set<PenProvider.Constructor> constructorsA=Collections.unmodifiableSet(constructors);
	private final Map<PenProvider.Constructor, PenProvider.ConstructionException> constructorToException=new HashMap<PenProvider.Constructor, PenProvider.ConstructionException>();
	private volatile boolean paused;
	private final List<PenManagerListener> listeners=new ArrayList<PenManagerListener>();
	private byte nextDeviceId;

	private final DragOutHandler dragOutHandler;


	public PenManager(Component component) {
		this.component=component;
		addProvider(new SystemProvider.Constructor());
		addProvider(new XinputProvider.Constructor());
		addProvider(new WintabProvider.Constructor());
		addProvider(new CocoaProvider.Constructor());
		dragOutHandler=new DragOutHandler(this);
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
				for(PenDevice device:provider.getDevices())
					firePenDeviceAdded(constructor, device);
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
			device.setId(nextDeviceId++);
			for(PenManagerListener l: listeners){
				l.penDeviceAdded(constructor, device);
			}
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
	
	void setPaused(boolean paused) {
		if(this.paused==paused)
			return;
		this.paused=paused;
		if(paused)
			pen.scheduleButtonReleasedEvents();
		for(PenProvider provider: constructorToProvider.values())
			provider.penManagerPaused(paused);
	}

	public boolean getPaused() {
		return paused;
	}
	
	public void scheduleButtonEvent(PButton button) {
		if(paused)
			return;
		pen.scheduleButtonEvent(button);
	}
	
	public void scheduleScrollEvent(PScroll scroll) {
		if(paused)
			return;
		pen.scheduleScrollEvent(scroll);
	}
	
	public boolean scheduleLevelEvent(PenDevice device, Collection<PLevel> levels) {
		return scheduleLevelEvent(device, levels, System.currentTimeMillis());
	}
	
	public boolean scheduleLevelEvent(PenDevice device, Collection<PLevel> levels, long time) {
		if(paused)
			return false;
		switch(dragOutHandler.getMode()){
		case DISABLED:
			return pen.scheduleLevelEvent(device, levels, time, 0, component.getWidth(), 0, component.getHeight());
		case ENABLED:
			return pen.scheduleLevelEvent(device, levels, time,  -Integer.MAX_VALUE, Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE);
		default:
			throw new AssertionError();
		}
	}
}
