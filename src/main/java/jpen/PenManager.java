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
import jpen.owner.awt.AwtPenOwner;
import jpen.owner.PenOwner;
import jpen.provider.system.SystemProvider;

public final class PenManager {
	private static final Logger L=Logger.getLogger(PenManager.class.getName());

	final PLevelEmulator levelEmulator=new PLevelEmulator(this);
	public final Pen  pen=new Pen(levelEmulator);
	/**
	@deprecated Use {@link #penOwner}.
	*/
	@Deprecated
	public final Component component;
	public final PenOwner penOwner;
	private final Set<PenProvider.Constructor> providerConstructors=new HashSet<PenProvider.Constructor>();
	private final Set<PenProvider.Constructor> providerConstructorsA=Collections.unmodifiableSet(providerConstructors);
	private final Map<Byte, PenDevice> deviceIdToDevice=new HashMap<Byte, PenDevice>();
	private final Collection<PenDevice> devicesA=Collections.unmodifiableCollection(deviceIdToDevice.values());
	private byte nextDeviceId;
	private volatile boolean paused=true;
	private final List<PenManagerListener> listeners=new ArrayList<PenManagerListener>();
	private PenManagerListener[] listenersArray;
	@Deprecated
	private PenDevice systemMouseDevice;

	public PenManager(Component component) {
		this(new AwtPenOwner(component));
	}

	public PenManager(PenOwner penOwner){
		this.penOwner=penOwner;
		penOwner.setPenManagerHandle(new PenOwner.PenManagerHandle(){
					//@Override
					public final PenManager getPenManager(){
						return PenManager.this;
					}
					//@Override
					public final Object getPenSchedulerLock(){
						return pen.scheduler;
					}
					//@Override
					public final void setPenManagerPaused(boolean paused){
						PenManager.this.setPaused(paused);
					}
				});
		this.component=penOwner instanceof AwtPenOwner?
									 ((AwtPenOwner)penOwner).component: null;
		for(PenProvider.Constructor penProviderConstructor: penOwner.getPenProviderConstructors())
			addProvider(penProviderConstructor);
	}

	/**
	Constructs and adds provider if {@link PenProvider.Constructor#constructable(PenManager)} is true.
	@return The {@link PenProvider} added or null if it couldn't be constructed.
	*/
	private  PenProvider addProvider(PenProvider.Constructor providerConstructor) {
		if(providerConstructor.constructable(this)) {
			if(!this.providerConstructors.add(providerConstructor))
				throw new IllegalArgumentException("constructor already added");
			if(providerConstructor.construct(this)){
				PenProvider provider=providerConstructor.getConstructed();
				for(PenDevice device:provider.getDevices())
					firePenDeviceAdded(providerConstructor, device);
				return provider;
			}
		}
		return null;
	}

	public void addListener(PenManagerListener l) {
		synchronized(listeners) {
			listeners.add(l);
			listenersArray=null;
		}
	}

	public void removeListener(PenManagerListener l) {
		synchronized(listeners) {
			listeners.remove(l);
			listenersArray=null;
		}
	}

	PenManagerListener[] getListenersArray(){
		synchronized(listeners){
			if(listenersArray==null)
				listenersArray=listeners.toArray(new PenManagerListener[listeners.size()]);
			return listenersArray;
		}
	}

	public void firePenDeviceAdded(PenProvider.Constructor constructor, PenDevice device) {
		byte nextDeviceId=getNextDeviceId();
		device.setId(nextDeviceId);
		if(deviceIdToDevice.put(nextDeviceId, device)!=null)
			throw new AssertionError();
		if(constructor.getName().equals(SystemProvider.Constructor.NAME))
			systemMouseDevice=device;
		for(PenManagerListener l: getListenersArray()){
			l.penDeviceAdded(constructor, device);
		}
	}

	private byte getNextDeviceId(){
		Set<Byte> deviceIds=deviceIdToDevice.keySet();
		while(deviceIds.contains(Byte.valueOf(nextDeviceId)))
			nextDeviceId++;
		return nextDeviceId;
	}

	public void firePenDeviceRemoved(PenProvider.Constructor constructor, PenDevice device) {
		if(deviceIdToDevice.remove(device.getId())==null)
			throw new IllegalArgumentException("device not found");
		for(PenManagerListener l: getListenersArray())
			l.penDeviceRemoved(constructor, device);
	}

	public PenDevice getDevice(byte deviceId){
		return deviceIdToDevice.get(Byte.valueOf(deviceId));
	}

	public Collection<PenDevice> getDevices(){
		return devicesA;
	}

	/**
	@deprecated There is no replacement.
	*/
	@Deprecated
	public PenDevice getSystemMouseDevice(){
		return systemMouseDevice;
	}

	public Set<PenProvider.Constructor> getConstructors() {
		return providerConstructorsA;
	}

	void setPaused(boolean paused) {
		if(this.paused==paused)
			return;
		this.paused=paused;
		for(PenProvider.Constructor providerConstructor: providerConstructors){
			PenProvider penProvider=providerConstructor.getConstructed();
			if(penProvider!=null)
				penProvider.penManagerPaused(paused);
		}
		if(paused)
			pen.scheduler.scheduleButtonReleasedEvents();
	}

	public boolean getPaused() {
		return paused;
	}

	/**
	Schedules button events. You must construct a new {@code PButton} each time you call this method (do not reuse).
	*/
	public void scheduleButtonEvent(PButton button) {
		if(paused)
			return;
		pen.scheduler.scheduleButtonEvent(button);
	}

	/**
	Schedules scroll events. You must construct a new {@code PScroll} each time you call this method (do not reuse).
	*/
	public void scheduleScrollEvent(PenDevice device, PScroll scroll) {
		if(paused)
			return;
		pen.scheduler.scheduleScrollEvent(device, scroll);
	}

	public boolean scheduleLevelEvent(PenDevice device, Collection<PLevel> levels) {
		return scheduleLevelEvent(device, levels, System.currentTimeMillis());
	}

	public boolean scheduleLevelEvent(PenDevice device, Collection<PLevel> levels, long deviceTime) {
		return scheduleLevelEvent(device, levels, deviceTime, false);
	}

	/**
	Schedules level events. You can reuse the levels {@code Collection} but you must construct new {@code PLevel}s each time you call this method.
	*/
	public boolean scheduleLevelEvent(PenDevice device, Collection<PLevel> levels, long deviceTime, boolean levelsOnScreen) {
		if(paused)
			return false;
		return pen.scheduler.scheduleLevelEvent(this, device, deviceTime, levels, levelsOnScreen);
	}
}