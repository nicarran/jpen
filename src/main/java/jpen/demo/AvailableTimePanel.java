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

import javax.swing.JComponent;
import javax.swing.JTextField;
import jpen.event.PenAdapter;
import jpen.Pen;

class AvailableTimePanel{
	final StringBuilder text=new StringBuilder();
	final JTextField textField=new JTextField(7);

	final JComponent panel=textField;

	AvailableTimePanel(final Pen pen){
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.setEditable(false);
		pen.addListener(new PenAdapter(){
			                String period=String.valueOf(1000/pen.getFrequency());
			                @Override
			                public void penTock(long availableMillis){
				                text.setLength(0);
				                text.append(availableMillis);
				                text.append(" / ");
				                text.append(period);
												text.append(" ms");
				                textField.setText(text.toString());
			                }
		                });
	}
}
