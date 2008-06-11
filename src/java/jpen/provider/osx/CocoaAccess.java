package jpen.provider.osx;

import javax.swing.JFrame;

import jpen.provider.Utils;


public class CocoaAccess {
	
	public CocoaAccess() {
		startup();
	}
	
	
	public void dispose() {
		shutdown();
	}
	
	
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
    }
    
    private void postEvent(
        final int type,
		final int pointingDeviceType,
        final float x, final float y,
        final int absoluteX, final int absoluteY,  final int absoluteZ,
        final int buttonMask,
        final float pressure, final float rotation,
        final float tiltX, final float tiltY,
        final float tangentialPressure,
        final float vendorDefined1,
        final float vendorDefined2,
        final float vendorDefined3
    ) {
		System.out.println(String.format("[postEvent] device type: %d", pointingDeviceType));
		
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
        
    }
    
    
    
    
    public static void main(final String[] in) {
    	Utils.loadLibrary();
    	
    	new CocoaAccess();
    	
    	new JFrame().setVisible(true);
    }
}
