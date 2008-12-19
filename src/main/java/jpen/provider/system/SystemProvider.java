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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jpen.provider.AbstractPenProvider;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;

public class SystemProvider
	extends AbstractPenProvider {

	public static class Constructor
	extends AbstractPenProvider.AbstractConstructor {
		public static final String NAME="System";
		//@Override
		public String getName() {
			return NAME;
		}
		//@Override
		public boolean constructable() {
			return true;
		}
		//@Override
		protected PenProvider constructProvider() throws Throwable {
			return new SystemProvider(this);
		}
	}

	private SystemProvider(Constructor constructor) {
		super(constructor);
		devices.add(new MouseDevice(this));
	}

	//@Override
	public void penManagerPaused(boolean paused) {}
}
