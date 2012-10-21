/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.provider.xinput;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpen.PButton;
import jpen.PButtonEvent;
import jpen.Pen;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PKind;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.provider.AbstractPenDevice;
import jpen.provider.VirtualScreenBounds;
import jpen.PScroll;
import jpen.PScrollEvent;
import jpen.internal.ObjectUtils;
import jpen.internal.Range;
import static jpen.provider.xinput.XiDevice.*;

@SuppressWarnings("deprecation")
final class XinputDevice extends AbstractPenDevice {
	private static final Logger L=Logger.getLogger(XinputDevice.class.getName());
	//static{L.setLevel(Level.ALL);}

	private final XiDevice xiDevice;
	private final Range[] levelRanges;
	private final XinputProvider xinputProvider;
	private final Point2D.Float componentLocation=new Point2D.Float();
	private final Dimension componentSize=new Dimension();
	private final boolean isPad;
	private final Thread thread;
	private boolean isListening;

	XinputDevice(XinputProvider xinputProvider, XiDevice xiDevice) {
		super(xinputProvider);
		this.xiDevice=xiDevice;
		this.xinputProvider=xinputProvider;
		levelRanges=new Range[PLevel.Type.VALUES.size()];
		resetLevelRanges();
		isPad=getName().toLowerCase().contains(" pad");
		setKindTypeNumber(getDefaultKindTypeNumber());
		thread=new Thread("jpen-XinputDevice-"+getName()){
					 @Override
					 public void run(){
						 while(true){
							 while(!isWorking())
								 ObjectUtils.synchronizedWait(this, 0);
							 if(XinputDevice.this.xiDevice.waitNextEvent())
								 processLastEvent();
							 else {// then a call to xiDevice.stopWaitingNextEvent was made
								 //System.out.println("stopWaitingNextEvent was called");
								 synchronized(XinputDevice.this){ // try to aquire this lock to wait until the XinputDevice.this sync methods return
								 }
							 }
						 }
					 }
				 };
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setDaemon(true);
		thread.start();
		setEnabled(true);
	}

	private synchronized boolean isWorking(){
		return getIsListening() && getEnabled();
	}

	synchronized void  setIsListening(boolean isListening){
		if(this.isListening==isListening)
			return;
		this.isListening=isListening;
		xiDevice.stopWaitingNextEvent(); // xiDevice.waitNextEvent has the xiDevice sync lock. stopWaitingNextEvent force xiDevice.waitNextEvent to return and release the xiDevice lock.
		xiDevice.setIsListening(isListening); // blocks until xiDevice.waitNextEvent() returns
		synchronized(thread){
			thread.notify();
		}
	}

	private synchronized boolean getIsListening(){
		return isListening;
	}

	@Override
	public synchronized void setEnabled(boolean enabled){
		if(getEnabled()==enabled)
			return;
		super.setEnabled(enabled);
		synchronized(thread){
			thread.notify();
		}
	}

	@Override
	public synchronized boolean getEnabled(){
		return super.getEnabled();
	}


	//@Override
	public synchronized String getName() {
		xiDevice.stopWaitingNextEvent();
		return xiDevice.getName();
	}

	public synchronized boolean getIsAbsoluteMode(){
		xiDevice.stopWaitingNextEvent();
		return xiDevice.getIsAbsoluteMode();
	}

	synchronized void reset(){
		xiDevice.stopWaitingNextEvent();
		while(xiDevice.nextEvent()) // flush pending events
			;
		resetLevelRanges();
	}

	private void resetLevelRanges(){
		xiDevice.refreshLevelRanges();
		for(int i=PLevel.Type.VALUES.size(); --i>=0; ){
			PLevel.Type levelType=PLevel.Type.VALUES.get(i);
			levelRanges[levelType.ordinal()]=xiDevice.getLevelRange(levelType);
		}
	}

	private int getDefaultKindTypeNumber() {
		if(isPad)
			return PKind.Type.IGNORE.ordinal();
		String lowerCaseName=getName().toLowerCase();
		if(lowerCaseName.contains("eraser"))
			return PKind.Type.ERASER.ordinal();
		if(lowerCaseName.contains("cursor"))
			return PKind.Type.CURSOR.ordinal();
		Range pressureRange=levelRanges[PLevel.Type.PRESSURE.ordinal()];
		return pressureRange.max-pressureRange.min>1?
				 PKind.Type.STYLUS.ordinal():
				 PKind.Type.CURSOR.ordinal();
	}

	private void processLastEvent(){
		EventType eventType=xiDevice.getLastEventType();
		switch(eventType) {
			/* nicarran: TODO: support buttons?
			case BUTTON_PRESS:
				//scheduleButtonEvent(xiDevice.getLastEventButton(), true); 
				break;
			case BUTTON_RELEASE:
				//scheduleButtonEvent(xiDevice.getLastEventButton(), false);
				break;
			*/
		case MOTION_NOTIFY:
			scheduleLevelEvent();
			break;
		default:
		}
	}

	private void scheduleScrollEvent(int number) {
		getPenManager().scheduleScrollEvent(this, xiDevice.getLastEventTime(), new PScroll(number==5? PScroll.Type.DOWN.ordinal(): PScroll.Type.UP.ordinal(),1));
	}

	private final List<PLevel> changedLevels=new ArrayList<PLevel>();

	private void scheduleLevelEvent() {
		for(int i=PLevel.Type.VALUES.size(); --i>=0;) {
			PLevel.Type levelType=PLevel.Type.VALUES.get(i);
			float value=getMultRangedValue(levelType);
			changedLevels.add(new PLevel(levelType, value));
		}
		getPenManager().scheduleLevelEvent(this, xiDevice.getLastEventTime(), changedLevels, true);
		changedLevels.clear();
	}

	/* nicarran: TODO: support buttons?
	private void scheduleButtonEvent(int number, boolean state) {
		if(L.isLoggable(Level.FINE))
			L.fine("scheduling button event: number="+number+", state="+state+	", isPad="+isPad);
		PButton.Type.Group buttonTypeGroup=isPad? PButton.Type.Group.PAD:
		PButton.Type.Group.PEN;
		List<PButton.Type> types=buttonTypeGroup.getTypes();
		if(types.size()<=number){
			L.warning("Unsupported button number:"+number);
			return;
		}
		getPenManager().scheduleButtonEvent(this, xiDevice.getLastEventTime(), new PButton(types.get(number-1), state));
}
	*/

	private static final float RADS_PER_DEG=(float)(Math.PI/180);
	private static final float PI_2=(float)(2f*Math.PI);

	private final float getMultRangedValue(PLevel.Type levelType) {
		boolean isRotation=PLevel.Type.ROTATION.equals(levelType);
		if(isRotation) // rotation and wheel are given using the same xinput valuator
			levelType=PLevel.Type.SIDE_PRESSURE;
		float devValue=xiDevice.getValue(levelType);
		// nicarran: TODO: ignore rotation or SIDE_PRESSURE depending on the name of the device? wait feedback

		if(PLevel.Type.TILT_TYPES.contains(levelType))
			return devValue*RADS_PER_DEG;

		devValue=levelRanges[levelType.ordinal()].getRangedValue(devValue);

		if(isRotation)
			return devValue*PI_2;

		if(PLevel.Type.MOVEMENT_TYPES.contains(levelType))
			devValue=xinputProvider.screenBounds.getLevelRangeOffset(levelType)+
						devValue*xinputProvider.screenBounds.getLevelRangeMult(levelType);

		return devValue;
	}

	/* nicarran: experimental: support relative movements like the wintab provider?

	private boolean useFractionalMovement=true;

	@Override
	public final boolean getUseFractionalMovements(){
		return useFractionalMovement;
}

	@Override
	public void penManagerSetUseFractionalMovements(boolean useFractionalMovement){
		this.useFractionalMovement=useFractionalMovement;
}
	*/
}