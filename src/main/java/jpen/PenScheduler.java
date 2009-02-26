package jpen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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



	synchronized boolean scheduleLevelEvent(PenDevice device, long penDeviceTime, Collection<PLevel> levels,  int minX, int maxX, int minY, int maxY, PenManagerPlayer penManagerPlayer) {
		// if device == null then this is an emulated event request
		if(device!=null && phantomLevelFilter.filter(device))
			return false;
		boolean scheduledMovement=false;
		for(PLevel level:levels) {
			if(level.value==lastScheduledState.getLevelValue(level.typeNumber))
				continue;
			if(device!=null && pen.levelEmulator!=null && pen.levelEmulator.isActiveLevel(level.typeNumber))
				continue;
			if(pen.getLevelFilter().filterPenLevel(level))
				continue;
			switch(level.getType()){
			case X:
				scheduledMovement=true;
				if(!evalLevelValueIsInRange(level.value, minX, maxX, penManagerPlayer))
					continue;
				break;
			case Y:
				scheduledMovement=true;
				if(!evalLevelValueIsInRange(level.value, minY, maxY, penManagerPlayer))
					continue;
				break;
			default:
			}
			scheduledLevels.add(level);
			lastScheduledState.levels.setValue(level.typeNumber, level.value);
		}
		if(scheduledLevels.isEmpty())
			return false;
		if(device!=null && scheduledMovement &&
		        lastScheduledState.getKind().typeNumber!=
		        device.getKindTypeNumber()){
			if(device.getKindTypeNumber()!=PKind.Type.IGNORE.ordinal()){
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
		PLevelEvent levelEvent=new PLevelEvent(pen,
		    scheduledLevels.toArray(new PLevel[scheduledLevels.size()]), device==null? -1: device.getId(), penDeviceTime);
		phantomLevelFilter.setLastEvent(levelEvent);
		schedule(levelEvent);
		scheduledLevels.clear();
		return true;
	}

	private boolean evalLevelValueIsInRange(float levelValue, int min , int max, PenManagerPlayer penManagerPlayer){
		if(levelValue<min || levelValue>max){
			if(penManagerPlayer!=null &&
			        penManagerPlayer.stopPlayingIfNotDragOut())
				return false;
		}
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
				if(buttonEvent!=null && pen.levelEmulator!=null)
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
