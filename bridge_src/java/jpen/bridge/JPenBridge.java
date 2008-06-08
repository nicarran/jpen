package jpen.bridge;

import java.awt.Component;

import jpen.PButton;
import jpen.PButtonEvent;
import jpen.PKind;
import jpen.PKindEvent;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PScroll;
import jpen.PScrollEvent;
import jpen.PenManager;
import jpen.event.PenListener;

public final class JPenBridge extends AbstractBridge
implements PenListener
{
private final PenManager pmanager;

public JPenBridge(final BridgedPenManager _bpmanager,
		final Component _component, final BridgedPenListener _listener) {
	super(_bpmanager, _component, _listener);
	
	pmanager = new PenManager(component);
	
	init();
}

private void init() {
	pmanager.pen.addListener(this);
}


/**************************
 * <code>Bridge</code> IMPLEMENTATION
 **************************/

@Override
public boolean isSimulated() {
	return false;
}

@Override
public void dispose() {
	pmanager.pen.removeListener(this);
}

/**************************
 * END <code>Bridge</code> IMPLEMENTATION
 **************************/


// PEN HOOKS

@Override
public void penButtonEvent(PButtonEvent ev) {
	final PenButton button;
	final PButton.Type type = ev.button.getType();
	if (PButton.Type.LEFT == type)
		button = PenButton.TIP;
	else if (PButton.Type.CENTER == type)
		button = PenButton.BARREL;
	else if (PButton.Type.RIGHT == type)
		button = PenButton.ERASER;
	else
		button = PenButton.TIP;
	buttonChange(button, ev.button.value);
}

@Override
public void penKindEvent(PKindEvent ev) {
	final PenMode mode;
	final PKind.Type type = ev.kind.getType();
	if (PKind.Type.CURSOR == type)
		mode = PenMode.CURSOR;
	else if (PKind.Type.STYLUS == type)
		mode = PenMode.WRITE;
	else if (PKind.Type.ERASER == type)
		mode = PenMode.ERASE;
	else
		mode = PenMode.WRITE;
	modeChange(mode);
}

@Override
public void penLevelEvent(PLevelEvent ev) {
	boolean mlocation = false;
	boolean mtilt = false;
	boolean mpressure = false;
	
	float npressure = pressure;
	float nx = x;
	float ny = y;
	float nz = z;
	float ntiltx = tiltx;
	float ntilty = tilty;
	
	for (PLevel level : ev.levels) {
		final PLevel.Type type = level.getType();
		if (PLevel.Type.PRESSURE == type) {
			npressure = level.value;
			mpressure = true;
		}
		else if (PLevel.Type.X == type) {
			nx = level.value;
			mlocation = true;
		}
		else if (PLevel.Type.Y == type) {
			ny = level.value;
			mlocation = true;
		}
//		else if (PLevel.Type.Z == type)
//			z = level.value;
		else if (PLevel.Type.TILT_X == type) {
			ntiltx = level.value;
			mtilt = true;
		}
		else if (PLevel.Type.TILT_Y == type) {
			ntilty = level.value;
			mtilt = true;
		}
	}
	
	if (mpressure)
		pressureChange(npressure);
	if (mlocation)
		locationChange(nx, ny, nz);
	if (mtilt)
		tiltChange(ntiltx, ntilty);
}

@Override
public void penScrollEvent(PScrollEvent ev) {
	int step = Math.abs(ev.scroll.value);
	if (PScroll.Type.UP == ev.scroll.getType())
		step = -step;
	scrollChange(step);
}

@Override
public void penTock(long availableMillis) {
	// Do nothing
}
}
