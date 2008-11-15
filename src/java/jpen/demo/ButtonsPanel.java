/* [{
Copyright 2008 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.demo;

import java.util.EnumMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JCheckBox;
import jpen.event.PenAdapter;
import jpen.PButton;
import jpen.PButtonEvent;
import jpen.Pen;

class ButtonsPanel{
	private final Map<PButton.Type, Display> buttonTypeToDisplay=new EnumMap<PButton.Type, Display>(PButton.Type.class);
	{
		for(PButton.Type buttonType: PButton.Type.VALUES){
			buttonTypeToDisplay.put(buttonType, new Display(buttonType));
		}
	}
	static class Display
		extends DataDisplay<JCheckBox>{
		final PButton.Type buttonType;
		Display(PButton.Type buttonType){
			super(new JCheckBox(buttonType.toString()));
			this.buttonType=buttonType;
			component.setEnabled(false);
		}

		@Override
		void updateImp(Pen pen){
			component.setSelected(pen.getButtonValue(buttonType));
		}
	}
	public final Box panel=Box.createVerticalBox();
	{
		for(PButton.Type buttonType: PButton.Type.VALUES){
			panel.add(buttonTypeToDisplay.get(buttonType).component);
		}
	}

	ButtonsPanel(Pen pen){
		pen.addListener(new PenAdapter(){
			                private Pen pen;
			                @Override
			                public void penButtonEvent(PButtonEvent ev){
				                pen=ev.pen;
				                PButton.Type buttonType=ev.button.getType();
				                if(buttonType==null){
					                System.out.println("null button type for event: "+ev);
					                return;
				                }
				                Display display=buttonTypeToDisplay.get(buttonType);
				                display.setIsDirty(true);
			                }
			                @Override
			                public void penTock(long availableMillis){
				                if(pen==null)
					                return;
				                for(Display display: buttonTypeToDisplay.values())
					                display.update(pen);
				                pen=null;
			                }
		                }
		               );
	}
}
