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