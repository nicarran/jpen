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

/**
 * The class representing a tablet proximity event.
 */

public class TabletProximityEvent extends AWTEvent {
    private int capabilityMask;
    private int deviceID;
    private boolean enteringProximity;
    private int pointingDeviceID;
    private int pointingDeviceSerialNumber;
    private int pointingDeviceType;
    private int systemTabletID;
    private int tabletID;
    private int uniqueID;
    private int vendorID;
    private int vendorPointingDeviceType;

    public TabletProximityEvent( Object source,
        int capabilityMask,
        int deviceID,
        boolean enteringProximity,
        int pointingDeviceID,
        int pointingDeviceSerialNumber,
        int pointingDeviceType,
        int systemTabletID,
        int tabletID,
        int uniqueID,
        int vendorID,
        int vendorPointingDeviceType
    ) {
        super( source, 0 );
        this.capabilityMask = capabilityMask;
        this.deviceID = deviceID;
        this.enteringProximity = enteringProximity;
        this.pointingDeviceID = pointingDeviceID;
        this.pointingDeviceSerialNumber = pointingDeviceSerialNumber;
        this.pointingDeviceType = pointingDeviceType;
        this.systemTabletID = systemTabletID;
        this.tabletID = tabletID;
        this.uniqueID = uniqueID;
        this.vendorID = vendorID;
        this.vendorPointingDeviceType = vendorPointingDeviceType;
    }

    public int getCapabilityMask() {
        return capabilityMask;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public boolean isEnteringProximity() {
        return enteringProximity;
    }

    public int getPointingDeviceID() {
        return pointingDeviceID;
    }

    public int getPointingDeviceSerialNumber() {
        return pointingDeviceSerialNumber;
    }

    public int getPointingDeviceType() {
        return pointingDeviceType;
    }

    public int getSystemTabletID() {
        return systemTabletID;
    }

    public int getTabletID() {
        return tabletID;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public int getVendorID() {
        return vendorID;
    }

    public int getVendorPointingDeviceType() {
        return vendorPointingDeviceType;
    }


}
