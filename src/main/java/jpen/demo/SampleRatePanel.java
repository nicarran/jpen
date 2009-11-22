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

import java.util.LinkedList;
import java.util.List;
import javax.swing.JTextField;
import jpen.event.PenAdapter;
import jpen.Pen;
import jpen.PLevelEvent;

class SampleRatePanel{
	public static final int SAMPLE_COUNT=10;
	private final LinkedList<Integer> periods=new LinkedList<Integer>();
	private int average;

	private final JTextField textField=new JTextField(7);
	{
		textField.setEditable(false);
		textField.setHorizontalAlignment(JTextField.RIGHT);
	}

	final JTextField panel=textField;

	SampleRatePanel(final Pen pen){
		pen.addListener(new PenAdapter(){
			                private long lastDeviceTime=-1;
			                @Override
			                public void penLevelEvent(PLevelEvent ev){
				                long evDeviceTime=ev.getDeviceTime();
				                if(lastDeviceTime==-1){
					                lastDeviceTime=evDeviceTime;
					                return;
				                }
				                long period=evDeviceTime-lastDeviceTime;
				                addPeriod((int)period);
				                lastDeviceTime=evDeviceTime;
			                }
			                @Override
			                public void penTock(long availableMillis){
				                updateTextField();
			                }
		                });
	}


	void addPeriod(int period){
		if(periods.size()==SAMPLE_COUNT)
			periods.removeFirst();
		periods.add(period);
	}

	void updateAverage(){
		average=0;
		for(int period: periods)
			average+=period;
		if(periods.size()>0)
			average/=periods.size();
	}

	void updateTextField(){
		updateAverage();
		textField.setText(average+" ms");
	}
}
