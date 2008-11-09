package jpen.provider.osx;

import jpen.PKind;
import jpen.provider.AbstractPenDevice;

public class CocoaDevice extends AbstractPenDevice {
	private final PKind.Type type;

	public CocoaDevice(final CocoaProvider _cocoaProvider, final PKind.Type _type) {
		super(_cocoaProvider);
		type = _type;
		setKindTypeNumber(type.ordinal());
	}
	
	public String getName() {
		switch (type) {
		case CURSOR: return "Cocoa Cursor";
		case ERASER: return "Cocoa Eraser";
		case STYLUS: return "Cocoa Stylus";
		default:
			return "UNKNOWN";
		}
	}
}
