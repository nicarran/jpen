import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import jpen.event.PenAdapter;
import jpen.owner.ScreenPenOwner;
import jpen.PButtonEvent;
import jpen.PenManager;
import jpen.PKindEvent;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.provider.wintab.WintabProvider;
import jpen.PScrollEvent;

public class SystemCursorTest2{
	
	public static void main(String... args) {
		// ScreenPenOwner fires only level events coming from the tablet and only when the application window is active:
		PenManager penManager=new PenManager(ScreenPenOwner.getInstance());
		// Thinking about controlling a music instrument... change the pen firing frequency to a high value to get events as soon as posible (no buffering): 
		penManager.pen.setFrequencyLater(2000);
		
		penManager.pen.addListener(new PenAdapter(){
				final StringBuilder sb=new StringBuilder();
					@Override
					public void penLevelEvent(PLevelEvent ev){
						if(!ev.isMovement())
							return;
						sb.setLength(0);
						sb.append("deviceId="+ev.getDeviceId());
						sb.append(", X="+ev.pen.getLevelValue(PLevel.Type.X));
						sb.append(", Y="+ev.pen.getLevelValue(PLevel.Type.Y));
						System.out.println(sb.toString());
					}
					@Override
					public void penButtonEvent(PButtonEvent ev){
						System.out.println("ev=" + ev);
					}
					@Override
					public void penScrollEvent(PScrollEvent ev){
						System.out.println("ev=" + ev);
					}
					@Override
					public void penKindEvent(PKindEvent ev){
						System.out.println("ev=" + ev);
					}
					@Override
					public void penTock(long availableMillis){
						System.out.println("availableMillis=" + availableMillis);
					}
				});
		final WintabProvider wintabProvider=penManager.getProvider(WintabProvider.class);
		if(wintabProvider==null){
			System.out.println("WintabAccess not found. Are you on Windows?");
			return;
		}
		
		wintabProvider.setSystemCursorEnabled(false);

		final JCheckBox checkBox=new JCheckBox("Enable System Cursor");
		checkBox.setSelected(wintabProvider.getSystemCursorEnabled());
		checkBox.addActionListener(
			new ActionListener(){
				//@Override
				public void actionPerformed(ActionEvent ev){
					wintabProvider.setSystemCursorEnabled(checkBox.isSelected());
				}
			}
		);
		
		JFrame frame=new JFrame("SystemCursorTest");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(checkBox);
		frame.pack();
		frame.setVisible(true);
	}
}