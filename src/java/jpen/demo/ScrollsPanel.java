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
	private final Map<PScroll.Type, Counter> scrollTypeToCounter=new EnumMap<PScroll.Type, Counter>(PScroll.Type.class);
	{
		for(PScroll.Type scrollType: PScroll.Type.VALUES)
			scrollTypeToCounter.put(scrollType, new Counter());
	}

	static class Counter
		extends JTextField{
		private int value=-1;
		Counter(){
			super(3);
			setHorizontalAlignment(JTextField.RIGHT);
			setEditable(false);
			increase();
		}
		void increase(){
			setText(String.valueOf(++value));
		}
	}

	Box panel=Box.createVerticalBox();
	{
		for(PScroll.Type scrollType: PScroll.Type.VALUES){
			panel.add(Utils.labelComponent(
			            scrollType.toString(), scrollTypeToCounter.get(scrollType)
			          ));
		}
	}

	ScrollsPanel(Pen pen){
		pen.addListener(new SwingPenListenerProxy(new PenAdapter(){
			                @Override
			                public void penScrollEvent(PScrollEvent ev){
												Counter counter=scrollTypeToCounter.get(ev.scroll.getType());
												counter.increase();
			                }
		                }).proxy);
	}
}
