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
