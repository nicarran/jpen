package jpen.bridge;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public final class MouseBridge extends AbstractBridge 
implements MouseListener, MouseMotionListener, MouseWheelListener
{
public MouseBridge(final BridgedPenManager _bpmanager,
		final Component _component, final BridgedPenListener _listener) {
	super(_bpmanager, _component, _listener);
	
	init();
}

private void init() {
	component.addMouseListener(this);
	component.addMouseMotionListener(this);
	component.addMouseWheelListener(this);
}


/**************************
 * <code>Bridge</code> IMPLEMENTATION
 **************************/

@Override
public boolean isSimulated() {
	return true;
}

@Override
public void dispose() {
	component.removeMouseListener(this);
	component.removeMouseMotionListener(this);
	component.removeMouseWheelListener(this);
}

/**************************
 * END <code>Bridge</code> IMPLEMENTATION
 **************************/


// MOUSE HOOKS

@Override
public void mouseWheelMoved(MouseWheelEvent e) {
	switch (bpmanager.wheelMode) {
		case PRESSURE:
			if (0 != bpmanager.wheelPressureMult) {
				pressureChange(pressure + 
						bpmanager.wheelPressureMult * e.getScrollAmount());
			}
			break;
		case SCROLL:
			if (0 != bpmanager.wheelScrollMult) {
				scrollChange(bpmanager.wheelScrollMult * e.getScrollAmount());
			}
			break;
	}
}

@Override
public void mouseDragged(MouseEvent e) {
	locationChange(e.getX(), e.getY(), bpmanager.defaultZ);
}

@Override
public void mouseMoved(MouseEvent e) {
	locationChange(e.getX(), e.getY(), bpmanager.defaultZ);
}

@Override
public void mouseClicked(MouseEvent e) {
	// Do nothing
}

@Override
public void mouseEntered(MouseEvent e) {
	// Do nothing
}

@Override
public void mouseExited(MouseEvent e) {
	// Do nothing
}

@Override
public void mousePressed(MouseEvent e) {
	final PenButton button;
	switch (e.getButton()) {
		case MouseEvent.BUTTON1: button = PenButton.TIP; break;
		case MouseEvent.BUTTON2: button = PenButton.BARREL; break;
		case MouseEvent.BUTTON3: button = PenButton.ERASER; break;
		default: button = PenButton.TIP; break;
	}
	buttonChange(button, true);
}


@Override
public void mouseReleased(MouseEvent e) {
	final PenButton button;
	switch (e.getButton()) {
		case MouseEvent.BUTTON1: button = bpmanager.leftButton; break;
		case MouseEvent.BUTTON2: button = bpmanager.middleButton; break;
		case MouseEvent.BUTTON3: button = bpmanager.rightButton; break;
		default: button = PenButton.TIP; break;
	}
	buttonChange(button, false);
}
}
