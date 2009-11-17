package jpen.owner;
import java.awt.Component;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;

import jpen.PenProvider;
import jpen.provider.osx.CocoaProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

public class ScreenPenOwner implements PenOwner {

//	@Override
	public Collection<PenProvider.Constructor> getPenProviderConstructors(){
		return Arrays.asList(
		         new PenProvider.Constructor[]{
		           // new SystemProvider.Constructor(), //Does not work because it needs a java.awt.Component to register the MouseListener
		           new XinputProvider.Constructor(),
		           new WintabProvider.Constructor(),
		           new CocoaProvider.Constructor()
		         }
		       );
	}

//	@Override
	public void setPenManagerHandle(PenManagerHandle penManagerHandle){
		// Only unpause it once and never pause it.
		penManagerHandle.setPenManagerPaused(false);
	}

	private final PenClip penClip = new PenClip() {	
//	    @Override
	    public void evalLocationOnScreen(Point locationOnScreen){
		    // The location of this PenClip is always on (0, 0) screen coordinates.
		    locationOnScreen.x=locationOnScreen.y=0;
	    }
//	    @Override
	    public boolean contains(Point2D.Float point){
		    // This PenClip covers all the screen.
		    return true;
	    }
    };

//	@Override
	public PenClip getPenClip() {
		return penClip;
	}

//	@Override
	public boolean isDraggingOut() {
		return false;
	}

	public Component getComponent() {
		return null;
	}
}