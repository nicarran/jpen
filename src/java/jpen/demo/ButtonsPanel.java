/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

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
	private final Map<PButton.Type, JCheckBox> buttonTypeToCheckBox=new EnumMap<PButton.Type, JCheckBox>(PButton.Type.class);
	{
		for(PButton.Type buttonType: PButton.Type.VALUES){
			JCheckBox checkBox=new JCheckBox(buttonType.toString());
			checkBox.setEnabled(false);
			buttonTypeToCheckBox.put(buttonType, checkBox);
		}
	}
	public final Box panel=Box.createVerticalBox();
	{
		for(PButton.Type buttonType: PButton.Type.VALUES){
			panel.add(buttonTypeToCheckBox.get(buttonType));
		}
	}

	ButtonsPanel(Pen pen){
		pen.addListener(new SwingPenListenerProxy(new PenAdapter(){
			                @Override
			                public void penButtonEvent(PButtonEvent ev){
												PButton.Type buttonType=ev.button.getType();
												if(buttonType==null){
													System.out.println("null button type for event: "+ev);
													return;
												}
				                JCheckBox checkBox=buttonTypeToCheckBox.get(buttonType);
				                checkBox.setSelected(ev.button.value);
			                }
		                }).proxy);
	}
}
