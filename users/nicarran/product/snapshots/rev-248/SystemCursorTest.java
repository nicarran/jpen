import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import jpen.event.PenAdapter;
import jpen.owner.ScreenPenOwner;
import jpen.PButtonEvent;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PLevelEvent;
import jpen.provider.wintab.WintabAccess;
import jpen.provider.wintab.WintabProvider;

class SystemCursorTest{
	
	public static void main(String... args) {
		//v
		PenManager penManager=new PenManager(ScreenPenOwner.getInstance());
		//^ ScreenPenOwner fires only level events coming from the tablet and only when the application window is active.
		penManager.pen.addListener(new PenAdapter(){
					@Override
					public void penLevelEvent(PLevelEvent ev){
						System.out.println("level event=" + ev);
					}
				});

		final WintabAccess wintabAccess=getWintabAccess(penManager);
		if(wintabAccess==null){
			System.out.println("WintabAccess not found. Are you on Windows?");
			return;
		}

		final JCheckBox checkBox=new JCheckBox("Enable System Cursor");
		checkBox.setSelected(wintabAccess.getSystemCursorEnabled());
		checkBox.addActionListener(
			new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent ev){
					wintabAccess.setSystemCursorEnabled(checkBox.isSelected());
				}
			}
		);

		JFrame frame=new JFrame("SystemCursorTest");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(checkBox);
		frame.pack();
		frame.setVisible(true);
	}

	static WintabAccess getWintabAccess(PenManager penManager){
		WintabProvider wintabProvider=null;
		//v do reflection to get the WintabProvider. If the get/setSystemCursorEnabled feature is useful we can move it to a higher level API (no-reflection) and implement it on linux (and osx if possible). 
		for(PenProvider.Constructor constructor:penManager.getProviderConstructors()){
			PenProvider penProvider=constructor.getConstructed();
			if(penProvider instanceof WintabProvider){
				wintabProvider=(WintabProvider)penProvider;
				break;
			}
		}
		return wintabProvider==null? null: wintabProvider.wintabAccess;
		//^
	}
}