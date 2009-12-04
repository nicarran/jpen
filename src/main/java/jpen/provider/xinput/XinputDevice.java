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
import static jpen.provider.xinput.XiDevice.*;

final class XinputDevice extends AbstractPenDevice {
	private static final Logger L=Logger.getLogger(XinputDevice.class.getName());
	//static{L.setLevel(Level.ALL);}

	private final XiDevice xiDevice;
	private final PLevel.Range[] levelRanges;
	private final XinputProvider xinputProvider;
	private final Point2D.Float componentLocation=new Point2D.Float();
	private final Dimension componentSize=new Dimension();
	private final Thread thread;
	private boolean isListening;

	XinputDevice(XinputProvider xinputProvider, XiDevice xiDevice) {
		super(xinputProvider);
		this.xiDevice=xiDevice;
		this.xinputProvider=xinputProvider;
		levelRanges=new PLevel.Range[PLevel.Type.VALUES.size()];
		resetLevelRanges();
		setKindTypeNumber(getDefaultKindTypeNumber());
		thread=new Thread("jpen-XinputDevice-"+getName()){
						 @Override
						 public void run(){
							 while(true){
								 while(!isWorking())
									 jpen.Utils.synchronizedWait(this, 0);
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

	//@Override
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
		String lowerCaseName=getName().toLowerCase();
		if(lowerCaseName.indexOf("eraser")!=-1)
			return PKind.Type.ERASER.ordinal();
		else if(lowerCaseName.indexOf("cursor")!=-1)
			return PKind.Type.CURSOR.ordinal();
		else if(lowerCaseName.indexOf("pad")!=-1)
			return PKind.Type.IGNORE.ordinal();
		else
			return PKind.Type.STYLUS.ordinal();
	}

	private void processLastEvent(){
		//L.fine("processing last event "+System.currentTimeMillis());
		EventType eventType=xiDevice.getLastEventType();
		switch(eventType) {
		case BUTTON_PRESS:
			int lastEventButton=xiDevice.getLastEventButton();
			if( lastEventButton ==4 || lastEventButton ==5 ){
				//scheduleScrollEvent(lastEventButton); nicarran: the mouse provider catches this.
			}
			else
				scheduleButtonEvent(lastEventButton-1, true);
			break;
		case BUTTON_RELEASE:
			lastEventButton=xiDevice.getLastEventButton();
			if( lastEventButton !=4 && lastEventButton !=5)
				scheduleButtonEvent(lastEventButton, false);
			break;
		case MOTION_NOTIFY:
			scheduleLevelEvent();
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

	void scheduleButtonEvent(int number, boolean state) {
		/*
		it fires different numbers for press and release. : (  TODO: support the pad buttons.
		if(L.isLoggable(Level.FINE))
			L.fine("scheduling button event: number="+number+", state="+state);
		getPenManager().scheduleButtonEvent(new PButton(number, state));*/
	}

	private static final float RADS_PER_DEG=(float)(Math.PI/180);

	private final float getMultRangedValue(PLevel.Type levelType) {
		if(levelType.equals(PLevel.Type.ROTATION)) // rotation and wheel are given using the same xinput valuator
			levelType=PLevel.Type.WHEEL;
		float devValue=xiDevice.getValue(levelType);
		// TODO: change to rotation if the name includes "airbrush" ? wait for feedback.

		if(PLevel.Type.TILT_TYPES.contains(levelType))
			return devValue*RADS_PER_DEG;

		devValue=levelRanges[levelType.ordinal()].getRangedValue(devValue);

		if(PLevel.Type.MOVEMENT_TYPES.contains(levelType))
			devValue=xinputProvider.screenBounds.getLevelRangeOffset(levelType)+
							 devValue*xinputProvider.screenBounds.getLevelRangeMult(levelType);

		return devValue;
	}
}