/* [{
Copyright 2007-2011 Nicolas Carranza <nicarran at gmail.com>

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

final class ComponentPenClip
	implements PenClip{
	final ComponentPenOwner componentPenOwner;

	public ComponentPenClip(ComponentPenOwner componentPenOwner){
		this.componentPenOwner=componentPenOwner;
	}
	
		//@Override
	public void evalLocationOnScreen(Point pointOnScreen){
		Component activeComponent=componentPenOwner.getActiveComponent();
		if(activeComponent==null)
			return;
		pointOnScreen.x=pointOnScreen.y=0;
		SwingUtilities.convertPointToScreen(pointOnScreen, activeComponent);
	}

	//@Override
	public boolean contains(Point2D.Float point){
		Component activeComponent=componentPenOwner.getActiveComponent();
		if(activeComponent==null)
			return false;
		if(point.x<0 || point.y<0 ||
			 point.x>activeComponent.getWidth() ||
			 point.y>activeComponent.getHeight()){
			return false;
		}
		return true;
	}
}