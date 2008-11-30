package jpen.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JPanel;

public class JPenDemoApplet
extends JApplet{
	
	private JPenDemoControl jpenDemoControl;
	private JButton showStatus;
	
	@Override
	public void init(){
		jpenDemoControl=new JPenDemoControl();
		
		Container container=getContentPane();
		container.add(jpenDemoControl.getMainPanelComponent());
		JPanel buttonsPanel=new JPanel();
		buttonsPanel.add(jpenDemoControl.getStatusReportButton());
		container.add(buttonsPanel, BorderLayout.SOUTH);
	}
}
