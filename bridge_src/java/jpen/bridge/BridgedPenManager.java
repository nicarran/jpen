package jpen.bridge;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class BridgedPenManager {
	public static enum WheelMode {
		SCROLL,
		PRESSURE
	}
	
	public static interface Bridge {
		public void enable();
		public void disable();
		public void dispose();
		
		public boolean isSimulated();
		public Component getComponent();
		public BridgedPenListener getListener();
	}
	
	
	/**************************
	 * EVENT FLAGS
	 **************************/
	public static final long LOCATION_FLAG 			= 0x01;
	public static final long TILT_FLAG 				= 0x01 << 1;
	public static final long MODE_FLAG 				= 0x01 << 2;
	public static final long PRESSURE_FLAG 			= 0x01 << 3;
	public static final long BUTTON_PRESS_FLAG 		= 0x01 << 4;
	public static final long SCROLL_FLAG 			= 0x01 << 5;
	/**************************
	 * END EVENT FLAGS
	 **************************/
	
	/**
	 * (start with all events enabled)
	 */
	protected long events = ~0x00;
	
	private Map<BridgedPenListener, Bridge> bridgeMap = new HashMap<BridgedPenListener, Bridge>(4);
	
	protected boolean swingSafe = true;
	
	protected float defaultPressure		= 0.5f;
	protected float defaultTiltx			= 0.f;
	protected float defaultTilty			= 0.f;
	protected float defaultZ				= 0.f;
	protected PenMode defaultMode			= PenMode.WRITE;
	protected PenButton leftButton		= PenButton.TIP;
	protected PenButton rightButton		= PenButton.ERASER;
	protected PenButton middleButton		= PenButton.BARREL;
	protected WheelMode wheelMode			= WheelMode.SCROLL;
	protected float wheelScrollMult		= 1.f;
	protected float wheelPressureMult		= 1.f;
	
	
	public BridgedPenManager() {
	}
	
	
	public void dispose() {
		final Bridge[] bridges = (Bridge[]) bridgeMap.values().toArray();
		for (Bridge bridge : bridges) {
			removeBridgedPenListener(bridge.getComponent(), bridge.getListener());
		}
	}
	
	
	/**************************
	 * BRIDGE FACTORY
	 **************************/
	
	private boolean isJPenInstalled() {
		// Attempt to detect <code>PenManager</code>
		try {
			return null != Class.forName("jpen.PenManager");
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	private Bridge createBridge(final Component c, final BridgedPenListener bpl) {
		if (isJPenInstalled()) {
//			new JPenBridge(this, c, bpl)
			try {
			final Class<? extends Bridge> clazz = (Class<? extends Bridge>) Class.forName("jpen.bridge.JPenBridge");
			if (null != clazz) {
				return clazz.getConstructor(
						BridgedPenManager.class,
						Component.class,
						BridgedPenListener.class
				).newInstance(this, c, bpl);
			}
			}
			// Fall through on all exceptions
			catch (ClassNotFoundException e) {
			}
			catch (InvocationTargetException e) {
			}
			catch (InstantiationException e) {
			}
			catch (IllegalAccessException e) {
			}
			catch (NoSuchMethodException e) {
			}
		}
		return new MouseBridge(this, c, bpl);
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
		final Bridge oldb = bridgeMap.put(bpl, b);
		assert null == oldb;
		b.enable();
	}
	
	public void removeBridgedPenListener(final Component c, final BridgedPenListener bpl) {
		final Bridge b = bridgeMap.remove(bpl);
		if (null == b)
			return;
		b.disable();
		b.dispose();
	}
	
	public boolean isSimulated(final BridgedPenListener bpl) {
		final Bridge b = bridgeMap.get(bpl);
		return null == b ? false : b.isSimulated();
	}
	
	
	public BridgedPenManager setLeftButton(final PenButton _leftButton) {
		leftButton = _leftButton;
		return this;
	}
	
	public BridgedPenManager setRightButton(final PenButton _rightButton) {
		rightButton = _rightButton;
		return this;
	}
	
	public BridgedPenManager setMiddleButton(final PenButton _middleButton) {
		middleButton = _middleButton;
		return this;
	}
	
	
	public BridgedPenManager setWheelMode(final WheelMode _wheelMode) {
		wheelMode = _wheelMode;
		return this;
	}
	
	public BridgedPenManager setWheelScrollMultiplier(final float _wheelScrollMult) {
		wheelScrollMult = _wheelScrollMult;
		return this;
	}
	
	public BridgedPenManager setWheelPressureMultiplier(final float _wheelPressureMult) {
		wheelPressureMult = _wheelPressureMult;
		return this;
	}
	
	
	public BridgedPenManager setDefaultMode(final PenMode mode) {
		defaultMode = mode;
		return this;
	}
	
	
	public BridgedPenManager setDefaultPressure(final float pressure) {
		defaultPressure = pressure;
		return this;
	}
	
	public BridgedPenManager setDefaultTiltX(final float tiltx) {
		defaultTiltx = tiltx;
		return this;
	}
	
	public BridgedPenManager setDefaultTiltY(final float tilty) {
		defaultTilty = tilty;
		return this;
	}
	
	public BridgedPenManager setDefaultZ(final float z) {
		defaultZ = z;
		return this;
	}
}
