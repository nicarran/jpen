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

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;

class MainPanel{
	final Box panel=Box.createVerticalBox();

	MainPanel(PenCanvas penCanvas, ButtonsPanel buttonsPanel, ScrollsPanel scrollsPanel, KindPanel kindPanel, LevelsPanel levelsPanel, AvailableTimePanel availableTimePanel){
		penCanvas.scrollPane.setBorder(BorderFactory.createTitledBorder("Pen Enabled Component"));
		panel.add(Utils.alignTopLeft(penCanvas.scrollPane));

		Box bottomPanel=Box.createHorizontalBox();
		panel.add(Utils.alignTopLeft(bottomPanel));

		Box column=Box.createVerticalBox();
		levelsPanel.panel.setBorder(BorderFactory.createTitledBorder("Levels"));
		column.add(Utils.alignTopLeft(levelsPanel.panel));

		bottomPanel.add(Utils.alignTopLeft(column));

		column=Box.createVerticalBox();
		column.add(Utils.labelComponent(
		             "Kind:", kindPanel.panel
		           ));
		column.add(Utils.labelComponent(
		             "Available Millis:", availableTimePanel.panel
		           ));
		Box line=Box.createHorizontalBox();
		buttonsPanel.panel.setBorder(BorderFactory.createTitledBorder("Buttons"));
		line.add(Utils.alignTopLeft(buttonsPanel.panel));
		scrollsPanel.panel.setBorder(BorderFactory.createTitledBorder("Scrolls"));
		line.add(Utils.createHorizontalStrut());
		line.add(Utils.alignTopLeft(scrollsPanel.panel));
		column.add(Utils.alignTopLeft(line));
		column.add(Utils.labelComponent(
		             "JPen Version:", new JLabel(jpen.provider.Utils.getFullVersion())
		           ));
		/*column.add(Utils.labelComponent(
																		"Java Version:", new JLabel(System.getProperty("java.version"))
																		));
		column.add(Utils.labelComponent(
																		"Operating System:", new JLabel(System.getProperty("os.name"))
																		));*/

		bottomPanel.add(Utils.createHorizontalStrut());
		bottomPanel.add(Utils.alignTopLeft(column));
	}
}
