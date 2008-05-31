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
package jpen.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jpen.PenProvider;
import jpen.PenDevice;
import jpen.PenManager;

public abstract class AbstractPenProvider
	implements PenProvider {
	private final PenManager penManager;
	private final Constructor constructor;
	protected final List<PenDevice> devices=new ArrayList<PenDevice>();
	private final List<PenDevice> devicesA=Collections.unmodifiableList(devices);

	protected AbstractPenProvider(PenManager penManager, Constructor constructor) {
		this.penManager=penManager;
		this.constructor=constructor;
	}

	//@Override
	public Collection<? extends PenDevice> getDevices() {
		return devicesA;
	}

	//@Override
	public final PenManager getPenManager() {
		return penManager;
	}

	//@Override
	public final Constructor getConstructor() {
		return constructor;
	}

	//@Override
	public String toString() {
		return "[PenProvider: constructor.name="+getConstructor().getName()+"]";
	}
}
