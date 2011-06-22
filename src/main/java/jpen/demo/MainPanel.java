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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

class MainPanel{
	private final JTabbedPane tabbedPane=new JTabbedPane(JTabbedPane.TOP);
	final Box panel=Box.createVerticalBox();
	final DevicesPanel devicesPanel;

	MainPanel(){
		JSplitPane canvasesPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		canvasesPane.setLeftComponent(new PenCanvas().scrollPane);
		canvasesPane.setRightComponent(new PenCanvas().scrollPane);
		//canvasesPane.setContinuousLayout(true);
		canvasesPane.setOneTouchExpandable(true);
		canvasesPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		canvasesPane.setBorder(BorderFactory.createTitledBorder("Pen Enabled Components"));
		panel.add(canvasesPane);

		StatePanel statePanel=new StatePanel();
		tabbedPane.addTab("Pen", statePanel.panel);

		devicesPanel=new DevicesPanel();
		//devicesPanel.panel.setBorder(BorderFactory.createTitledBorder("Devices"));
		tabbedPane.addTab("Input Devices", devicesPanel.panel);

		tabbedPane.setMaximumSize(tabbedPane.getPreferredSize());

		tabbedPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(tabbedPane);
	}
}