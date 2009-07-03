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

import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JTextField;
import jpen.event.PenAdapter;
import jpen.Pen;
import jpen.PScroll;
import jpen.PScrollEvent;

class ScrollsPanel{
	private final Map<PScroll.Type, Display> scrollTypeToDisplay=new EnumMap<PScroll.Type, Display>(PScroll.Type.class);
	{
		for(PScroll.Type scrollType: PScroll.Type.VALUES)
			scrollTypeToDisplay.put(scrollType, new Display());
	}

	static class Display
		extends DataDisplay<JTextField>{
		private int value=0;
		Display(){
			super(new JTextField(3));
			component.setHorizontalAlignment(JTextField.RIGHT);
			component.setEditable(false);
		}

		@Override
		void updateImp(Pen pen){
			component.setText(String.valueOf(value));
		}

		void increase(int value){
			this.value+=value;
			setIsDirty(true);
		}
	}

	Box panel=Box.createVerticalBox();
	{
		for(PScroll.Type scrollType: PScroll.Type.VALUES){
			panel.add(Utils.labelComponent(
									scrollType.toString(), scrollTypeToDisplay.get(scrollType).component
								));
		}
	}

	ScrollsPanel(Pen pen){
		pen.addListener(new PenAdapter(){
											private Pen pen;
											@Override
											public void penScrollEvent(PScrollEvent ev){
												pen=ev.pen;
												Display display=scrollTypeToDisplay.get(ev.scroll.getType());
												if(display!=null)
													display.increase(ev.scroll.value);
											}
											@Override
											public void penTock(long availableMillis){
												if(pen==null)
													return;
												for(Display display: scrollTypeToDisplay.values())
													display.update(pen);
												pen=null;
											}
										});
	}
}
