package jpen.provider.osx;

import jpen.PenManager;
import jpen.PenProvider;
import jpen.PenProvider.ConstructionException;
import jpen.provider.AbstractPenProvider;
import jpen.provider.Utils;
import jpen.provider.wintab.WintabAccess;
import jpen.provider.wintab.WintabProvider;

public class CocoaProvider extends AbstractPenProvider {

	
	
	public static class Constructor
	implements PenProvider.Constructor {
	public String getName() {
		return "OSX";
	}
	public boolean constructable() {
		return System.getProperty("os.name").toLowerCase().contains("mac");
	}

	public PenProvider construct(PenManager pm) throws ConstructionException {
		try {
			Utils.loadLibrary();
			CocoaAccess cocoaAccess=new CocoaAccess();
			return new CocoaProvider(pm, this, cocoaAccess);
		} catch(Throwable t) {
			throw new ConstructionException(t);
		}
	}
}
}
