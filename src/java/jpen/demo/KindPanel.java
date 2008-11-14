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
import javax.swing.JTextField;
import jpen.event.PenAdapter;
import jpen.Pen;
import jpen.PKind;
import jpen.PKindEvent;

class KindPanel{
	final JTextField kindTextField=new JTextField(8);
	{
		kindTextField.setEditable(false);
		kindTextField.setHorizontalAlignment(JTextField.CENTER);
	}
	final Component panel=kindTextField;

	KindPanel(Pen pen){
		pen.addListener(new SwingPenListenerProxy(new PenAdapter(){
			                @Override
			                public void penKindEvent(PKindEvent ev){
												update(ev.kind);
											}
		                }).proxy);
		update(pen.getKind());
	}
	
	void update(PKind kind){
		kindTextField.setText(kind.getType().toString());
	}
}
