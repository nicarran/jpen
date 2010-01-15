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

import java.util.EnumMap;
import java.util.Map;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PKind;
import jpen.provider.AbstractPenProvider;
import jpen.provider.NativeLibraryLoader;
import jpen.internal.BuildInfo;

public class CocoaProvider extends AbstractPenProvider {

	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(
				Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.osx.nativeVersion")));

	public static class Constructor
		extends AbstractPenProvider.AbstractConstructor{

		public String getName() {
			return "Cocoa";
		}

		//@Override
		public boolean constructable(PenManager penManager) {
			return System.getProperty("os.name").toLowerCase().contains("mac");
		}

		@Override
		public PenProvider constructProvider() throws Throwable {
			LIB_LOADER.load();
			CocoaAccess cocoaAccess=new CocoaAccess();
			return new CocoaProvider(this, cocoaAccess);
		}
		@Override
		public int getNativeVersion(){
			return LIB_LOADER.nativeVersion;
		}
		@Override
		public int getNativeBuild(){
			LIB_LOADER.load();
			return CocoaAccess.getNativeBuild();
		}
		@Override
		public int getExpectedNativeBuild(){
			return Integer.valueOf(BuildInfo.getProperties().
						 getString("jpen.provider.osx.nativeBuild"));
		}
	}


	private final CocoaAccess cocoaAccess;
	private final Map<PKind.Type, CocoaDevice> deviceMap;

	private CocoaProvider(final Constructor _constructor, final CocoaAccess _cocoaAccess) {
		super(_constructor);
		cocoaAccess = _cocoaAccess;
		cocoaAccess.setProvider(this);

		deviceMap = new EnumMap<PKind.Type, CocoaDevice>(PKind.Type.class);
		for (PKind.Type type : PKind.Type.VALUES) {
			final CocoaDevice device = new CocoaDevice(this, type);
			deviceMap.put(type, device);
			devices.add(device);
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