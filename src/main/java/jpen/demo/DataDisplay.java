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

import javax.swing.JComponent;
import jpen.Pen;

abstract class DataDisplay<
	C extends JComponent>{
	final C component;
	private boolean isDirty;

	DataDisplay(C component){
		this.component=component;
	}

	void setIsDirty(boolean isDirty){
		this.isDirty=isDirty;
	}
	
	boolean getIsDirty(){
		return isDirty;
	}

	final void update(Pen pen){
		if(getIsDirty())
			updateImp(pen);
		setIsDirty(false);
	}
	
	abstract void updateImp(Pen pen);
}
