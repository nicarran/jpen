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
package jpen.event;

import jpen.PKindEvent;
import jpen.PLevelEvent;
import jpen.PScrollEvent;
import jpen.PButtonEvent;

public class PenAdapter
	implements PenListener {
	//@Override
	public void penKindEvent(PKindEvent ev) {}
	//@Override
	public void penLevelEvent(PLevelEvent ev) {}
	//@Override
	public void penButtonEvent(PButtonEvent ev) {}
	//@Override
	public void penScrollEvent(PScrollEvent ev) {}
	//@Override
	public void penTock(long availableMillis) {}
}
