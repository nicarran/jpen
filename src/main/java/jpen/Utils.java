/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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
package jpen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ResourceBundle;

/**
Various utility methods. Don't use this class directly, it is only intended to be used internally by jpen and its providers.
*/
public final class Utils{
	private static ResourceBundle moduleProperties;
	
	/**
	@deprecated Use {@link #getBuildProperties()}.
	*/
	@Deprecated
	public static ResourceBundle getModuleProperties(){
		return getBuildProperties();
	}
	
	public static ResourceBundle getBuildProperties() {
		if(moduleProperties==null) {
			moduleProperties=ResourceBundle.getBundle("jpen.build");
		}
		return moduleProperties;
	}

	public static String getModuleId(){
		return getBuildProperties().getString("module.id");
	}

	public static String getFullVersion(){
		return getVersion()+"-"+getBuild();
	}

	public static String getVersion(){
		return getBuildProperties().getString("module.version");
	}

	public static String getBuild(){
		return getBuildProperties().getString("module.distVersion");
	}
	
	public static String evalStackTrace(Throwable t){
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}

	public static void synchronizedWait(Object lock, long timeout){
		synchronized(lock){
			waitUninterrupted(lock, timeout);
		}
	}

	public static void waitUninterrupted(Object lock, long timeout){
		try{
			lock.wait(timeout);
		}catch(InterruptedException ex){
			throw new AssertionError(ex);
		}
	}

	public static void waitUninterrupted(Object lock){
		waitUninterrupted(lock, 0l);
	}

	public static void sleepUninterrupted(long millis){
		try{
			Thread.currentThread().sleep(millis);
		}catch(InterruptedException ex){
			throw new AssertionError(ex);
		}
	}

	public static final class AccessibleField{
		private final Class clazz;
		private final String fieldName;
		private Field field;
		AccessibleField(Class clazz, String fieldName){
			this.clazz=clazz;
			this.fieldName=fieldName;
		}

		Field getField(){
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
}