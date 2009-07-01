/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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
package jpen;

import java.awt.geom.Point2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import jpen.owner.PenOwner;

final class PenScheduler{
	static final Logger L=Logger.getLogger(PenScheduler.class.getName());
	//static { L.setLevel(Level.ALL); }

	private final Pen pen;
	private PenEvent lastScheduledEvent;
	public final PenState lastScheduledState=new PenState();
	private final PhantomEventFilter phantomLevelFilter=new PhantomEventFilter();
	private final List<PLevel> scheduledLevels=new ArrayList<PLevel>();

	PenScheduler(Pen pen){
		this.pen=pen;
		lastScheduledEvent=pen.getLastDispatchedEvent();
	}

	private static class PhantomEventFilter {
		public static int THRESHOLD_PERIOD=200;
		private PenDevice lastDevice; // last device NOT filtered
		private PenEvent lastEvent; // last event scheduled
		boolean filteredFirstInSecuence;
		private long time;
		private long firstInSecuenceTime;

		boolean filter(PenDevice device) {
			if(!device.isDigitizer()) {
				time=System.currentTimeMillis();
				if(lastDevice!=null &&
								lastDevice!=device &&
								lastEvent!=null &&
								time-lastEvent.time<=THRESHOLD_PERIOD
					)
					return true;
				if(!filteredFirstInSecuence) {
					L.fine("filtered first in sequence to prioritize digitized input in race");
					filteredFirstInSecuence=true;
					firstInSecuenceTime=System.currentTimeMillis();
					return true;
				}
				if(time-firstInSecuenceTime<=THRESHOLD_PERIOD){
					L.fine("filtering after the first for a period to allow digitized input to come and win in race");
					return true;
				}
				L.fine("non digitized input going as event");
			} else
				filteredFirstInSecuence=false;
			lastDevice=device;
			return false;
		}

		void setLastEvent(PenEvent event) {
			this.lastEvent=event;
		}

		PenEvent getLastEvent() {
			return lastEvent;
		}
	}

	private final Point clipLocationOnScreen=new Point();
	private final Point2D.Float scheduledLocation=new Point2D.Float();

	synchronized boolean scheduleLevelEvent(PenManager penManager, PenDevice device, long penDeviceTime, Collection<PLevel> levels, boolean levelsOnScreen) {
		// if device == null then this is an emulated event request
		if(device!=null && phantomLevelFilter.filter(device))
			return false;
		boolean scheduledMovement=false;
		scheduledLocation.x=lastScheduledState.levels.getValue(PLevel.Type.X);
		scheduledLocation.y=lastScheduledState.levels.getValue(PLevel.Type.Y);
		if(penManager!=null && levelsOnScreen)
			penManager.penOwner.getPenClip().evalLocationOnScreen(clipLocationOnScreen);
		for(PLevel level:levels) {
			if(level.value==lastScheduledState.getLevelValue(level.typeNumber))
				continue;
			if(device!=null && pen.levelEmulator!=null && pen.levelEmulator.isActiveLevel(level.typeNumber))
				continue;
			if(pen.getLevelFilter().filterPenLevel(level))
				continue;
			PLevel.Type levelType=level.getType();
			switch(levelType){
			case X:
				scheduledMovement=true;
				if(levelsOnScreen)
					level.value=level.value-clipLocationOnScreen.x;
				scheduledLocation.x=level.value;
				break;
			case Y:
				scheduledMovement=true;
				if(levelsOnScreen)
					level.value=level.value-clipLocationOnScreen.y;
				scheduledLocation.y=level.value;
				break;
			default:
			}
			scheduledLevels.add(level);
		}
		if(scheduledLevels.isEmpty())
			return false;

		if(scheduledMovement){
			if(penManager!=null && !penManager.penOwner.getPenClip().contains(scheduledLocation)
							&& !penManager.penOwner.isDraggingOut())
				return false;

			if(device!=null &&
							device.getKindTypeNumber() !=lastScheduledState.getKind().typeNumber &&
							device.getKindTypeNumber()!=PKind.Type.IGNORE.ordinal() ){
				PKind newKind=PKind.valueOf(device.getKindTypeNumber());
				if(L.isLoggable(Level.FINE)){
					L.fine("changing kind to:"+newKind);
					L.fine("scheduledLevels: "+scheduledLevels);
					L.fine("device: "+device);
				}
				lastScheduledState.setKind(newKind);
				schedule(new PKindEvent(pen, newKind));
			}
		}

		lastScheduledState.levels.setValues(scheduledLevels);
		PLevelEvent levelEvent=new PLevelEvent(pen,
				scheduledLevels.toArray(new PLevel[scheduledLevels.size()]),
				device==null? -1: device.getId(),
				penDeviceTime);
		phantomLevelFilter.setLastEvent(levelEvent);
		scheduledLevels.clear();
		schedule(levelEvent);
		return true;
	}

	synchronized void scheduleButtonReleasedEvents(){
		for(int i=PButton.Type.VALUES.size(); --i>=0;)
			scheduleButtonEvent(new PButton(i, false));
		for(Integer extButtonTypeNumber: lastScheduledState.extButtonTypeNumberToValue.keySet())
			scheduleButtonEvent(new PButton(extButtonTypeNumber, false));
	}

	synchronized void scheduleButtonEvent(PButton button) {
		if(lastScheduledState.setButtonValue(button.typeNumber, button.value)){
			if(L.isLoggable(Level.FINE))
				L.fine("scheduling button event: "+button);
			PButtonEvent buttonEvent=new PButtonEvent(pen, button);
			schedule(buttonEvent);
			if(pen.levelEmulator!=null)
				pen.levelEmulator.scheduleEmulatedEvent(buttonEvent);
		}
	}

	synchronized void scheduleScrollEvent(PenDevice device, PScroll scroll) {
		schedule(new PScrollEvent(pen, scroll));
	}

	private void schedule(PenEvent ev) {
		ev.time=System.currentTimeMillis();
		lastScheduledEvent.next=ev;
		lastScheduledEvent=ev;
		pen.processNewEvents();
	}
}