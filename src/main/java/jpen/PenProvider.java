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

import java.util.Collection;
import jpen.owner.PenOwner;

/**
A {@code PenProvider } contains and maintains a collection of {@link PenDevice}s which access a pointer (pen tablet/mouse or similar) data source using an specific method (e.g. Wintab). Its main role is to feed pointer data using the following methods: {@link PenManager#scheduleLevelEvent(PenDevice device, long deviceTime, Collection levels, boolean levelsOnScreen)}, {@link PenManager#scheduleScrollEvent(PenDevice device, long deviceTime, PScroll scroll)}, and {@link PenManager#scheduleButtonEvent(PenDevice device, long deviceTime, PButton button)}. <p>

Each {@code PenDevice} has a {@link PKind.Type}. A tablet provider constructs typically three {@code PenDevice}s, each one initialized with {@link PKind.Type#ERASER} for the eraser, {@link PKind.Type#STYLUS} for the stylus, and {@link PKind.Type#CURSOR} for the mouse. <p>

The pointer creates its own thread (or uses native threads through JNI) to feed the data.
*/
public interface PenProvider {
	/**
	Each {@code PenProvider} is constructed using a {@code Constructor}. The available {@code Constructor}s are given by the {@link PenOwner#getPenProviderConstructors()} and are used by the {@link PenManager} to try to construct one {@code PenProvider} for each {@code Constructor}. 
	*/
	public interface Constructor {
		/**
		@return The name of this provider. It corresponds to the method used by the {@code PenProvider} to access the pen/tablet.
		*/
		String getName();
		/**
		@return {@code true} if the {@code PenProvider} can be constructed on this system, {@code false} otherwise.This method usually test for the name of the operating system and returns {@code true} if it matches an operating system in which this provider can run.
		*/
		boolean constructable(PenManager pm);
		/**
		This method constructs the {@code PenProvider}. It is called only when {@link #constructable(PenManager)} returns {@code true}.  When this methods completes, it is expected that the {@link #getConstructed()} method returns the {@code PenProvider} constructed. If the {@code PenProvider} couldn't be constructed due to some condition (e.g. the required native drivers are not present) then the {@link #getConstructionException()} method is expected to return an exception describing the condition.
		
		@return {@code true} if the {@code PenProvider} was constructed. {@code false} if the {@code PenProvider} couldn't be constructed.
		*/
		boolean construct(PenManager pm);
		/**
		@return The {@link PenManager} which called the {@link #construct(PenManager)} method. {@code null} if the {@code construct(PenManager)} has not being called.
		*/
		PenManager getPenManager();
		/**
		@return An exception describing an unexpected condition which prevented the {@code PenProvider} from being constructed. {@code null} if the {@code PenProvider} was constructed on the {@link #construct(PenManager)} method call or if it has not yet being called.
		*/
		ConstructionException getConstructionException();
		/**
		@return The {@code PenProvider} constructed when {@link #construct(PenManager)}  was called. {@code null} if it couldn't be constructed or if it has not yet being called. 
		*/
		PenProvider getConstructed();
		/**
		@return the native library version number. {@code -1} if the provider does not use a native library. The version is used to construct the native library name. 
		*/
		int getNativeVersion();
		/**
		@return the loaded native library build number. {@code -1} if the provider does not use a native library.
		*/
		int getNativeBuild();
		/**
		@return the expected native library build number. {@code -1} if the provider does not use a native library.
		*/
		int getExpectedNativeBuild();
	}

	/**
	A condition which prevented the {@code PenProvider} from being constructed on the {@link Constructor#construct(PenManager)} method call.
	*/
	public class ConstructionException extends Exception {
		public static final long serialVersionUID=1l;
		public ConstructionException(Throwable cause) {
			super(cause);
		}
		public ConstructionException(String m) {
			super(m);
		}
	}
	/**
	@return The {@code Constructor} which constructed this {@code PenProvider}.
	*/
	Constructor getConstructor();
	/**
	@return A {@code Collection} of devices currently owned by this {@code PenProvider}. This {@code Collection} can change over the lifetime of this {@code PenProvider}. Each time the {@code Collection} changes,  {@link PenManager#firePenDeviceAdded(PenProvider.Constructor, PenDevice)} or {@link PenManager#firePenDeviceRemoved(PenProvider.Constructor, PenDevice)} must be called to notify the change. Warning: For convenience, there is no need to call {@link PenManager#firePenDeviceAdded(PenProvider.Constructor, PenDevice)} when constructing the {@code PenProvider} inside the {@code Constructor#construct(PenManager)} method because in this case it is automatically called by the {@link PenManager} when calling the {@code Constructor}.
	*/
	Collection<PenDevice> getDevices();
	/**
	Called by the {@link PenManager} to notify that all the {@link PenDevice}s owned by this {@code PenProvider} must start/stop sending events. 
	
	@param paused If {@code true} then the devices must stop sending events. If {@code false} then the devices must start sending events. 
	*/
	void penManagerPaused(boolean paused);
	
	/**
	@return {@code true} if this provider needs a location filter to automatically detect if one of its devices is using mouse (relative) location mode and replace its movement levels values with mouse pointer location values.
	*/
	boolean getUseRelativeLocationFilter();
}
