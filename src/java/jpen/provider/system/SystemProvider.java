/* [{
* (C) Copyright 2007 Nicolas Carranza and individual contributors.
* See the jpen-copyright.txt file in the jpen distribution for a full
* listing of individual contributors.
*
* This file is part of jpen.
*
* jpen is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* jpen is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with jpen.  If not, see <http://www.gnu.org/licenses/>.
* }] */
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
		implements PenProvider.Constructor {
		@Override
		public String getName() {
			return "System";
		}
		@Override
		public boolean constructable() {
			return true;
		}
		@Override
		public PenProvider construct(PenManager pm) throws ConstructionException {
			return new SystemProvider(pm, this);
		}
	}

	private SystemProvider(PenManager penManager, Constructor constructor) {
		super(penManager, constructor);
		devices.add(new MouseDevice(this));
	}

	@Override
	public void penManagerPaused(boolean paused) {}}
