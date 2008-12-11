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
package jpen;

import java.util.Collection;

public interface PenProvider {
	public interface Constructor {
		PenManager getPenManager();
		String getName();
		boolean constructable();
		boolean construct(PenManager pm);
		ConstructionException getConstructionException();
		PenProvider getConstructed();
	}

	public class ConstructionException extends Exception {
		public static final long serialVersionUID=1l;
		public ConstructionException(Throwable cause) {
			super(cause);
		}
		public ConstructionException(String m) {
			super(m);
		}
	}
	Constructor getConstructor();
	Collection<PenDevice> getDevices();
	void penManagerPaused(boolean paused);
}
