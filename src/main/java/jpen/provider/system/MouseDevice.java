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
package jpen.provider.system;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpen.PButton;
import jpen.PButtonEvent;
import jpen.PenDevice;
import jpen.PenEvent;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.provider.AbstractPenDevice;
import jpen.PScroll;
import jpen.PScrollEvent;

@SuppressWarnings("deprecation")
public final class MouseDevice
	extends AbstractPenDevice {
	private static final Logger L=Logger.getLogger(MouseDevice.class.getName());
	//static {L.setLevel(Level.ALL);	}

	private final MouseListener mouseL=new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent ev) {
					scheduleMove(ev);
					mouseButtonChanged(ev, true);
				}

				@Override
				public void mouseReleased(MouseEvent ev) {
					scheduleMove(ev);
					mouseButtonChanged(ev, false);
				}
			};
	private final MouseMotionListener mouseMotionL=new MouseMotionListener(){
				//@Override
				public void mouseMoved(MouseEvent ev) {
					scheduleMove(ev);
				}
				//@Override
				public void mouseDragged(MouseEvent ev) {
					scheduleMove(ev);
				}
			};
	private final MouseWheelListener mouseWheelL=new MouseWheelListener(){
				//@Override
				public void mouseWheelMoved(MouseWheelEvent ev) {
					if(!getEnabled())
						return;
					int value=ev.getWheelRotation();
					PScroll.Type type=PScroll.Type.DOWN;
					if(value<0) {
						type=PScroll.Type.UP;
						value=-value;
					}
					if(ev.getScrollType()==ev.WHEEL_UNIT_SCROLL && ev.getScrollAmount()>0) // > 0 : is because windows bug workaround, sometimes it is 0.
						value*=ev.getScrollAmount();
					getPenManager().scheduleScrollEvent(MouseDevice.this, ev.getWhen(), new PScroll(type.ordinal(), value));
				}
			};
	private final SystemProvider systemProvider;
	private WeakReference<Component> activeComponentRef;

	MouseDevice(SystemProvider systemProvider) {
		super(systemProvider);
		this.systemProvider=systemProvider;
		setEnabled(true);
	}

	//@Override
	public String getName() {
		return "Mouse";
	}

	@Override
	public boolean getUseFractionalMovements(){
		return false;
	}

	void setPaused(boolean paused) {
		Component activeComponent=activeComponentRef==null? null:activeComponentRef.get();
		activeComponentRef=null;
		if(activeComponent!=null){
			activeComponent.removeMouseListener(mouseL);
			activeComponent.removeMouseMotionListener(mouseMotionL);
			activeComponent.removeMouseWheelListener(mouseWheelL);
		}
		if(!paused){
			activeComponent=systemProvider.componentPenOwner.getActiveComponent();
			if(activeComponent!=null){
				activeComponent.addMouseListener(mouseL);
				activeComponent.addMouseMotionListener(mouseMotionL);
				activeComponent.addMouseWheelListener(mouseWheelL);
				activeComponentRef=new WeakReference<Component>(activeComponent);
			}
		}
	}

	private void scheduleMove(MouseEvent ev){
		if(!getEnabled())
			return;
		scheduleMove(ev.getWhen(), ev.getX(), ev.getY());
	}

	private final PLevel[] changedLevelsA=new PLevel[2];
	private final List<PLevel> changedLevels=Arrays.asList(changedLevelsA);

	private void scheduleMove(long time, int x, int y) {
		changedLevelsA[0]=new PLevel(PLevel.Type.X.ordinal(), x);
		changedLevelsA[1]=new PLevel(PLevel.Type.Y.ordinal(), y);
		getPenManager().scheduleLevelEvent(this, time, changedLevels, false);
	}

	private void mouseButtonChanged(MouseEvent ev, boolean state) {
		if(!getEnabled())
			return;
		PButton.Type buttonType=getButtonType(ev.getButton());
		if(buttonType==null)
			return;
		if(L.isLoggable(Level.FINE))
			L.fine("scheduling button event: "+buttonType+", "+state);
		getPenManager().scheduleButtonEvent(this, ev.getWhen(), new PButton(buttonType.ordinal(), state));
	}

	private static PButton.Type getButtonType(int buttonNumber) {
		switch(buttonNumber) {
		case MouseEvent.BUTTON1:
			return PButton.Type.LEFT;
		case MouseEvent.BUTTON2:
			return PButton.Type.CENTER;
		case MouseEvent.BUTTON3:
			return PButton.Type.RIGHT;
		}
		return null;
	}
}