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
import jpen.provider.Utils;
import jpen.provider.VirtualScreenBounds;

public class XinputProvider
	extends AbstractPenProvider {
	private static final Logger L=Logger.getLogger(XinputProvider.class.getName());
	public static final int PERIOD=10;

	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(new String[]{""}, new String[]{"x86_64", "ia64"});

	static void loadLibrary(){
		LIB_LOADER.load();
	}

	private final  Thread thread;
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
	}

	private XinputProvider(Constructor constructor) throws Exception {
		super(constructor);
		L.fine("start");

		XiBus xiBus=new XiBus();

		for(int xiDeviceIndex=xiBus.getXiDevicesSize(); --xiDeviceIndex>=0; ) {
			XiBus xiBus2=new XiBus(); // each device has a connection to the X server.
			try {
				xiBus2.setXiDevice(xiDeviceIndex);
			} catch(Exception ex) {
				continue;
			}
			devices.add(new XinputDevice(this, xiBus2.getXiDevice()));
		}

		xinputDevices=devices.toArray(new XinputDevice[devices.size()]);
		if(devices.size()==1){
			xinputDevices[0].setKindTypeNumber(PKind.Type.STYLUS.ordinal());
		}

		thread=new Thread("jpen-XinputProvider") {
						 public void run() {
							 while(true) {
								 processQuedEvents();
								 jpen.Utils.synchronizedWait(this, PERIOD);
								 while(getPenManager().getPaused()){
									 jpen.Utils.synchronizedWait(this, 0);
								 }
							 }
						 }
					 }
					 ;
		thread.setDaemon(true);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
		L.fine("end");
	}

	private void processQuedEvents() {
		for(int i=xinputDevices.length; --i>=0;)
			xinputDevices[i].processQuedEvents();
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
			synchronized(thread) {
				thread.notifyAll();
			}
		}
	}
}
