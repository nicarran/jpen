package jpen.owner.awt;

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

class ActiveWindow
	implements PropertyChangeListener, Runnable{
	interface Listener{
		void activeWindowChanged(Window newWindow);
	}

	private final Listener listener;
	private final KeyboardFocusManager keyboardFocusManager;
	private Window activeWindow;

	ActiveWindow(Listener listener){
		this.listener=listener;
		this.keyboardFocusManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		keyboardFocusManager.addPropertyChangeListener("activeWindow",this);
	}

	//@Override
	public void propertyChange(PropertyChangeEvent ev){
		Window activeWindow=(Window)ev.getNewValue();
		if(activeWindow==null){
			if(nullWindowTask==null || nullWindowTask.isDone()){
				nullWindowTask=nullWindowScheduler.schedule(this, 50, TimeUnit.MILLISECONDS);
			}
			return;
		}
		if(this.activeWindow==activeWindow)
			return;
		if(nullWindowTask!=null)
			nullWindowTask.cancel(false);
		setActiveWindow(activeWindow);
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
					setActiveWindow(null);
				}
			};

	private synchronized void setActiveWindow(Window activeWindow){
		this.activeWindow=activeWindow;
		listener.activeWindowChanged(activeWindow);
	}
}