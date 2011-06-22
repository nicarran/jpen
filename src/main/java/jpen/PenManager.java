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
import jpen.internal.BuildInfo;
import jpen.owner.awt.AwtPenOwner;
import jpen.owner.PenOwner;
import jpen.provider.system.MouseDevice;
/**
Create a {@code PenManager} to start using JPen, {@link jpen.owner.multiAwt.AwtPenToolkit} contains one ready to be used. 
*/
public final class PenManager {
	private static final Logger L=Logger.getLogger(PenManager.class.getName());

	public static String getJPenFullVersion(){
		return BuildInfo.getFullVersion();
	}

	private static int instanceCount;
	private static boolean singletonMode;

	private synchronized static void incrementInstanceCount(){
		if(singletonMode && instanceCount!=0)
			throw new IllegalStateException("can't create more than one PenManager when in singleton mode");
		instanceCount++;
	}
	
	/**
	See {@link PenOwner#enforceSinglePenManager()}.
	*/
	private synchronized static void setSingletonMode(boolean singletonMode){
		if(singletonMode && instanceCount>1)
			throw new IllegalStateException("can't change singleton mode to true when many PenManagers has already being created");
		PenManager.singletonMode=singletonMode;
	}

	public final Pen  pen=new Pen(this);
	public final PenOwner penOwner;
	private final Set<PenProvider.Constructor> providerConstructors=new HashSet<PenProvider.Constructor>();
	private final Set<PenProvider.Constructor> providerConstructorsA=Collections.unmodifiableSet(providerConstructors);
	private final Map<Byte, PenDevice> deviceIdToDevice=new HashMap<Byte, PenDevice>(Byte.MAX_VALUE, 1f);
	private final Collection<PenDevice> devicesA=Collections.unmodifiableCollection(deviceIdToDevice.values());
	private byte nextDeviceId;
	private volatile boolean paused=true;
	private final List<PenManagerListener> listeners=new ArrayList<PenManagerListener>();
	private PenManagerListener[] listenersArray;
	final PenDevice emulationDevice;
	private PenDevice systemMouseDevice; // may be null

	/**
	Creates an {@code AwtPenOwner} and calls the {@link #PenManager(PenOwner)} constructor. <b>Warning:</b> see {@link jpen.owner.awt.AwtPenOwner#AwtPenOwner(Component)}.
	*/
	public PenManager(Component component) {
		this(new AwtPenOwner(component));
	}

	public PenManager(PenOwner penOwner){
		if(penOwner.enforceSinglePenManager())
			setSingletonMode(true);
		incrementInstanceCount();
		this.penOwner=penOwner;
		synchronized(pen.scheduler){
			PenProvider.Constructor emulationProviderConstructor=new EmulationProvider.Constructor();
			addProvider(emulationProviderConstructor);
			@SuppressWarnings("unchecked")
			EmulationProvider emulationProvider=(EmulationProvider)emulationProviderConstructor.getConstructed();
			emulationDevice=emulationProvider.device;

			for(PenProvider.Constructor penProviderConstructor: penOwner.getPenProviderConstructors())
				addProvider(penProviderConstructor);
			penOwner.setPenManagerHandle(new PenOwner.PenManagerHandle(){
						//@Override
						public PenManager getPenManager(){
							return PenManager.this;
						}
						//@Override
						public Object getPenSchedulerLock(){
							return pen.scheduler;
						}
						//@Override
						public void setPenManagerPaused(boolean paused){
							PenManager.this.setPaused(paused);
						}
						//@Override
						public Object retrievePenEventTag(PenEvent ev){
							return ev.getPenOwnerTag();
						}
					});
		}
	}

	/**
	@return the mouse PenProvider or {@code null} if no mouse provider has been added.
	@see #addProvider(PenProvider.Constructor)
	*/
	public PenProvider getSystemMouseProvider(){
		return systemMouseDevice==null? null: systemMouseDevice.getProvider();
	}

	public boolean isSystemMouseDevice(PenDevice device){
		return device!=null && device==systemMouseDevice;
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
		device.penManagerSetId(nextDeviceId);
		if(deviceIdToDevice.put(nextDeviceId, device)!=null)
			throw new AssertionError();
		if(systemMouseDevice==null && device instanceof MouseDevice)
			this.systemMouseDevice=device;
		for(PenManagerListener l: getListenersArray()){
			l.penDeviceAdded(constructor, device);
		}
	}

	private byte getNextDeviceId(){
		Set<Byte> deviceIds=deviceIdToDevice.keySet();
		while(deviceIds.contains(Byte.valueOf(nextDeviceId)))
			nextDeviceId++;
		if(nextDeviceId<0)
			throw new IllegalStateException();
		return nextDeviceId;
	}

	public void firePenDeviceRemoved(PenProvider.Constructor constructor, PenDevice device) {
		if(deviceIdToDevice.remove(device.getId())==null)
			throw new IllegalArgumentException("device not found");
		for(PenManagerListener l: getListenersArray())
			l.penDeviceRemoved(constructor, device);
		if(systemMouseDevice==device)
			this.systemMouseDevice=null;
	}

	public PenDevice getDevice(byte deviceId){
		return deviceIdToDevice.get(Byte.valueOf(deviceId));
	}

	public Collection<PenDevice> getDevices(){
		return devicesA;
	}

	/**
	@deprecated replacement: #getProviderConstructors().
	*/
	@Deprecated
	public Set<PenProvider.Constructor> getConstructors() {
		return providerConstructorsA;
	}

	public Set<PenProvider.Constructor> getProviderConstructors() {
		return providerConstructorsA;
	}

	void setPaused(boolean paused) {
		if(this.paused==paused)
			return;
		pen.scheduler.setPaused(paused);
		this.paused=paused;
		for(PenProvider.Constructor providerConstructor: providerConstructors){
			PenProvider penProvider=providerConstructor.getConstructed();
			if(penProvider!=null)
				penProvider.penManagerPaused(paused);
		}
	}

	public boolean getPaused() {
		return paused;
	}

	/**
	Schedules button events. You must construct a new {@code PButton} each time you call this method (do not reuse).
	*/
	public void scheduleButtonEvent(PenDevice device, long deviceTime, PButton button) {
		if(paused)
			return;
		pen.scheduler.scheduleButtonEvent(device, deviceTime, button);
	}

	/**
	Schedules scroll events. You must construct a new {@code PScroll} each time you call this method (do not reuse).
	*/
	public void scheduleScrollEvent(PenDevice device, long deviceTime, PScroll scroll) {
		if(paused)
			return;
		pen.scheduler.scheduleScrollEvent(device, deviceTime, scroll);
	}

	public boolean scheduleLevelEvent(PenDevice device, long deviceTime, Collection<PLevel> levels) {
		return scheduleLevelEvent(device, deviceTime, levels, false);
	}

	/**
	Schedules level events. You can reuse the levels {@code Collection} but you must construct new {@code PLevel}s each time you call this method.
	*/
	public boolean scheduleLevelEvent(PenDevice device, long deviceTime, Collection<PLevel> levels, boolean levelsOnScreen) {
		if(paused)
			return false;
		return pen.scheduler.scheduleLevelEvent(device, deviceTime, levels, levelsOnScreen);
	}

	/**
	Uses reflection to get the first provider of the given class.
	*/
	public <T extends PenProvider> T getProvider(Class<T> providerClass){
		for(PenProvider.Constructor constructor: getProviderConstructors()){
			PenProvider penProvider=constructor.getConstructed();
			if(providerClass.isInstance(penProvider))
				return providerClass.cast(penProvider);
		}
		return null;
	}
}