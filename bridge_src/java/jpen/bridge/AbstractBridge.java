package jpen.bridge;

import java.awt.Component;
import java.util.EnumSet;

import javax.swing.SwingUtilities;

import jpen.bridge.BridgedPenManager.Bridge;

abstract class AbstractBridge implements Bridge {
	protected final BridgedPenManager bpmanager;
	protected final Component component;
	protected final BridgedPenListener listener;
	protected boolean enabled = false;
	
	/**************************
	 * CURRENT STATE
	 **************************/
	protected final EnumSet<PenButton> pressed = EnumSet.noneOf(PenButton.class);
	protected PenMode mode;
	protected float x, y, z;
	protected float tiltx, tilty;
	protected float pressure;
	/**************************
	 * END CURRENT STATE
	 **************************/
	
	/**************************
	 * PREVIOUS STATE
	 **************************/
	protected final EnumSet<PenButton> ppressed = EnumSet.noneOf(PenButton.class);
	protected PenMode pmode;
	protected float px, py, pz;
	protected float ptiltx, ptilty;
	protected float ppressure;
	/**************************
	 * END PREVIOUS STATE
	 **************************/
	
	
	protected AbstractBridge(final BridgedPenManager _bpmanager,
			final Component _component, final BridgedPenListener _listener) {
		this.bpmanager = _bpmanager;
		this.component = _component;
		this.listener = _listener;
		
		mode = pmode = bpmanager.defaultMode;
		z = pz = bpmanager.defaultZ;
		tiltx = ptiltx = bpmanager.defaultTiltx;
		tilty = ptilty = bpmanager.defaultTilty;
		pressure = ppressure = bpmanager.defaultPressure;
	}
	
	
	/**************************
	 * <code>Bridge</code> IMPLEMENTATION
	 **************************/
	
	@Override
	public Component getComponent() {
		return component;
	}
	
	@Override
	public BridgedPenListener getListener() {
		return listener;
	}
	
	@Override
	public void enable() {
		enabled = true;
	}
	
	@Override
	public void disable() {
		enabled = false;
	}
	
	/**************************
	 * END <code>Bridge</code> IMPLEMENTATION
	 **************************/
	
	
	protected void cycle() {
		cycleButtons();
		cycleMode();
		cycleLocation();
		cycleTilt();
		cyclePressure();
	}
	
	protected void cycleButtons() {
		ppressed.clear();
		ppressed.addAll(pressed);
	}
	
	protected void cycleMode() {
		pmode = mode;
	}
	
	protected void cycleLocation() {
		px = x;
		py = y;
		pz = z;
	}
	
	protected void cycleTilt() {
		ptiltx = tiltx;
		ptilty = tilty;
	}
	
	protected void cyclePressure() {
		ppressure = pressure;
	}
	
	
	protected BridgedPenEvent createEvent() {
		return createEvent(0);
	}
	
	protected BridgedPenEvent createEvent(final float sstep) {
		return new BridgedPenEvent(this,
			sstep,
			pressed, mode, x, y, z, tiltx, tilty, pressure,
			ppressed, pmode, px, py, pz, ptiltx, ptilty, ppressure
		);
	}
	
	protected void ss(final Runnable r) {
		if (bpmanager.swingSafe && !SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(r);
		}
		else {
			r.run();
		}
	}
	
	// TODO: these check enabled bits
	protected void locationChange(final float nx, final float ny, final float nz) {
		x = nx;
		y = ny;
		z = nz;
		
		if (enabled && 0 != (bpmanager.events & BridgedPenManager.LOCATION_FLAG)) {
			final BridgedPenEvent e = createEvent();
			ss(new Runnable() {public void run() {
				listener.penMoved(e);
			}});
		}
		cycleLocation();
	}
	
	protected void modeChange(final PenMode nmode) {
		mode = nmode;
		
		if (enabled && 0 != (bpmanager.events & BridgedPenManager.MODE_FLAG)) {
			final BridgedPenEvent e = createEvent();
			ss(new Runnable() {public void run() {
				listener.penModeChanged(e);
			}});
		}
		cycleMode();
	}
	
	protected void buttonChange(final PenButton button, final boolean npressed) {
		if (npressed)
			pressed.add(button);
		else
			pressed.remove(button);
		
		if (enabled && 0 != (bpmanager.events & BridgedPenManager.BUTTON_PRESS_FLAG)) {
			final BridgedPenEvent e = createEvent();
			ss(new Runnable() {public void run() {
				listener.penButtonPressed(e);
			}});
		}
		cycleButtons();
	}
	
	protected void pressureChange(final float npressure) {
		pressure = npressure;
		
		if (enabled && 0 != (bpmanager.events & BridgedPenManager.PRESSURE_FLAG)) {
			final BridgedPenEvent e = createEvent();
			ss(new Runnable() {public void run() {
				listener.penPressed(e);
			}});
		}
		cyclePressure();
	}
	
	protected void tiltChange(final float ntiltx, final float ntilty) {
		tiltx = ntiltx;
		tilty = ntilty;
		
		if (enabled && 0 != (bpmanager.events & BridgedPenManager.TILT_FLAG)) {
			final BridgedPenEvent e = createEvent();
			ss(new Runnable() {public void run() {
				listener.penTilted(e);
			}});
		}
		cycleTilt();
	}
	
	protected void scrollChange(final float step) {
		if (enabled && 0 != (bpmanager.events & BridgedPenManager.SCROLL_FLAG)) {
			final BridgedPenEvent e = createEvent();
			ss(new Runnable() {public void run() {
				listener.penScrolled(e);
			}});
		}
	}
}
