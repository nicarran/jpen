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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.SwingUtilities;
import jpen.event.PenListener;

class SwingPenListenerProxy{
	private final PenListener penListener;
	final PenListener proxy;

	SwingPenListenerProxy(PenListener penListener){
		this.penListener=penListener;
		proxy=(PenListener)Proxy.newProxyInstance(PenListener.class.getClassLoader(),
		      new Class[]{PenListener.class},
		      new SwingInvocationHandler());
	}

	class SwingInvocationHandler
		implements InvocationHandler{

		class SwingRunner
			implements Runnable{
			private Method method;
			private Object[] args;
			void setup(Method method, Object[] args){
				this.method=method;
				this.args=args;
			}
			//@Override
			public void run(){
				try{
					method.invoke(penListener, args);
				}catch(Exception ex){
					throw new AssertionError(ex);
				}
			}
		}

		SwingRunner swingRunner=new SwingRunner();

		//@Override
		public Object invoke(Object proxy, Method method, Object[] args){
			swingRunner.setup(method, args);
			try{
				SwingUtilities.invokeAndWait(swingRunner);
			}catch(Exception ex){
				throw new AssertionError(ex);
			}
			return null;
		}
	}
}
