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

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public final class AccessibleField{
	private final Class clazz;
	private final String fieldName;
	private Field field;

	public AccessibleField(Class clazz, String fieldName){
		this.clazz=clazz;
		this.fieldName=fieldName;
	}

	public Field getField(){
		if(field==null)
			try{
				field=getAccessibleField(clazz, fieldName);
			}catch(PrivilegedActionException ex){
				throw new AssertionError(ex);
			}
		return field;
	}

	private static Field getAccessibleField(final Class clazz, final String fieldName)
	throws PrivilegedActionException{
		return AccessController.doPrivileged(new PrivilegedExceptionAction<Field>(){
						 //@Override
						 public Field run() throws Exception{
							 Field field=clazz.getDeclaredField(fieldName);
							 field.setAccessible(true);
							 return field;
						 }
					 });
	}
}