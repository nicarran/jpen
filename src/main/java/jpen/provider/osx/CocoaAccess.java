/* [{
Copyright 2008 Brien Colwell

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.provider.osx;

import java.awt.Component;
import java.awt.Insets;
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
	private static final float HALF_PI = (float) (Math.PI / 2);


	private boolean active = false;
	private CocoaProvider cocoaProvider = null;

	private int[] buttonMasks = {};
	private int[] pointingDeviceTypes = {};

	private int activePointingDeviceType = -1;


	public CocoaAccess() {
	}


	public void start() {
		if (! active) {
			active = true;
			startup();

			buttonMasks = getButtonMasks();
			pointingDeviceTypes = getPointingDeviceTypes();
		}
	}

	public void stop() {
		if (active) {
			active = false;
			shutdown();
		}
	}

	public void dispose() {
		stop();
	}


	public native void enable();
	public native void disable();


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
		//    	System.out.println(String.format("[postProximityEvent] device type: %d", pointingDeviceType));

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
	  final float x,
	  // Note: Cocoa gives the y-coordinate inverted, i.e. "opengl coordinates"
	  final float y,
	  final int absoluteX, final int absoluteY,  final int absoluteZ,
	  final int buttonMask,
	  final float pressure, final float rotation,
	  final float tiltX, final float tiltY,
	  final float tangentialPressure,
	  final float vendorDefined1,
	  final float vendorDefined2,
	  final float vendorDefined3
	) {
		if(special_pointingDeviceType!=1) // nicarran: EXPERIMENTAL: possible "pressure jump" bug workaround.
			return;
		
		// nicarran: EXPERIMENTAL: no need to use the event dispatcher thread.
		postTabletEvent(
			  type, special_pointingDeviceType,
			  x, y, absoluteX, absoluteY, absoluteZ,
			  buttonMask, pressure, rotation,
			  tiltX, tiltY,
			  tangentialPressure,
			  vendorDefined1, vendorDefined2, vendorDefined3
			);
		/*
		if (SwingUtilities.isEventDispatchThread()) {
			postEvent_swing(
			  type, special_pointingDeviceType,
			  x, y, absoluteX, absoluteY, absoluteZ,
			  buttonMask, pressure, rotation,
			  tiltX, tiltY,
			  tangentialPressure,
			  vendorDefined1, vendorDefined2, vendorDefined3
			);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {public void run() {
					    postEvent_swing(
					      type, special_pointingDeviceType,
					      x, y, absoluteX, absoluteY, absoluteZ,
					      buttonMask, pressure, rotation,
					      tiltX, tiltY,
					      tangentialPressure,
					      vendorDefined1, vendorDefined2, vendorDefined3
					    );
				    }});
		}*/
	}

	private void postTabletEvent(
	  final int type,
	  final int special_pointingDeviceType,
	  float x,
	  // Note: Cocoa gives the y-coordinate inverted, i.e. "opengl coordinates"
	  float y,
	  final int absoluteX, final int absoluteY,  final int absoluteZ,
	  final int buttonMask,
	  final float pressure, final float rotation,
	  float tiltX, float tiltY,
	  final float tangentialPressure,
	  final float vendorDefined1,
	  final float vendorDefined2,
	  final float vendorDefined3
	) {
		//		System.out.println(String.format("[postEvent] device type: %d; %d; %d", special_pointingDeviceType, type, buttonMask));

		final Component c = cocoaProvider.getPenManager().component;
		//		final Component r = SwingUtilities.getRoot(c);
		final Window w = SwingUtilities.getWindowAncestor(c);
		assert w == KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
		if (null != w) {
			final Insets insets = w.getInsets();
			y = -y + (w.getHeight() - insets.bottom);
			x = x - insets.left;
		}

		Point origin = new Point(0, 0);
		origin = SwingUtilities.convertPoint(w, origin, c);

		x += origin.x;
		y += origin.y;

		// JPen expects tilt to be -pi/2 to pi/2 from vertical;
		// Cocoa delivers tilt as -1 to 1 from vertical
		tiltX *= HALF_PI;
		tiltY *= HALF_PI;

		final CocoaDevice device;
		if (0 == special_pointingDeviceType || 2 == special_pointingDeviceType) {
			device = cocoaProvider.getDevice(PKind.Type.CURSOR);
		}
		else if (pointingDeviceTypes[1] == activePointingDeviceType) {
			device = cocoaProvider.getDevice(PKind.Type.STYLUS);
		}
		else if (pointingDeviceTypes[3] == activePointingDeviceType) {
			device = cocoaProvider.getDevice(PKind.Type.ERASER);
		}
		else {
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
