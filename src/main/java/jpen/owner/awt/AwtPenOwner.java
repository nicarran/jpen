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
import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.SwingUtilities;
import jpen.owner.AbstractPenOwner;
import jpen.owner.PenClip;
import jpen.PenProvider;
import jpen.internal.ActiveWindowProperty;

/**
<b>Warning:</b> This class is not suitable for use on multiple {@link java.awt.components}, use {@link jpen.owner.multiAwt.AwtPenToolkit} instead. When using multiple instances of this class, the following problems will arise:
<ul>
<li>
Button and scroll events actioned before moving the pointer on the component (see http://sourceforge.net/p/jpen/discussion/753961/thread/71cb3b82/ ) will be lost.
</li>
<li>
The Mac OS X (osx) provider will go berserk.
</li>
</ul>

@deprecated use {@link jpen.owner.multiAwt.AwtPenToolkit}
*/
@Deprecated
public final class AwtPenOwner
	extends ComponentPenOwner {

	private static int instanceCount=0;

	private synchronized static  void increaseInstanceCount() {
		instanceCount++;
	}
	
	private synchronized static int getInstanceCount(){
		return instanceCount;
	}

	public final Component component;
	private final MouseListener mouseListener=new MouseAdapter() {
		@Override
		public void mouseExited(MouseEvent ev) {
			synchronized(getPenSchedulerLock(ev.getComponent())) {
				if(!startDraggingOut())
					pause();
			}
		}

		@Override
		public void mouseEntered(MouseEvent ev) {
			synchronized(getPenSchedulerLock(ev.getComponent())) {
				if(!stopDraggingOut()) {
					if(getInstanceCount()>1)
						unpauser.enable(); // unpauses when mouse motion is detected
					else
						unpause();
				}
			}
		}
	};

	public AwtPenOwner(Component component) {
		increaseInstanceCount();
		this.component=component;
	}

	@Override
	public Component getActiveComponent() {
		return component;
	}

	@Override
	protected void init() {
		component.addMouseListener(mouseListener);
	}
}