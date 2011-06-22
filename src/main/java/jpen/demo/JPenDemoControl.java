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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jpen.demo.inspect.Inspector;
import jpen.owner.multiAwt.AwtPenToolkit;
import jpen.PButton;
import jpen.Pen;
import jpen.PenManager;
import jpen.PKind;
import jpen.PLevel;
import jpen.PLevelEmulator;

public class JPenDemoControl{
	private static final Logger L=Logger.getLogger(JPenDemoControl.class.getName());
	//static{L.setLevel(Level.ALL);}

	//private static final Dimension SIZE=new Dimension(400, 400);

	final MainPanel mainPanel;
	final JButton statusReportButton=new JButton("Status Report...");
	final JButton newInstanceButton=new JButton("New Demo Window");

	public JPenDemoControl(){
		PenManager penManager=AwtPenToolkit.getPenManager();
		penManager.pen.setFirePenTockOnSwing(true);
		penManager.pen.setFrequencyLater(40);
		penManager.pen.levelEmulator.setPressureTriggerForLeftCursorButton(0.5f);

		mainPanel=new MainPanel();

		setSupportCustomPKinds(true);

		statusReportButton.addActionListener(new ActionListener(){
					//@Override
					public void actionPerformed(ActionEvent ev){
						StatusReportPanel statusReportPanel=new StatusReportPanel(
									new StatusReport(AwtPenToolkit.getPenManager()));
						//statusReportPanel.panel.setPreferredSize(SIZE);
						JOptionPane.showMessageDialog(mainPanel.panel, statusReportPanel.panel, "JPen Status Report", JOptionPane.INFORMATION_MESSAGE);
					}
				});
		newInstanceButton.addActionListener(new ActionListener(){
					//@Override
					public void actionPerformed(ActionEvent ev){
						JPenDemoControl jpenDemoControl=new JPenDemoControl();
						jpenDemoControl.showFrame();
					}
				});
	}

	public void setSupportCustomPKinds(boolean supportCustomPKinds){
		mainPanel.devicesPanel.devicesTableModel.
		setSupportCustomPKinds(supportCustomPKinds);
	}

	public JComponent getMainPanelComponent(){
		return mainPanel.panel;
	}

	public JButton getStatusReportButton(){
		return statusReportButton;
	}

	public static void main(String... args) throws IOException, NumberFormatException{
		setupLookAndFeel();
		JPenDemoControl jpenDemoControl=new JPenDemoControl();
		startInspector(AwtPenToolkit.getPenManager());
		jpenDemoControl.showFrame();
	}

	static void setupLookAndFeel(){
		try{
			if(System.getProperty("os.name").toLowerCase().contains("linux"))
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			else
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception ex){
			L.warning("The \"system\" look and feel couldn't be set.");
		}
	}

	static void startInspector(PenManager penManager) throws IOException{
		String inspectorPeriodProperty=System.getProperty("jpen.demo.inspectorPeriod");
		if(inspectorPeriodProperty!=null){
			int inspectorPeriod=Integer.valueOf(inspectorPeriodProperty);
			Inspector inspector=new Inspector(penManager, "jpen", inspectorPeriod);
			L.info("inspector constructed");
		}
	}

	void showFrame(){
		JFrame f=new JFrame("JPen Demo");
		JPanel framePanel=new JPanel(new BorderLayout(4, 4));
		framePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 5, 5));
		f.setContentPane(framePanel);
		framePanel.add(mainPanel.panel);
		Box buttonBox=Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		//buttonBox.add(newInstanceButton);
		//buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(statusReportButton);
		framePanel.add(buttonBox, BorderLayout.SOUTH);

		f.pack();
		f.setLocationByPlatform(true);
		f.setVisible(true);
		f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
	}

	@Deprecated
	public void showDialog(JComponent parent, String title){
		String closeOption="Close";
		Object[] options=new Object[]{statusReportButton, closeOption};
		JOptionPane.showOptionDialog(null, getMainPanelComponent(), title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, closeOption);
	}
}