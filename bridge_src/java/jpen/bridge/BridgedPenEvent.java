package jpen.bridge;

import java.util.EnumSet;
import java.util.EventObject;

import jpen.bridge.PenBridgeManager.Bridge;


// the source object is the component
public final class BridgedPenEvent extends EventObject {
	private final Bridge bridge;
	
	private final float sstep;
	
	private final EnumSet<PenButton> pressed;
	private final PenMode mode;
	private final float x;
	private final float y;
	private final float z;
	private final float tiltx;
	private final float tilty;
	private final float pressure;
	
	private final EnumSet<PenButton> ppressed;
	private final PenMode pmode;
	private final float px;
	private final float py;
	private final float pz;
	private final float ptiltx;
	private final float ptilty;
	private final float ppressure;
	
	
	public BridgedPenEvent(
		final Bridge _bridge,
		
		final float _sstep,
		
		final EnumSet<PenButton> _pressed,
		final PenMode _mode,
		final float _x,
		final float _y,
		final float _z,
		final float _tiltx,
		final float _tilty,
		final float _pressure,
		
		final EnumSet<PenButton> _ppressed,
		final PenMode _pmode,
		final float _px,
		final float _py,
		final float _pz,
		final float _ptiltx,
		final float _ptilty,
		final float _ppressure
	) {
		super(_bridge.getComponent());
		
		this.bridge = _bridge;
		
		this.sstep = _sstep;
		
		this.pressed = _pressed;
		this.mode = _mode;
		this.x = _x;
		this.y = _y;
		this.z = _z;
		this.tiltx = _tiltx;
		this.tilty = _tilty;
		this.pressure = _pressure;
		
		this.ppressed = _ppressed;
		this.pmode = _pmode;
		this.px = _px;
		this.py = _py;
		this.pz = _pz;
		this.ptiltx = _ptiltx;
		this.ptilty = _ptilty;
		this.ppressure = _ppressure;
	}
	
	
	public Bridge getBridge() {
		return bridge;
	}
	
	// returns true if this event is from a simulated pen, e.g.
	// mouse
	public boolean isSimulated() {
		return bridge.isSimulated();
	}
	
	
	public float getScrollStep() {
		return sstep;
	}
	
	public float getPressure() {
		return pressure;
	}
	
	public float getTiltX() {
		return tiltx;
	}
	
	public float getTiltY() {
		return tilty;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getZ() {
		return z;
	}
	
	public PenMode getMode() {
		return mode;
	}
	
	public EnumSet<PenButton> getButtons() {
		return pressed;
	}
	
	
	
	public float getPPressure() {
		return ppressure;
	}
	
	public float getPTiltX() {
		return ptiltx;
	}
	
	public float getPTiltY() {
		return ptilty;
	}
	
	public float getPX() {
		return px;
	}
	
	public float getPY() {
		return py;
	}
	
	public float getPZ() {
		return pz;
	}
	
	public PenMode getPMode() {
		return pmode;
	}
	
	public EnumSet<PenButton> getPButtons() {
		return ppressed;
	}
}
