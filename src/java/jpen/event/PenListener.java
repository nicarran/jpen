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

import jpen.*;

public interface PenListener {
	void penKindEvent(PKindEvent ev);
	void penLevelEvent(PLevelEvent ev);
	void penButtonEvent(PButtonEvent ev);
	void penScrollEvent(PScrollEvent ev);
	/**
	Useful to detect if the listeners are taking too much time processing events ({@code availableMillis<0}) causing pen lagging (pen events being queued at a higher rate than they are being processed).<p>
	The pen fires queued events at a given frequency (by default {@link jpen.Pen#DEFAULT_FREQUENCY}) in its own thread. Each cycle, after firing and processing the events, the pen calls jpen.event.PenListener.penTock(long availableMillis), where availableMillis is the time left of the period: {@code availableMillis=period-firingTime }, {@code period=1000/frequency}, and {@code firingTime} is the time spent in firing and processing events in the cycle.<p>
	This method is called from the event dispatch thread if {@link Pen#getFirePenTockOnSwing()} is {@code true}.
	
	@see Pen#setFirePenTockOnSwing(boolean)
	*/
	void penTock(long availableMillis); // TODO: Pen parameter??
}
