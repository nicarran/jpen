package jpen.owner.awt;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import jpen.owner.AbstractPenOwner;
import jpen.owner.PenClip;
import jpen.PenProvider;
import jpen.provider.osx.CocoaProvider;
import jpen.provider.system.SystemProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

public final class AwtPenOwner
	extends AbstractPenOwner{

	public final Component component;
	final PenClipOnComponent penClipOnComponent;
	private final MouseListener mouseListener=new MouseAdapter(){
				@Override
				public void mouseExited(MouseEvent ev) {
					synchronized(penManagerHandle.getPenSchedulerLock()){
						if(!startDraggingOut())
							penManagerHandle.setPenManagerPaused(true);
					}
				}

				@Override
				public void mouseEntered(MouseEvent ev) {
					synchronized(penManagerHandle.getPenSchedulerLock()){
						if(!stopDraggingOut())
							penManagerHandle.setPenManagerPaused(false);
					}
				}
			};

	public AwtPenOwner(Component component){
		this.component=component;
		this.penClipOnComponent=new PenClipOnComponent(component);
	}

	//@Override
	public Collection<PenProvider.Constructor> getPenProviderConstructors(){
		return Arrays.asList(
						 new PenProvider.Constructor[]{
							 new SystemProvider.Constructor(),
							 new XinputProvider.Constructor(),
							 new WintabProvider.Constructor(),
							 new CocoaProvider.Constructor(),
						 }
					 );
	}

	//@Override
	public PenClip getPenClip(){
		return penClipOnComponent;
	}

	@Override
	protected void init(){
		component.addMouseListener(mouseListener);
	}
}