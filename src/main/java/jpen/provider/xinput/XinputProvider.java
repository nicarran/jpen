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
	private final XinputDevice[] xipDevices;
	final VirtualScreenBounds screenBounds=VirtualScreenBounds.getInstance();

	public static class Constructor
		implements PenProvider.Constructor {
		//@Override
		public String getName() {
			return "XInput";
		}
		//@Override
		public boolean constructable() {
			return System.getProperty("os.name").toLowerCase().contains("linux");
		}

		//@Override
		public PenProvider construct(PenManager pm) throws ConstructionException {
			try {
				loadLibrary();
				return new XinputProvider(pm, this);
			} catch(Throwable t) {
				throw new ConstructionException(t);
			}
		}
	}

	private XinputProvider(PenManager penManager, Constructor constructor) throws Exception {
		super(penManager, constructor);
		L.fine("start");

		XiBus bus=new XiBus();

		for(int i=bus.getDevicesSize(); --i>=0; ) {
			XiBus bus2=new XiBus();
			try {
				bus2.setDevice(i);
			} catch(Exception ex) {
				continue;
			}
			devices.add(new XinputDevice(this, bus2.getDevice()));
		}

		xipDevices=devices.toArray(new XinputDevice[devices.size()]);

		thread=new Thread("jpen-XinputProvider") {
			       public synchronized void run() {
				       try {
					       while(true) {
						       processQuedEvents();
						       wait(PERIOD);
						       while(getPenManager().getPaused())
							       wait();
					       }
				       } catch(InterruptedException ex) { throw new Error(ex);}
			       }
		       }
		       ;
		thread.setDaemon(true);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
		L.fine("end");
	}

	private void processQuedEvents() {
		for(int i=xipDevices.length; --i>=0;)
			xipDevices[i].processQuedEvents();
	}

	private void resetDevices(){
		for(int i=xipDevices.length; --i>=0;)
			xipDevices[i].reset();
	}

	private void pauseDevices(boolean paused){
		for(int i=xipDevices.length; --i>=0;)
			xipDevices[i].device.setIsListening(!paused);
	}

	//@Override
	public void penManagerPaused(boolean paused) {
		pauseDevices(paused);
		if(!paused){
			screenBounds.reset();
			resetDevices();
			synchronized(thread) {
				thread.notifyAll();
			}
		}
	}
}