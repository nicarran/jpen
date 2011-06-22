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
package jpen.provider.system;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jpen.owner.awt.ComponentPenOwner;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.provider.AbstractPenProvider;

public final class SystemProvider
	extends AbstractPenProvider {

	public static class Constructor
		extends AbstractPenProvider.AbstractConstructor {
		public static final String NAME="System";
		//@Override
		public String getName() {
			return NAME;
		}
		//@Override
		public boolean constructable(PenManager penManager) {
			return penManager.penOwner instanceof ComponentPenOwner;
		}
		@Override
		protected PenProvider constructProvider() throws Throwable {
			ComponentPenOwner componentPenOwner=(ComponentPenOwner)getPenManager().penOwner;
			return new SystemProvider(this, componentPenOwner);
		}
	}

	final ComponentPenOwner componentPenOwner;
	private final MouseDevice mouseDevice=new MouseDevice(this);
	private final KeyboardDevice keyboardDevice=new KeyboardDevice(this);

	private SystemProvider(Constructor constructor, ComponentPenOwner componentPenOwner) {
		super(constructor);
		this.componentPenOwner=componentPenOwner;
		devices.add(mouseDevice);
		devices.add(keyboardDevice);
	}

	//@Override
	public void penManagerPaused(boolean paused) {
		mouseDevice.setPaused(paused);
		keyboardDevice.setPaused(paused);
	}
}