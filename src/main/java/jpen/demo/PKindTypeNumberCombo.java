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

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import jpen.PKind;

class PKindTypeNumberCombo{
	final JComboBox comboBox=new JComboBox();
	private int maxPKindTypeNumber=PKind.Type.VALUES.size()-1;
	{
		update();
		comboBox.setRenderer(new BasicComboBoxRenderer(){
					{
						setHorizontalAlignment(JLabel.CENTER);
					}
					@Override
					public Component 	getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
						super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						setText(getPKindTypeStringValue((Integer)value));
						return this;
					}
				});
	}


	static String getPKindTypeStringValue(int intValue){
		int maxKindTypeNumber=PKind.Type.VALUES.size();
		if(intValue>=maxKindTypeNumber)
			return "CUSTOM "+(intValue-maxKindTypeNumber);
		else
			return PKind.Type.VALUES.get(intValue).toString();
	}

	private void update(){
		comboBox.removeAllItems();
		for(int i=0; i<=maxPKindTypeNumber; i++)// the CUSTOM PKind can not be set directly.
				comboBox.addItem(Integer.valueOf(i));
	}

	int getMaxPKindTypeNumber(){
		return maxPKindTypeNumber;
	}

	void setMaxPKindTypeNumber(int maxPKindTypeNumber){
		this.maxPKindTypeNumber=maxPKindTypeNumber;
		update();
	}

}
