package jpen.owner;

import java.awt.geom.Point2D;
import java.awt.Point;
/**
Defines the screen area where {@link jpen.PenEvent}s are fired or a drag-out operation can be started.
*/
public interface PenClip{
	/**
	Evaluates the current location of this PenClip on the screen. {@code locationOnScreen} is in screen coordinates and is the origin of this PenClip coordinates.
	*/
	public void evalLocationOnScreen(Point locationOnScreen);
	/**
	@param point The point to test in PenClip coordinates.
	@return {@code true} if the given point is inside this PenClip.
	*/
	public boolean contains(Point2D.Float point);
}