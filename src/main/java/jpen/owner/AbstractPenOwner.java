/* [{
Copyright 2007-2011 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.owner;

import jpen.event.PenAdapter;
import jpen.event.PenListener;
import jpen.PButtonEvent;
import jpen.Pen;
import jpen.PenEvent;

/**
Provides a mechanism to start/stop the drag-out operation.
*/
public abstract class AbstractPenOwner
	implements PenOwner{

	protected PenManagerHandle penManagerHandle;
	private boolean isDraggingOut; // state: true when dragging outside the component
	private final PenListener draggingOutPenListener=new PenAdapter(){
				@Override
				public void penButtonEvent(PButtonEvent ev){
					synchronized(penManagerHandle.getPenSchedulerLock()){
						if(!ev.button.value &&
							 !getPen().hasPressedButtons() &&
							 isDraggingOut
							){
							stopDraggingOut();
							draggingOutDisengaged();
						}
					}
				}
			};

	/**
	Force stopping a drag-out operation if it was in progress. This method must be called when entering the {@link PenClip} to stop the drag-out operation if it was still in progress.

	This method must be called while holding the {@link jpen.owner.PenOwner.PenManagerHandle#getPenSchedulerLock()} (See {@link PenOwner#isDraggingOut()}).

	@return {@code true} if a drag-out operation was in progress and was stopped.
	*/
	protected final boolean stopDraggingOut(){
		if(!isDraggingOut)
			return false;
		isDraggingOut=false;
		getPen().removeListener(draggingOutPenListener);
		return true;
	}

	protected abstract void draggingOutDisengaged();

	/**
	Starts a drag-out operation if there are pressed buttons and installs a penListener which automatically stops the drag-out operation when all the buttons are unpressed.

	This method must be called while holding the {@link jpen.owner.PenOwner.PenManagerHandle#getPenSchedulerLock()} (See {@link PenOwner#isDraggingOut()}).

	@return {@code true} if a drag-out operation was started or was already in progress.
	*/
	protected final boolean startDraggingOut(){
		if(isDraggingOut)
			return true;
		if(!getPen().hasPressedButtons())
			return false;
		isDraggingOut=true;
		getPen().addListener(draggingOutPenListener);
		return true;
	}

	//@Override
	public final void setPenManagerHandle(PenManagerHandle penManagerHandle){
		if(this.penManagerHandle!=null)
			throw new IllegalStateException("The penManagerHandle has already being set. This PenOwner can't be used on multiple PenManagers.");
		this.penManagerHandle=penManagerHandle;
		init();
	}

	/**
	Override this method to do initialization. This method is called after the {@link #penManagerHandle} is set.
	*/
	protected abstract void init();

	//@Override
	public final boolean isDraggingOut(){
		return isDraggingOut;
	}

	protected final Pen getPen(){
		return penManagerHandle.getPenManager().pen;
	}

	//@Override
	public Object evalPenEventTag(PenEvent ev){
		return null;
	}

	//@Override
	public boolean enforceSinglePenManager(){
		return false;
	}

}