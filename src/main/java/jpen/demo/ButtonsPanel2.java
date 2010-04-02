package jpen.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import jpen.demo.Utils;
import jpen.event.PenAdapter;
import jpen.PButton;
import jpen.PButtonEvent;
import jpen.Pen;

final class ButtonsPanel2{
	
	private PButtonEvent lastButtonEvent;
	final JTextField lastButtonEventTF=new JTextField();
	{
		lastButtonEventTF.setEditable(false);
		Dimension prefSize=lastButtonEventTF.getPreferredSize();
		lastButtonEventTF.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefSize.height));
	}

	Display display=new Display();

	class Display
		extends DataDisplay<JList>{
		final DefaultListModel listModel=new DefaultListModel();

		Display(){
			super(new JList());
			component.setModel(listModel);
			//component.setEnabled(false);
			setupSize();
		}

		private void setupSize(){
			// fill the list to calc width:
			for(PButton.Type buttonType: PButton.Type.VALUES)
				listModel.addElement(buttonType);
			component.setFixedCellWidth(component.getPreferredSize().width+5);
			component.setVisibleRowCount(4);
			listModel.clear();
		}

		@Override
		void updateImp(Pen pen){
			listModel.clear();
			for(PButton.Type buttonType: PButton.Type.VALUES){
				if(pen.getButtonValue(buttonType)){
					listModel.addElement(buttonType);
				}
			}
			if(lastButtonEvent!=null)
				lastButtonEventTF.setText(lastButtonEvent.button.getType().toString());
		}
	}
	
	final JComponent panel=Box.createVerticalBox();
	{
		JScrollPane scrollPane=new JScrollPane(display.component);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(Utils.alignTopLeft(scrollPane));
		panel.add(Utils.createVerticalStrut());
		panel.add(Utils.alignTopLeft(new JLabel("Last:")));
		panel.add(Utils.createVerticalStrut());
		panel.add(Utils.alignTopLeft(lastButtonEventTF));
	}

	ButtonsPanel2(Pen pen){
		pen.addListener(new PenAdapter(){
											Pen pen;
											@Override
											public void penButtonEvent(PButtonEvent ev){
												this.pen=ev.pen;
												display.setIsDirty(true);
												lastButtonEvent=ev;
											}
											@Override
											public void penTock(long availableMillis){
												if(pen==null)
													return;
												display.update(pen);
												pen=null;
											}
										});
	}
}