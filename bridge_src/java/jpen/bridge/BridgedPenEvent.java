package jpen.bridge;

import java.util.EventObject;


// the source object is the component
public class BridgedPenEvent extends EventObject {

	
	
	public PenBridge getBridge() {
	}
	
	// returns true if this event is from a simulated pen, e.g.
	// mouse
	public boolean isSimulated() {
	}
	
	
	public float getPressure() {
	}
	
	public float getTiltX() {
	}
	
	public float getTiltY() {
	}
	
	public float getX() {
	}
	
	public float getY() {
	}
	
	public float getZ() {
	}
	
	public PenMode getMode() {
	}
	
	public PenButton getButton() {
	}
	
	
	
	public float getPPressure() {
	}
	
	public float getPTiltX() {
	}
	
	public float getPTiltY() {
	}
	
	public float getPX() {
	}
	
	public float getPY() {
	}
	
	public float getPZ() {
	}
	
	public PenMode getPMode() {
	}
	
	public PenButton getPButton() {
	}
}
