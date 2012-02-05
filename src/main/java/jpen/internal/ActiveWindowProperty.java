/* [{
Copyright 2009 Nicolas Carranza <nicarran at gmail.com>

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

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
/**
Allows to keep an eye on the application active window avoiding the unnecessary null activeWindow change reported by the default KeyboardFocusManager when switching windows. 
*/
public final class ActiveWindowProperty
	implements PropertyChangeListener, Runnable{

	public interface Listener{
		void activeWindowChanged(Window newWindow);
	}

	private final Listener listener;
	private Window activeWindow;

	public ActiveWindowProperty(Listener listener){
		this.listener=listener;
		KeyboardFocusManager keyboardFocusManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		keyboardFocusManager.addPropertyChangeListener("activeWindow",this);
		activeWindow=keyboardFocusManager.getActiveWindow();
	}

	public synchronized Window get(){
		return activeWindow;
	}

	private synchronized void set(Window activeWindow){
		this.activeWindow=activeWindow;
		listener.activeWindowChanged(activeWindow);
	}

	//@Override
	public void propertyChange(PropertyChangeEvent ev){
		Window activeWindow=(Window)ev.getNewValue();
		if(activeWindow==this.activeWindow)
			return;
		if(activeWindow==null){
			// if the new activeWindow is null then we do the change only after a delay to avoid unnecessary changes to null (java does change the activeWindow to null when switching).
			if(nullWindowTask==null || nullWindowTask.isDone())
				nullWindowTask=nullWindowScheduler.schedule(this, 50, TimeUnit.MILLISECONDS);
			return;
		}
		if(nullWindowTask!=null){
			nullWindowTask.cancel(false);
			nullWindowTask=null;
		}
		set(activeWindow);
	}

	private final ScheduledExecutorService nullWindowScheduler=Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){
				//@Override
				public Thread newThread(Runnable runnable){
					Thread t=new Thread(runnable, "jpen-ActiveWindow-filter");
					t.setDaemon(true);
					return t;
				}
			});
	private ScheduledFuture nullWindowTask;

	//@Override
	public void run(){
		try{
			SwingUtilities.invokeAndWait(nullWindowRunnable);
		}catch(Exception ex){
			throw new AssertionError(ex);
		}
	}

	private final Runnable nullWindowRunnable=new Runnable(){
				//@Override
				public void run(){
					set(null);
				}
			};
}