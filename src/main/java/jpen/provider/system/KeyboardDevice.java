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
package jpen.provider.system;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;
import jpen.PButton;
import jpen.PenProvider;
import jpen.PKind;
import jpen.provider.AbstractPenDevice;

final class KeyboardDevice
	extends AbstractPenDevice{
	private static final Logger L=Logger.getLogger(KeyboardDevice.class.getName());
	//static { L.setLevel(Level.ALL); }

	KeyboardDevice(PenProvider penProvider){
		super(penProvider);
		setKindTypeNumber(PKind.Type.IGNORE.ordinal());
		setEnabled(true);
	}

	//@Override
	public String getName(){
		return "Keyboard";
	}

	@Override
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		if(enabled){
			AccessController.doPrivileged(new PrivilegedAction<Object>(){
						//@Override
						public Object run(){
							Toolkit.getDefaultToolkit().addAWTEventListener(awtListener, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
							return null;
						}
					});

		}else{
			Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener);
		}
	}

	@Override
	public boolean getUseFractionalMovements(){
		return false;
	}

	private final AwtListener awtListener=new AwtListener();

	private static class ModifiersInfo{
		final int modifiersEx;
		final long when;
		ModifiersInfo(InputEvent inputEvent){
			this.modifiersEx=inputEvent.getModifiersEx();
			this.when=inputEvent.getWhen();
		}
	}

	class AwtListener
		implements AWTEventListener{

		private ModifiersInfo lastModifiersInfo;

		//@Override
		public void eventDispatched(AWTEvent ev){
			if(ev instanceof InputEvent){
				InputEvent inputEvent=(InputEvent)ev;
				lastModifiersInfo=new ModifiersInfo(inputEvent);
				if(inputEvent.getID()==MouseEvent.MOUSE_ENTERED){
					fireModifiers();
				}else	if(inputEvent instanceof KeyEvent){
					fireModifiers();
				}
			}
		}

		private void fireModifiers(){
			if(!getEnabled())
				return;
			ModifiersInfo modifiersInfo=lastModifiersInfo;// local copy because this method is concurrent
			if(modifiersInfo==null)
				return;
			for(PButton.Type modifierType: PButton.TypeGroup.MODIFIER.getTypes()){
				boolean value=getModifierValue(modifiersInfo.modifiersEx, modifierType);
				getPenManager().scheduleButtonEvent(KeyboardDevice.this, modifiersInfo.when, new PButton(modifierType, value) );
			}
		}
	}

	private static boolean getModifierValue(int modifiers, PButton.Type modifier){
		int modifierMask=getModifierMask(modifier);
		return (modifiers & modifierMask)==modifierMask;
	}

	private static int getModifierMask(PButton.Type modifier){
		switch(modifier){
		case CONTROL:
			return InputEvent.CTRL_DOWN_MASK;
		case SHIFT:
			return InputEvent.SHIFT_DOWN_MASK;
		case ALT:
			return InputEvent.ALT_DOWN_MASK;
		}
		return 0;
	}

	void setPaused(boolean paused){
		if(!paused)
			awtListener.fireModifiers();
	}
}