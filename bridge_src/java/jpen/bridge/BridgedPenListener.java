package jpen.bridge;

import java.util.EventListener;

public interface BridgedPenListener extends EventListener {
	public void penModeChanged(final BridgedPenEvent e);
	public void penMoved(final BridgedPenEvent e);
	public void penTilted(final BridgedPenEvent e);
	public void penPressed(final BridgedPenEvent e);
	public void penButtonPressed(final BridgedPenEvent e);
	public void penScrolled(final BridgedPenEvent e);
}
