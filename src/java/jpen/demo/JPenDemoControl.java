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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import jpen.Pen;

public class JPenDemoControl{

	//private static final Dimension SIZE=new Dimension(450, 480);
	//private static final Dimension SIZE=new Dimension(400, 440);
	private static final Dimension SIZE=new Dimension(400, 400);

	final PenCanvas penCanvas;
	final MainPanel mainPanel;
	final JButton statusReportButton=new JButton("Status Report...");

	JPenDemoControl(){
		penCanvas=new PenCanvas();
		Pen pen=penCanvas.penManager.pen;
		ButtonsPanel buttonsPanel=new ButtonsPanel(pen);
		ScrollsPanel scrollsPanel=new ScrollsPanel(pen);
		KindPanel kindPanel=new KindPanel(pen);
		LevelsPanel levelsPanel=new LevelsPanel(pen);
		AvailableTimePanel availableTimePanel=new AvailableTimePanel(pen); // the last listener to measure the really available time
		mainPanel=new MainPanel(penCanvas, buttonsPanel, scrollsPanel, kindPanel, levelsPanel, availableTimePanel);
		mainPanel.panel.setPreferredSize(SIZE);

		statusReportButton.addActionListener(new ActionListener(){
			    //@Override
			    public void actionPerformed(ActionEvent ev){
				    StatusReportPanel statusReportPanel=new StatusReportPanel(
				          new StatusReport(penCanvas.penManager));
				    statusReportPanel.panel.setPreferredSize(SIZE);
				    JOptionPane.showMessageDialog(mainPanel.panel, statusReportPanel.panel, "JPen Status Report", JOptionPane.INFORMATION_MESSAGE);
			    }
		    });

		/*JFrame f=new JFrame("JPen Demo");
		f.getContentPane().add(mainPanel.panel);
		f.setSize(new Dimension(450, 480));
		f.setResizable(false);
		f.setVisible(true);
		f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);*/
	}

	public void showDialog(JComponent parent, String title){
		String closeOption="Close";
		Object[] options=new Object[]{statusReportButton, closeOption};
		JOptionPane.showOptionDialog(null, mainPanel.panel, title,
		    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
		    null, options, closeOption);
	}

	public static void main(String... args) {
		JPenDemoControl jpenDemoControl=new JPenDemoControl();
		jpenDemoControl.showDialog(null, "JPen Demo");
		System.exit(0);
	}
}
