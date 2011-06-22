package jpen.owner.multiAwt;

import java.awt.Component;
import jpen.event.PenListener;
import jpen.owner.multiAwt.MultiAwtPenOwner;
import jpen.PenEvent;
import jpen.PenManager;

/**
Contains a {@link PenManager} ready to be used on multiple Java AWT components. 
*/
public final class AwtPenToolkit{
	private static final MultiAwtPenOwner multiAwtPenOwner=new MultiAwtPenOwner();
	private static final PenManager penManager=new PenManager(multiAwtPenOwner);

	private AwtPenToolkit(){}

	public static final PenManager getPenManager(){
		return penManager;
	}

	public static void addPenListener(Component component, PenListener penListener){
		multiAwtPenOwner.componentPool.addPenListener(component, penListener);
	}

	public static void removePenListener(Component component, PenListener penListener){
		multiAwtPenOwner.componentPool.removePenListener(component, penListener);
	}

	public static Component getPenEventComponent(PenEvent ev){
		Object penOwnerTag= multiAwtPenOwner.getPenManagerHandle().retrievePenEventTag(ev);
		if(penOwnerTag instanceof MultiAwtPenOwner.ActiveComponentInfo){
			MultiAwtPenOwner.ActiveComponentInfo activeComponentInfo=(MultiAwtPenOwner.ActiveComponentInfo)penOwnerTag;
			return activeComponentInfo.component;
		}
		throw new IllegalArgumentException("the given PenEvent was not tagged by the MultiAwtPenOwner");
	}
}