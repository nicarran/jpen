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

/**
nicarran: experimental class. Does not work right because key auto-repeat (linux only problem?).
*/
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

		//@Override
		public void eventDispatched(AWTEvent ev){
			if(ev instanceof InputEvent){
				lastInputEvent=(InputEvent)ev;
				if(lastInputEvent.getID()==MouseEvent.MOUSE_ENTERED){
					fireModifiers();
				}else	if(lastInputEvent instanceof KeyEvent){
					System.out.println("lastInputEvent: "+lastInputEvent);
					fireModifiers();
					// TODO: 
					// fireKey((KeyEvent)lastInputEvent);
				}
			}
		}

		private void fireModifiers(){
			InputEvent inputEvent=lastInputEvent; // local copy because this method is concurrent
			if(inputEvent==null)
				return;
			int modifiers=inputEvent.getModifiersEx();
			/*
			for(PButton.Type modifierType: PButton.TypeGroup.MODIFIER.getTypes()){
				boolean value=getModifierValue(modifiers, modifierType);
				getPenManager().scheduleButtonEvent(KeyboardDevice.this, inputEvent.getWhen(), new PButton(modifierType, value) );
			}
			*/
		}

		private void reset(){
			lastInputEvent=null;
		}

		/*
		private void fireKey(KeyEvent ev){
			boolean value=ev.getID()!=KeyEvent.KEY_RELEASED;
			if(value && ev.getID()!=KeyEvent.KEY_PRESSED)
				return;
			PButton.Type virtualKey=getVirtualKey(ev.getKeyCode());
			if(virtualKey==null)
				return;
			getPenManager().scheduleButtonEvent(KeyboardDevice.this, ev.getWhen(), new PButton(virtualKey, value));
		}
		*/
	}

	/*
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
		if(paused){
			awtListener.reset();
		}else{
			awtListener.fireModifiers();
		}
	}
	*/

	/*
	private static PButton.Type getVirtualKey(int keyCode){
		switch(keyCode){
		case KeyEvent.VK_1:
			return PButton.Type.VK_1;
		case KeyEvent.VK_2:
			return PButton.Type.VK_2;
		case KeyEvent.VK_3:
			return PButton.Type.VK_3;
		case KeyEvent.VK_4:
			return PButton.Type.VK_4;
		case KeyEvent.VK_5:
			return PButton.Type.VK_5;
		case KeyEvent.VK_6:
			return PButton.Type.VK_6;
		case KeyEvent.VK_7:
			return PButton.Type.VK_7;
		case KeyEvent.VK_8:
			return PButton.Type.VK_8;
		case KeyEvent.VK_9:
			return PButton.Type.VK_9;
		case KeyEvent.VK_0:
			return PButton.Type.VK_0;
		}
		return null;
	}
	*/
}