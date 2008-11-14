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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JTextField;
import jpen.event.PenAdapter;
import jpen.Pen;
import jpen.PLevel;
import jpen.PLevelEvent;

class LevelsPanel{
	final Map<PLevel.Type, Display> levelTypeToDisplay=new EnumMap<PLevel.Type, Display>(PLevel.Type.class);
	{
		for(PLevel.Type levelType: PLevel.Type.VALUES){
			levelTypeToDisplay.put(levelType,
			    PLevel.Type.TILT_TYPES.contains(levelType)?
			    new AngleDisplay():
			    new Display());
		}
	}

	static class Display
		extends JTextField{

		static NumberFormat FORMAT=new DecimalFormat("###0.000");

		Display(){
			super(6);
			setHorizontalAlignment(JTextField.RIGHT);
			setEditable(false);
		}

		public void setValue(float value){
			setText(format(value));
		}

		String format(float value){
			return FORMAT.format(value);
		}
	}

	static class AngleDisplay
		extends Display{
		static NumberFormat FORMAT=new DecimalFormat("###0.0\u00ba");
		static float RAD_TO_DEG=(float)(180f/Math.PI);
		String format(float value){
			value*=RAD_TO_DEG;
			return FORMAT.format(value);
		}
	}

	final Box panel=Box.createVerticalBox();
	{
		for(PLevel.Type levelType:PLevel.Type.VALUES){
			panel.add(Utils.labelComponent(
			            levelType.toString(),levelTypeToDisplay.get(levelType)
			          ));
		}
	}

	LevelsPanel(Pen pen){
		pen.addListener(new SwingPenListenerProxy(new PenAdapter(){
			                @Override
			                public void penLevelEvent(PLevelEvent ev){
				                for(PLevel level: ev.levels){
					                Display display=levelTypeToDisplay.get(level.getType());
					                display.setValue(level.value);
				                }
			                }
		                }).proxy);
		for(PLevel.Type levelType: PLevel.Type.VALUES){
			Display display=levelTypeToDisplay.get(levelType);
			display.setValue(pen.getLevelValue(levelType));
		}
	}

}
