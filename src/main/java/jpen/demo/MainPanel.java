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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JTabbedPane;

class MainPanel{
	private final JTabbedPane tabbedPane=new JTabbedPane(JTabbedPane.TOP);
	final Box panel=Box.createVerticalBox();
	final DevicesPanel devicesPanel; 
	
	MainPanel(PenCanvas penCanvas){
		penCanvas.scrollPane.setBorder(BorderFactory.createTitledBorder("Pen Enabled Component"));
		panel.add(Utils.alignTopLeft(penCanvas.scrollPane));
		
		StatePanel statePanel=new StatePanel(penCanvas.penManager);
		tabbedPane.addTab("Pen", statePanel.panel);
		
		devicesPanel=new DevicesPanel(penCanvas.penManager);
		//devicesPanel.panel.setBorder(BorderFactory.createTitledBorder("Devices"));
		tabbedPane.addTab("Input Devices", devicesPanel.panel);
		
		panel.add(Utils.alignTopLeft(tabbedPane));
	}
}
