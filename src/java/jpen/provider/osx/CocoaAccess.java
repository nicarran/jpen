package jpen.provider.osx;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.SwingUtilities;

import jpen.PButton;
import jpen.PKind;
import jpen.PLevel;



public class CocoaAccess {
	private boolean active = false;
	private CocoaProvider cocoaProvider = null;
	
	private int[] buttonMasks = {};
	private int[] pointingDeviceTypes = {};
	
	private int activePointingDeviceType = -1;
	
	
	public CocoaAccess() {
	}
	
	
	public void start() {
		if (! active) {
			System.out.println("Starting OSX Access");
			active = true;
			startup();
			
			buttonMasks = getButtonMasks();
			pointingDeviceTypes = getPointingDeviceTypes();
		}
	}
	
	public void stop() {
		if (active) {
			System.out.println("Stopping OSX Access");
			active = false;
			shutdown();
		}
	}
	
	public void dispose() {
		stop();
	}
	
	
	public void setProvider(final CocoaProvider _cocoaProvider) {
		cocoaProvider = _cocoaProvider;
	}
	
	
	/**
	 * @return
	 * The following Cocoa constants:
	 * {
	 * NSPenTipMask,
	 * NSPenLowerSideMask,
	 * NSPenUpperSideMask
	 * }
	 */
	private native int[] getButtonMasks();
	
	/**
	 * @return
	 * The following Cocoa constants:
	 * {
	 * NSUnknownPointingDevice,
	 * NSPenPointingDevice,
	 * NSCursorPointingDevice,
	 * NSEraserPointingDevice
	 * }
	 */
	private native int[] getPointingDeviceTypes();
	
	private native void startup();
    private native void shutdown();
    
    
    // IIBIIIIIIII
    private void postProximityEvent(
        final int capabilityMask,
        final int deviceID,
        final boolean enteringProximity,
        final int pointingDeviceID,
        final int pointingDeviceSerialNumber,
        final int pointingDeviceType,
        final int systemTabletID,
        final int tabletID,
        final int uniqueID,
        final int vendorID,
        final int vendorPointingDeviceType
    ) {
    	System.out.println(String.format("[postProximityEvent] device type: %d", pointingDeviceType));
    	
    	activePointingDeviceType = pointingDeviceType;
    }
    
    
    private Collection<PLevel> levels = new ArrayList<PLevel>(8);
    private Collection<PButton> buttons = new ArrayList<PButton>(8);
    /**
     * 
     * @param special_pointingDeviceType 
     * indicates whether this event came from the mouse or the tablet.
     * A value of <code>0</code> indicates the mouse;
     * a value of <code>1</code> indicates the tablet.
     * Note that proximity events are not generated when switching between the mouse and tablet.
     * 
     */
    private void postEvent(
        final int type,
		final int special_pointingDeviceType,
        float x, 
        // Note: Cocoa gives the y-coordinate inverted, i.e. "opengl coordinates"
        float y,
        final int absoluteX, final int absoluteY,  final int absoluteZ,
        final int buttonMask,
        final float pressure, final float rotation,
        final float tiltX, final float tiltY,
        final float tangentialPressure,
        final float vendorDefined1,
        final float vendorDefined2,
        final float vendorDefined3
    ) {
		System.out.println(String.format("[postEvent] device type: %d; %d; %d", special_pointingDeviceType, type, buttonMask));
		
		// TODO: cycle buttons
		// TODO: 
		
		/*
        final int awt_type;
        switch ( type ) {
        default:
        case 1: // NSLeftMouseDown
            awt_type = MouseEvent.MOUSE_PRESSED;
            break;
        case 2: // NSLeftMouseUp
            awt_type = MouseEvent.MOUSE_RELEASED;
            break;
        case 5: // NSLeftMouseMoved
            awt_type = MouseEvent.MOUSE_MOVED;
            break;
        case 6: // NSLeftMouseDragged
            awt_type = MouseEvent.MOUSE_DRAGGED;
            break;
        }
        */
		
		final Component c = cocoaProvider.getPenManager().component;
		final Window w = SwingUtilities.getWindowAncestor(c);
		assert w == KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
		if (null != w) {
			y = w.getHeight() - y;
		}
		
		final Point origin = new Point(0, 0);
		SwingUtilities.convertPoint(c, origin, w);
		
		x += origin.x;
		y += origin.y;
		
		
		final CocoaDevice device;
		if (0 == special_pointingDeviceType || 2 == special_pointingDeviceType) {
			System.out.println("::CURSOR");
			device = cocoaProvider.getDevice(PKind.Type.CURSOR);
		}
		else
			if (pointingDeviceTypes[1] == activePointingDeviceType) {
				System.out.println("::STYLUS");
				device = cocoaProvider.getDevice(PKind.Type.STYLUS);
			}
			else if (pointingDeviceTypes[3] == activePointingDeviceType) {
				System.out.println("::ERASER");
				device = cocoaProvider.getDevice(PKind.Type.ERASER);
			}
			else {
				System.out.println("::BAD::STYLUS");
				assert false;
				device = cocoaProvider.getDevice(PKind.Type.STYLUS);
			}
		
		
		levels.clear();
		levels.add(new PLevel(PLevel.Type.X.ordinal(), x));
		levels.add(new PLevel(PLevel.Type.Y.ordinal(), y));
		levels.add(new PLevel(PLevel.Type.TILT_X.ordinal(), tiltX));
		levels.add(new PLevel(PLevel.Type.TILT_Y.ordinal(), tiltY));
		levels.add(new PLevel(PLevel.Type.PRESSURE.ordinal(), pressure));
		
		cocoaProvider.getPenManager().scheduleLevelEvent(device, levels);

		
		
		buttons.clear();
		
		if (2 == special_pointingDeviceType) {
			buttons.add(new PButton(PButton.Type.LEFT.ordinal(), false));
			buttons.add(new PButton(PButton.Type.CENTER.ordinal(), false));
			buttons.add(new PButton(PButton.Type.RIGHT.ordinal(), false));
		}
		else if (0 == special_pointingDeviceType) {
			// 0: left
			// 1: right
			// 2: middle
			buttons.add(new PButton(PButton.Type.LEFT.ordinal(), 0 == buttonMask));
			buttons.add(new PButton(PButton.Type.CENTER.ordinal(), 2 == buttonMask));
			buttons.add(new PButton(PButton.Type.RIGHT.ordinal(), 1 == buttonMask));
		}
		else {
			// Consult the button masks
			// TODO: is the barrel button handled correctly?
			buttons.add(new PButton(PButton.Type.LEFT.ordinal(), 0 != (buttonMask & buttonMasks[0])));
			buttons.add(new PButton(PButton.Type.CENTER.ordinal(), 0 != (buttonMask & buttonMasks[1])));
			buttons.add(new PButton(PButton.Type.RIGHT.ordinal(), 0 != (buttonMask & buttonMasks[2])));
		}
		
		for (PButton button : buttons) {
			cocoaProvider.getPenManager().scheduleButtonEvent(button);
		}
		
		levels.clear();
		buttons.clear();
    }
}
