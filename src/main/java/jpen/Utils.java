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

public final class Utils{
	public static String evalStackTrace(Throwable t){
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}

	public static void synchronizedWait(Object lock, long timeout){
		synchronized(lock){
			try{
				lock.wait(timeout);
			}catch(InterruptedException ex){
				throw new AssertionError(ex);
			}
		}
	}
	
	public static void sleepUninterrupted(long millis){
		try{
			Thread.currentThread().sleep(millis);
		}catch(InterruptedException ex){
			throw new AssertionError(ex);
		}
	}
}
