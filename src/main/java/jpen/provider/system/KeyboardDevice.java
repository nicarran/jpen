package jpen.provider.system;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;
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

	@Override
	public String getName(){
		return "Keyboard";
	}

	@Override
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		if(enabled){
			Toolkit.getDefaultToolkit().addAWTEventListener(awtListener, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
		}else{
			Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener);
		}
	}

	@Override
	public boolean getUseFractionalMovements(){
		return false;
	}

	private final AwtListener awtListener=new AwtListener();

	class AwtListener
		implements AWTEventListener{

		private InputEvent lastInputEvent;

		@Override
		public void eventDispatched(AWTEvent ev){
			if(ev instanceof InputEvent){
				lastInputEvent=(InputEvent)ev;
				if(lastInputEvent.getID()==MouseEvent.MOUSE_ENTERED){
					fireModifiers();
				}else	if(lastInputEvent instanceof KeyEvent){
					//System.out.println("lastInputEvent: "+lastInputEvent);
					fireModifiers();
				}
			}
		}

		private void fireModifiers(){
			InputEvent inputEvent=lastInputEvent; // local copy because this method is concurrent
			if(inputEvent==null)
				return;
			int modifiers=inputEvent.getModifiersEx();
			for(PButton.Type modifierType: PButton.TypeGroup.MODIFIER.getTypes()){
				boolean value=getModifierValue(modifiers, modifierType);
				getPenManager().scheduleButtonEvent(KeyboardDevice.this, inputEvent.getWhen(), new PButton(modifierType, value) );
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