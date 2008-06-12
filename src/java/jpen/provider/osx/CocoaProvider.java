package jpen.provider.osx;

import java.util.HashMap;
import java.util.Map;

import jpen.PKind;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.provider.AbstractPenProvider;
import jpen.provider.Utils;

public class CocoaProvider extends AbstractPenProvider {
	public static class Constructor implements PenProvider.Constructor {
		public Constructor() {
		}
		
		public String getName() {
			return "OS X";
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

	
	private final CocoaAccess cocoaAccess;
	private final Map<PKind.Type, CocoaDevice> deviceMap;
	
	private CocoaProvider(final PenManager _penManager, 
			final Constructor _constructor, final CocoaAccess _cocoaAccess
	) {
		super(_penManager, _constructor);
		cocoaAccess = _cocoaAccess;
		cocoaAccess.setProvider(this);
		
		deviceMap = new HashMap<PKind.Type, CocoaDevice>(3);
		for (PKind.Type type : PKind.Type.VALUES) {
			deviceMap.put(type, new CocoaDevice(this, type));
		}
		
		// TODO: when do we stop?
		cocoaAccess.start();
	}
	
	public void penManagerPaused(final boolean paused) {
		// Do nothing
	}
}
