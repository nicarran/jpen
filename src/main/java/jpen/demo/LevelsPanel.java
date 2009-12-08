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
			    PLevel.Type.TILT_TYPES.contains(levelType) ||
			    PLevel.Type.ROTATION.equals(levelType)?
			    new AngleDisplay(levelType):
			    new Display(levelType));
		}
	}

	static class Display
		extends DataDisplay<JTextField>{

		static NumberFormat FORMAT=new DecimalFormat("###0.000");

		final PLevel.Type levelType;

		Display(PLevel.Type levelType){
			super(new JTextField(6));
			this.levelType=levelType;
			component.setHorizontalAlignment(JTextField.RIGHT);
			component.setEditable(false);
		}

		@Override
		void updateImp(Pen pen){
			setValue(pen.getLevelValue(levelType));
		}

		public void setValue(float value){
			component.setText(format(value));
		}

		String format(float value){
			return FORMAT.format(value);
		}
	}

	static class AngleDisplay
		extends Display{
		static NumberFormat FORMAT=new DecimalFormat("###0.0\u00ba");
		static float RAD_TO_DEG=(float)(180f/Math.PI);

		AngleDisplay(PLevel.Type levelType){
			super(levelType);
		}

		String format(float value){
			value*=RAD_TO_DEG;
			return FORMAT.format(value);
		}
	}

	final Box panel=Box.createVerticalBox();
	{
		for(PLevel.Type levelType:PLevel.Type.VALUES){
			panel.add(Utils.labelComponent(
			            levelType.toString(),levelTypeToDisplay.get(levelType).component
			          ));
		}
	}

	LevelsPanel(Pen pen){
		pen.addListener(new PenAdapter(){
										private Pen pen;
			                @Override
			                public void penLevelEvent(PLevelEvent ev){
												pen=ev.pen;
				                for(PLevel level: ev.levels){
					                Display display=levelTypeToDisplay.get(level.getType());
													if(display!=null)
														display.setIsDirty(true);
				                }
			                }
											@Override
											public void penTock(long availableMillis){
												if(pen==null)
													return;
												for(Display display: levelTypeToDisplay.values())
													display.update(pen);
												pen=null;
											}
		                });
		for(PLevel.Type levelType: PLevel.Type.VALUES){
			Display display=levelTypeToDisplay.get(levelType);
			display.setValue(pen.getLevelValue(levelType));
		}
	}

}
