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
import java.awt.Dimension;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;

final class Utils{
	private static final Border LABELED_COMPONENT_BORDER=BorderFactory.createEmptyBorder(3,3,2,2);

	static JComponent labelComponent(String label, JComponent c){
		Box box=Box.createHorizontalBox();
		if(label!=null){
			if(!label.trim().endsWith(":"))
				label=label.trim()+": ";
			box.add(new JLabel(label));
		}
		box.add(Box.createHorizontalGlue());
		freezeSizeToPreferred(c);
		box.add(c);
		box.setBorder(LABELED_COMPONENT_BORDER);
		return alignTopLeft(box);
	}

	static JComponent freezeSizeToPreferred(JComponent c){
		freezeSize(c, c.getPreferredSize());
		return c;
	}

	static JComponent freezeSize(JComponent c, Dimension s){
		c.validate();
		c.setMinimumSize(s);
		c.setMaximumSize(s);
		c.setPreferredSize(s);
		return c;
	}

	static JComponent alignTopLeft(JComponent c){
		c.setAlignmentX(Component.LEFT_ALIGNMENT);
		c.setAlignmentY(Component.TOP_ALIGNMENT);
		return c;
	}

	static Component createHorizontalStrut(){
		return new Box.Filler(new Dimension(3,0), new Dimension(3,0),
					 new Dimension(3, 0));
	}

	static Component createVerticalStrut(){
		return new Box.Filler(new Dimension(0,3), new Dimension(0,3),
					 new Dimension(0, 3));
	}
}