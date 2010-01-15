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

public final class ObjectUtils{
	private ObjectUtils(){}

	public static void synchronizedWait(Object lock, long timeout){
		synchronized(lock){
			waitUninterrupted(lock, timeout);
		}
	}

	public static void waitUninterrupted(Object o, long timeout){
		try{
			o.wait(timeout);
		}catch(InterruptedException ex){
			throw new AssertionError(ex);
		}
	}

	public static void waitUninterrupted(Object lock){
		waitUninterrupted(lock, 0l);
	}
}