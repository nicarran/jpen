/* [{
Copyright 2009 Nicolas Carranza <nicarran at gmail.com>

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

import jpen.provider.AbstractPenDevice;
import jpen.provider.AbstractPenProvider;

@SuppressWarnings("deprecation")
final class EmulationProvider
	extends AbstractPenProvider{

	final static class Constructor
		extends AbstractConstructor{
		//@Override
		public String getName(){
			return "JPen";
		}

		//@Override
		public boolean constructable(PenManager pm){
			return true;
		}

		@Override
		protected PenProvider constructProvider(){
			return new EmulationProvider(this);
		}
	}

	final PenDevice device;

	private EmulationProvider(Constructor constructor){
		super(constructor);
		devices.add(device=new Device());
	}

	final class Device
		extends AbstractPenDevice{
		private Device(){
			super(EmulationProvider.this);
			super.setKindTypeNumber(PKind.Type.IGNORE.ordinal());
			super.setEnabled(true); // but the enabled state is currently ignored on this device... it is considered as always enabled.
		}

		@Override
		public void setEnabled(boolean enabled){
			throw new UnsupportedOperationException("the enabled state can not be changed on the Emulation device");
		}

		//@Override
		public String getName(){
			return "Emulation";
		}
		@Override
		public void setKindTypeNumber(int kindType){
			throw new UnsupportedOperationException("the pen kind can not be changed on the Emulation device");
		}
	}

	//@Override
	public void penManagerPaused(boolean paused){}
}