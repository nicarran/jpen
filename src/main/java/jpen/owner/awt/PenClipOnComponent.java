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
package jpen.owner.awt;

import java.awt.Component;
import java.awt.geom.Point2D;
import java.awt.Point;
import javax.swing.SwingUtilities;
import jpen.owner.PenClip;

final class PenClipOnComponent
	implements PenClip{
	final Component component;

	public PenClipOnComponent(Component component){
		this.component=component;
	}

	//@Override
	public void evalLocationOnScreen(Point pointOnScreen){
		pointOnScreen.x=pointOnScreen.y=0;
		SwingUtilities.convertPointToScreen(pointOnScreen, component);
	}

	//@Override
	public boolean contains(Point2D.Float point){
		if(point.x<0 || point.y<0 ||
			 point.x>component.getWidth() ||
			 point.y>component.getHeight())	
			return false;
		return true;
	}
}