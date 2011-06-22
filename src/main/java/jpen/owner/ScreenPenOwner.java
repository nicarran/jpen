/* [{
Copyright 2009 Marcello Bastea-Forte <marcello at cellosoft.com>
Copyright 2009 Nicolas Carranza <nicarran at gmail.com>

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
import java.awt.Window;
import java.util.Arrays;
import java.util.Collection;
import jpen.internal.ActiveWindowProperty;
import jpen.PenEvent;
import jpen.PenProvider;
import jpen.provider.osx.CocoaProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;
/**
Defines a {@link PenClip} for all the screen. Its {@link jpen.PenManager} is unpaused when the current application has an active window.
*/
public class ScreenPenOwner implements PenOwner {

	private static ScreenPenOwner instance;

	public synchronized static ScreenPenOwner getInstance(){
		return instance==null? instance=new ScreenPenOwner():
					 instance;
	}

	//@Override
	public Collection<PenProvider.Constructor> getPenProviderConstructors(){
		return Arrays.asList(
						 new PenProvider.Constructor[]{
							 // new SystemProvider.Constructor(), //Does not work because it needs a java.awt.Component to register the MouseListener
							 new XinputProvider.Constructor(),
							 new WintabProvider.Constructor(),
							 new CocoaProvider.Constructor()
						 }
					 );
	}

	//@Override
	public void setPenManagerHandle(final PenManagerHandle penManagerHandle){
		ActiveWindowProperty.Listener activeWindowPL=new ActiveWindowProperty.Listener(){
					//@Override
					public void activeWindowChanged(Window activeWindow){
						synchronized(penManagerHandle.getPenSchedulerLock()){
							penManagerHandle.setPenManagerPaused(activeWindow==null);
						}
					}
				};
		ActiveWindowProperty activeWindowP=new ActiveWindowProperty(activeWindowPL); // -> registers itself with the current KeyboardFocusManager
		activeWindowPL.activeWindowChanged(activeWindowP.get());
	}

	private final PenClip penClip = new PenClip() {
				//@Override
				public void evalLocationOnScreen(Point locationOnScreen){
					// The location of this PenClip is always on (0, 0) screen coordinates.
					locationOnScreen.x=locationOnScreen.y=0;
				}
				//@Override
				public boolean contains(Point2D.Float point){
					// This PenClip covers all the screen.
					return true;
				}
			};

	//@Override
	public PenClip getPenClip() {
		return penClip;
	}

	//@Override
	public boolean isDraggingOut() {
		return false;
	}

	//@Override
	public Object evalPenEventTag(PenEvent ev){
		return null;
	}

	//@Override
	public boolean enforceSinglePenManager(){
		return false;
	}
}