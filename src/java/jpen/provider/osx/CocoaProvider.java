/* [{
Copyright 2008 Brien Colwell

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
package jpen.provider.osx;

import java.util.HashMap;
import java.util.Map;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PKind;
import jpen.provider.AbstractPenProvider;
import jpen.provider.NativeLibraryLoader;
import jpen.provider.Utils;

public class CocoaProvider extends AbstractPenProvider {
	
	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(); 
	
	public static class Constructor implements PenProvider.Constructor {
		public Constructor() {
		}

		public String getName() {
			return "Cocoa";
		}

		public boolean constructable() {
			return System.getProperty("os.name").toLowerCase().contains("mac");
		}

		public PenProvider construct(PenManager pm) throws ConstructionException {
			try {
				LIB_LOADER.load();
				CocoaAccess cocoaAccess=new CocoaAccess();
				return new CocoaProvider(pm, this, cocoaAccess);
			} catch(Throwable t) {
				throw new ConstructionException(t);
			}
		}
	}


	private final CocoaAccess cocoaAccess;
	private final Map<PKind.Type, CocoaDevice> deviceMap;

	private CocoaProvider(final PenManager _penManager,
	    final Constructor _constructor, final CocoaAccess _cocoaAccess
	                     ) {
		super(_penManager, _constructor);
		cocoaAccess = _cocoaAccess;
		cocoaAccess.setProvider(this);

		deviceMap = new HashMap<PKind.Type, CocoaDevice>(3);
		for (PKind.Type type : PKind.Type.VALUES) {
			final CocoaDevice device = new CocoaDevice(this, type);
			deviceMap.put(type, device);
			getPenManager().firePenDeviceAdded(getConstructor(), device);
		}

		cocoaAccess.start();
	}

	public CocoaDevice getDevice(final PKind.Type type) {
		return deviceMap.get(type);
	}

	public void penManagerPaused(final boolean paused) {
		if (! paused) {
			cocoaAccess.enable();
		}
		else {
			cocoaAccess.disable();
		}
	}


	public void dispose() {
		cocoaAccess.stop();
	}
}
