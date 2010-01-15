/* [{
Copyright 2010 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.internal;

public class Range {
	public final float min;
	public final float max;
	private final float range;

	public Range(float min, float max) {
		if(min<0 && max>0 && Math.abs(max+min)==1){ // trick to avoid loosing the tool center
			if(max>-min)
				min=-max;
			else
				max=-min;
		}
		this.min=min;
		this.max=max;
		this.range=max-min;
	}
	
	public final float getRangedValue(float value) {
		return (value-min)/range;
	}

	@Override
	public String toString() {
		return "[PLevel.Range: "+min+", "+max+"]";
	}
}