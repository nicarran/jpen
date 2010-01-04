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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import jpen.Pen;
import jpen.PenManager;

class StatePanel{
	final Box panel=Box.createVerticalBox();

	StatePanel(PenManager penManager){
		Pen pen=penManager.pen;
		
		ButtonsPanel buttonsPanel=new ButtonsPanel(pen);
		ScrollsPanel scrollsPanel=new ScrollsPanel(pen);
		KindPanel kindPanel=new KindPanel(pen);
		LevelsPanel levelsPanel=new LevelsPanel(pen);
		SampleRatePanel sampleRatePanel=new SampleRatePanel(pen);
		// --- the last listener to measure the time still available: 
		AvailableTimePanel availableTimePanel=new AvailableTimePanel(pen); 
		
		Box mainLine=Box.createHorizontalBox();
		panel.add(Utils.alignTopLeft(mainLine));

		Box column=Box.createVerticalBox();
		levelsPanel.panel.setBorder(BorderFactory.createTitledBorder("Levels"));
		column.add(Utils.alignTopLeft(levelsPanel.panel));

		mainLine.add(Utils.alignTopLeft(column));

		column=Box.createVerticalBox();
		column.add(Utils.labelComponent(
		             "Kind:", kindPanel.panel
		           ));
		Box line=Box.createHorizontalBox();
		buttonsPanel.panel.setBorder(BorderFactory.createTitledBorder("Buttons"));
		line.add(Utils.alignTopLeft(buttonsPanel.panel));
		scrollsPanel.panel.setBorder(BorderFactory.createTitledBorder("Scrolls"));
		line.add(Utils.createHorizontalStrut());
		line.add(Utils.alignTopLeft(scrollsPanel.panel));
		column.add(Utils.alignTopLeft(line));
		column.add(Utils.labelComponent(
		             "Available Millis:", availableTimePanel.panel
		           ));
		column.add(Utils.labelComponent(
																		"Sample Period:", sampleRatePanel.panel
																		));
		column.add(Utils.labelComponent(
		             "JPen Version:", new JLabel(PenManager.getJPenFullVersion())
		           ));

		mainLine.add(Utils.createHorizontalStrut());
		mainLine.add(Utils.alignTopLeft(column));
	}
}
