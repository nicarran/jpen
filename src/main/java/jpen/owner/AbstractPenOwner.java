package jpen.owner;

import jpen.event.PenAdapter;
import jpen.event.PenListener;
import jpen.PButtonEvent;
import jpen.Pen;

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
							penManagerHandle.setPenManagerPaused(true);
						}
					}
				}
			};

	//@Override
	public final void setPenManagerHandle(PenManagerHandle penManagerHandle){
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

	protected final Pen getPen(){
		return penManagerHandle.getPenManager().pen;
	}

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

}