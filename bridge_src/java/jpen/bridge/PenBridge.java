package jpen.bridge;

import java.awt.Component;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;

public class PenBridge {
	/**************************
	 * EVENT FLAGS
	 **************************/
	public static final long LOCATION_FLAG 			= 0x01;
	public static final long TILT_FLAG 				= 0x01 << 1;
	public static final long MODE_FLAG 				= 0x01 << 2;
	public static final long PRESSED_FLAG 			= 0x01 << 3;
	public static final long BUTTON_PRESSED_FLAG 	= 0x01 << 4;
	/**************************
	 * END EVENT FLAGS
	 **************************/
	
	/**
	 * (start with all events enabled)
	 */
	private long events = ~0x00;
	
	private Map<BridgedPenListener, Bridge> bridgeMap = new HashMap<BridgedPenListener, Bridge>(4);
	
	
	private float defaultPressure		= 0.5f;
	private float defaultTiltx			= 0.f;
	private float defaultTilty			= 0.f;
	private float defaultZ				= 0.f;
	private PenMode defaultMode			= PenMode.WRITE;
	private PenButton leftButton		= PenButton.TIP;
	private PenButton rightButton		= PenButton.ERASER;
	private PenButton middleButton		= PenButton.BARREL;
	private float wheelPressureInc		= 0.f;
	
	
	public PenBridge() {
	}
	
	
	/**************************
	 * BRIDGE FACTORY
	 **************************/
	
	private Bridge createBridge(final Component c, final BridgedPenListener bpl) {
		
	}
	
	/**************************
	 * END BRIDGE FACTORY
	 **************************/
	
	
	
	public void enableEvents(final long devents) {
		events |= devents;
	}
	
	public void disableEvents(final long devents) {
		events &= ~devents;
	}
	
	
	public void addBridgedPenListener(final Component c, final BridgedPenListener bpl) {
		removeBridgedPenListener(c, bpl);
		final Bridge b = createBridge(c, bpl);
		Collection bc = bridgeMap.get(c);
		if (null == bc) {
			bridgeMap.put(c, bc = new HashSet<Bridge>(4));
		}
		bc.add(b);
		b.enable();
	}
	
	public void removeBridgedPenListener(final Component c, final BridgedPenListener bpl) {
		final Set<Bridge> bc = bridgeMap.get(c);
		if (null == bc)
			return;
		bc.remove(bpl);
		if (null == b)
			return;
		b.disable();
		b.dispose();
	}
	
	public boolean isSimulated(final BridgedPenListener bpl) {
		final Bridge b = bridgeMap.remove(c, bpl);
		return null == b ? false : b.isSimulated();
	}
	
	
	public PenBridge setLeftButton(final PenButton _leftButton) {
		leftButton = _leftButton;
		return this;
	}
	
	public PenBridge setRightButton(final PenButton _rightButton) {
		rightButton = _rightButton;
		return this;
	}
	
	public PenBridge setMiddleButton(final PenButton _middleButton) {
		middleButton = _middleButton;
		return this;
	}
	
	
	public PenBridge setWheelPressureIncrement(final float _wheelPressureInc) {
		wheelPressureInc = _wheelPressureInc;
		return this;
	}
	
	
	public PenBridge setDefaultMode(final PenMode mode) {
		defaultMode = mode;
		return this;
	}
	
	
	public PenBridge setDefaultPressure(final float pressure) {
		defaultPressure = pressure;
		return this;
	}
	
	public PenBridge setDefaultTiltX(final float tiltx) {
		defaultTiltx = tiltx;
		return this;
	}
	
	public PenBridge setDefaultTiltY(final float tilty) {
		defaultTilty = tilty;
		return this;
	}
	
	public PenBridge setDefaultZ(final float z) {
		defaultZ = z;
		return this;
	}
	
	
	
	// BRIDGE CODE
	
	
	private static interface Bridge {
		public void enable();
		public void disable();
		public void dispose();
		
		public boolean isSimulated();
		public Component getComponent();
		public BridgedPenListener getListener();
	}
	
	
	private abstract class AbstractBridge {
		
		// protected current state
		// protected new state
		
		
		// TODO: these check enabled bits
		protected void locationChange(nx, ny, nz) {
		}
		
		protected void modeChange(...) {
		}
		
		protected void buttonChange(PenButton button, boolean pressed) {
		}
		
		protected void pressureChange(np) {
		}
		
		protected void tiltedChange(ntx, nty) {
		}
		
		
		
		
	}
	
	
	private class MouseBridge extends AbstractBridge 
		implements MouseListener, MouseMotionListener, MouseWheelListener
	{
	}
	
	private class JPenBridge extends AbstractBridge
		implements PenListener
	{
	}
}
