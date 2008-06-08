package jpen.bridge;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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
	private WheelMode wheelMode			= WheelMode.SCROLL;
	private float wheelScrollMult		= 1.f;
	private float wheelPressureMult		= 1.f;
	
	
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
		return isJPenInstalled()
			? new JPenBridge(c, bpl)
			: new MouseBridge(c, bpl);
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
	
	
	
	// BRIDGE CODE
	
	
	
	
	
	private abstract class AbstractBridge implements Bridge {
		protected final Component component;
		protected final BridgedPenListener listener;
		protected boolean enabled = false;
		
		/**************************
		 * CURRENT STATE
		 **************************/
		protected final EnumSet<PenButton> pressed = EnumSet.noneOf(PenButton.class);
		protected PenMode mode = defaultMode;
		protected float x, y, z = defaultZ;
		protected float tiltx = defaultTiltx, tilty = defaultTilty;
		protected float pressure = defaultPressure;
		/**************************
		 * END CURRENT STATE
		 **************************/
		
		/**************************
		 * PREVIOUS STATE
		 **************************/
		protected final EnumSet<PenButton> ppressed = EnumSet.noneOf(PenButton.class);
		protected PenMode pmode = defaultMode;
		protected float px, py, pz = defaultZ;
		protected float ptiltx = defaultTiltx, ptilty = defaultTilty;
		protected float ppressure = defaultPressure;
		/**************************
		 * END PREVIOUS STATE
		 **************************/
		
		
		protected AbstractBridge(final Component _component, final BridgedPenListener _listener) {
			this.component = _component;
			this.listener = _listener;
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
		
		
		// TODO: these check enabled bits
		protected void locationChange(final float nx, final float ny, final float nz) {
			x = nx;
			y = ny;
			z = nz;
			
			if (enabled && 0 != (events & LOCATION_FLAG)) {
				listener.penMoved(createEvent());
			}
			cycleLocation();
		}
		
		protected void modeChange(final PenMode nmode) {
			mode = nmode;
			
			if (enabled && 0 != (events & MODE_FLAG)) {
				listener.penModeChanged(createEvent());
			}
			cycleMode();
		}
		
		protected void buttonChange(final PenButton button, final boolean npressed) {
			if (npressed)
				pressed.add(button);
			else
				pressed.remove(button);
			
			if (enabled && 0 != (events & BUTTON_PRESS_FLAG)) {
				listener.penModeChanged(createEvent());
			}
			cycleButtons();
		}
		
		protected void pressureChange(final float npressure) {
			pressure = npressure;
			
			if (enabled && 0 != (events & PRESSURE_FLAG)) {
				listener.penPressed(createEvent());
			}
			cyclePressure();
		}
		
		protected void tiltChange(final float ntiltx, final float ntilty) {
			tiltx = ntiltx;
			tilty = ntilty;
			
			if (enabled && 0 != (events & TILT_FLAG)) {
				listener.penPressed(createEvent());
			}
			cycleTilt();
		}
		
		protected void scrollChange(final float step) {
			if (enabled && 0 != (events & SCROLL_FLAG)) {
				listener.penScrolled(createEvent(step));
			}
		}
	}
	
	
	
	
	private final class MouseBridge extends AbstractBridge 
		implements MouseListener, MouseMotionListener, MouseWheelListener
	{
		public MouseBridge(final Component _component, final BridgedPenListener _listener) {
			super(_component, _listener);
			
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
			switch (wheelMode) {
				case PRESSURE:
					if (0 != wheelPressureMult) {
						pressureChange(pressure + 
								wheelPressureMult * e.getScrollAmount());
					}
					break;
				case SCROLL:
					if (0 != wheelScrollMult) {
						scrollChange(wheelScrollMult * e.getScrollAmount());
					}
					break;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// Do nothing
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			locationChange(e.getX(), e.getY(), defaultZ);
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
				case MouseEvent.BUTTON1: button = leftButton; break;
				case MouseEvent.BUTTON2: button = middleButton; break;
				case MouseEvent.BUTTON3: button = rightButton; break;
				default: button = PenButton.TIP; break;
			}
			buttonChange(button, false);
		}
	}
	
	private final class JPenBridge extends AbstractBridge
		implements PenListener
	{
		private final PenManager pmanager;
		
		public JPenBridge(final Component _component, final BridgedPenListener _listener) {
			super(_component, _listener);
			
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
//				else if (PLevel.Type.Z == type)
//					z = level.value;
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
}
