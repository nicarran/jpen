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
 * The class representing a tablet event.
 */
public class TabletEvent extends AWTEvent {
    private float absoluteX;
    private float absoluteY;
    private float absoluteZ;
    private int buttonMask;
    private float pressure;
    private float rotation;
    private float tiltX;
    private float tiltY;
    private float tangentialPressure;
    private float vendorDefined1;
    private float vendorDefined2;
    private float vendorDefined3;
    
    public TabletEvent( Object source,
        int type,
        float absoluteX,
        float absoluteY,
        float absoluteZ,
        int buttonMask,
        float pressure,
        float rotation,
        float tiltX,
        float tiltY,
        float tangentialPressure,
        float vendorDefined1,
        float vendorDefined2,
        float vendorDefined3
    ) {
        super( source, type );
        this.absoluteX = absoluteX;
        this.absoluteY = absoluteY;
        this.absoluteZ = absoluteZ;
        this.buttonMask = buttonMask;
        this.pressure = pressure;
        this.rotation = rotation;
        this.tiltX = tiltX;
        this.tiltY = tiltY;
        this.tangentialPressure = tangentialPressure;
        this.vendorDefined1 = vendorDefined1;
        this.vendorDefined2 = vendorDefined2;
        this.vendorDefined3 = vendorDefined3;
    }

    public float getAbsoluteX() {
        return absoluteX;
    }

    public float getAbsoluteY() {
        return absoluteY;
    }

    public float getAbsoluteZ() {
        return absoluteZ;
    }

    public int getButtonMask() {
        return buttonMask;
    }

    public float getPressure() {
        return pressure;
    }

    public float getRotation() {
        return rotation;
    }

    public float getTiltX() {
        return tiltX;
    }

    public float getTiltY() {
        return tiltY;
    }

    public float getTangentialPressure() {
        return tangentialPressure;
    }

    public float getVendorDefined1() {
        return vendorDefined1;
    }

    public float getVendorDefined2() {
        return vendorDefined2;
    }

    public float getVendorDefined3() {
        return vendorDefined3;
    }

}
