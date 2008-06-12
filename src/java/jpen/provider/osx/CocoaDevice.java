package jpen.provider.osx;

import jpen.PKind;
import jpen.provider.AbstractPenDevice;

public class CocoaDevice extends AbstractPenDevice {
	private final PKind.Type type;
	
	public CocoaDevice(final CocoaProvider _cocoaProvider, final PKind.Type _type) {
		super(_cocoaProvider);
		
		type = _type;
	}
	
	public String getName() {
		switch (type) {
			case CURSOR: return "OS X Cursor";
			case ERASER: return "OS X Eraser";
			case STYLUS: return "OS X Stylus";
			default:
				return "UNKNOWN";
		}
	}
}
