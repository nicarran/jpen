/* % [{
% (C) Copyright 2008 Nicolas Carranza and individual contributors.
% See the jpen-copyright.txt file in the jpen distribution for a full
% listing of individual contributors.
%
% This file is part of jpen.
%
% jpen is free software: you can redistribute it and/or modify
% it under the terms of the GNU Lesser General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
%
% jpen is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU Lesser General Public License for more details.
%
% You should have received a copy of the GNU Lesser General Public License
% along with jpen.  If not, see <http://www.gnu.org/licenses/>.
% }] */
package jpen;

public class PenStateCopy
			extends PenState
	implements java.io.Serializable {
	public static final long serialVersionUID=1l;

	public PenStateCopy(){}

	public PenStateCopy(PenState penState){
		setValues(penState);
	}
	
	public PenState.Levels getLevels(){
		return levels;
	}

	public void setLevelValue(PLevel level){
		levels.setValue(level.typeNumber, level.value);
	}

	public void setLevelValue(PLevel.Type type, float value){
		levels.setValue(type, value);
	}

	public void setLevelValue(int typeNumber, float value){
		levels.setValue(typeNumber, value);
	}

	public boolean setButtonValue(PButton button) {
		return super.setButtonValue(button.typeNumber, button.value);
	}

	public boolean setButtonValue(int typeNumber, boolean value){
		return super.setButtonValue(typeNumber,value);
	}

	public boolean setButtonValue(PButton.Type type, boolean value){
		return super.setButtonValue(type.ordinal(), value);
	}

	public void setKind(PKind kind){
		super.setKind(kind);
	}

	public void setKind(PKind.Type type){
		super.setKind(PKind.valueOf(type.ordinal()));
	}

	public void setKind(int kindTypeNumber){
		super.setKind(PKind.valueOf(kindTypeNumber));
	}

	public void setValues(PenState penState){
		super.setValues(penState);
	}

	public void setValues(PenEvent ev){
		ev.copyTo(this);
	}
}
