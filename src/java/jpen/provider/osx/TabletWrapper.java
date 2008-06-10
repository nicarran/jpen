/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * The class which wraps the tablet interface. To use this, create an instance and then add tabletListeners to it.
 */
public class TabletWrapper {

    static {
        System.loadLibrary("JNITablet");
    }

    private static TabletWrapper instance;

    private Vector listeners = new Vector();

    public TabletWrapper() {
        if ( instance == null ) {
            instance = this;
            startup();
        }
    }

    public void addTabletListener( TabletListener listener ) {
        listeners.add( listener );
    }

    public void removeTabletListener( TabletListener listener ) {
        listeners.remove( listener );
    }

    public void finalize() {
        shutdown();
    }
    
    private native void startup();
    private native void shutdown();

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
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    TabletProximityEvent event = new TabletProximityEvent( this, 
                        capabilityMask, deviceID, enteringProximity,
                        pointingDeviceID, pointingDeviceSerialNumber, pointingDeviceType,
                        systemTabletID, tabletID, uniqueID, vendorID, vendorPointingDeviceType
                    );
                    for ( Iterator it = listeners.iterator(); it.hasNext(); ) {
                        TabletListener listener = (TabletListener)it.next();
                        listener.tabletProximity( event );
                    }
                }
            }
        );
    }
    
    private void postEvent(
        final int type,
		final int deviceType,
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
		System.out.println(String.format("device type: %d", deviceType));
		
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
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    TabletEvent event = new TabletEvent( this,
                            awt_type,
                            absoluteX, absoluteY, absoluteZ,
                            buttonMask,
                            pressure, rotation,
                            tiltX, tiltY,
                            tangentialPressure,
                            vendorDefined1, vendorDefined2, vendorDefined3
                    );
                    for ( Iterator it = listeners.iterator(); it.hasNext(); ) {
                        TabletListener listener = (TabletListener)it.next();
                        listener.tabletEvent( event );
                    }
                }
            }
        );
    }

}
