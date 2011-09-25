/* [{
Copyright 2011 Nicolas Carranza <nicarran at gmail.com>

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
			return activeComponentInfo.getComponent();
		}
		throw new IllegalArgumentException("the given PenEvent was not tagged by the MultiAwtPenOwner");
	}
}