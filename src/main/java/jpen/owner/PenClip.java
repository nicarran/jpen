/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.owner;

import java.awt.geom.Point2D;
import java.awt.Point;
/**
Defines the screen area where {@link jpen.PenEvent}s are fired or a drag-out operation can be started.
*/
public interface PenClip{
	/**
	Evaluates the current location of the origin of this PenClip on the screen, using screen coordinates. This method is called while holding the {@link PenOwner.PenManagerHandle#getPenSchedulerLock()}.
	@param locationOnScreen a Point to put the evaluated result on. 
	*/
	public void evalLocationOnScreen(Point locationOnScreen);
	/**
	 This method is called while holding the {@link PenOwner.PenManagerHandle#getPenSchedulerLock()}.
	@param point The point to test in PenClip coordinates.
	@return {@code true} if the given point is inside this PenClip.
	*/
	public boolean contains(Point2D.Float point);
}