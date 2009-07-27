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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class StatusReportPanel{
	private final JTextArea textArea=new JTextArea();
	
	final JScrollPane panel=new JScrollPane(textArea);
	{
		panel.setPreferredSize(new Dimension(600, 430));
	}
	
	StatusReportPanel(StatusReport statusReport){
		textArea.setText(statusReport.toString());
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setTabSize(1);
		textArea.setCaretPosition(0);
	}
}