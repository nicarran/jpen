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
package jpen.provider.xinput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PKind;
import jpen.provider.AbstractPenProvider;
import jpen.provider.NativeLibraryLoader;
import jpen.provider.VirtualScreenBounds;
import jpen.internal.BuildInfo;

public final class XinputProvider
	extends AbstractPenProvider {
	private static final Logger L=Logger.getLogger(XinputProvider.class.getName());

	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(new String[]{""},
			new String[]{"x86_64"},
			Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.xinput.nativeVersion")));

	static void loadLibrary(){
		LIB_LOADER.load();
	}

	private final XinputDevice[] xinputDevices;
	final VirtualScreenBounds screenBounds=VirtualScreenBounds.getInstance();

	public static class Constructor
		extends AbstractPenProvider.AbstractConstructor{
		//@Override
		public String getName() {
			return "XInput";
		}
		//@Override
		public boolean constructable(PenManager penManager) {
			return System.getProperty("os.name").toLowerCase().contains("linux");
		}

		@Override
		public PenProvider constructProvider() throws Throwable {
			loadLibrary();
			return new XinputProvider(this);
		}
		@Override
		public int getNativeVersion(){
			return LIB_LOADER.nativeVersion;
		}
		@Override
		public int getNativeBuild(){
			loadLibrary();
			return XiBus.getNativeBuild();
		}
		@Override
		public int getExpectedNativeBuild(){
			return Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.xinput.nativeBuild"));
		}
	}

	private XinputProvider(Constructor constructor) throws Exception {
		super(constructor);
		L.fine("start");

		XiBus xiBus=new XiBus();

		for(int xiDeviceIndex=xiBus.getXiDevicesSize(); --xiDeviceIndex>=0; ) {
			XiBus xiBus2=new XiBus(); // each XiBus opens a connection to the X server.
			try {
				xiBus2.setXiDevice(xiDeviceIndex);
			} catch(Exception ex) {
				continue;
			}
			XinputDevice xinputDevice=new XinputDevice(this, xiBus2.getXiDevice());
			devices.add(xinputDevice);
		}

		xinputDevices=devices.toArray(new XinputDevice[devices.size()]);
		if(devices.size()==1){
			xinputDevices[0].setKindTypeNumber(PKind.Type.STYLUS.ordinal());
		}
		L.fine("end");
	}

	private void resetXinputDevices(){
		for(int i=xinputDevices.length; --i>=0;)
			xinputDevices[i].reset();
	}

	private void pauseXinputDevices(boolean paused){
		for(int i=xinputDevices.length; --i>=0;)
			xinputDevices[i].setIsListening(!paused);
	}

	//@Override
	public void penManagerPaused(boolean paused) {
		pauseXinputDevices(paused);
		if(!paused){
			screenBounds.reset();
			resetXinputDevices();
		}
	}

	/*
	//v EXPERIMENTAL:
	@Override
	public boolean getUseRelativeLocationFilter(){
		return true;
}
	//^
	*/
}