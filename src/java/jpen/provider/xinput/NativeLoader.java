package jpen.provider.xinput;

import java.security.AccessController;
import java.security.PrivilegedAction;
import jpen.provider.Utils;

class NativeLoader{
	private static NativeLoader INSTANCE;

	static synchronized NativeLoader getInstance(){
		if(INSTANCE==null)
			INSTANCE=new NativeLoader();
		return INSTANCE;
	}

	private boolean loaded;

	private NativeLoader(){
	}

	public void load(){
		if(!loaded){
			Utils.loadLibrary(getArchitecture());
			loaded=true;
		}
	}

	public static String getArchitecture(){
		String architecture=AccessController.doPrivileged(new PrivilegedAction<String>() {
			    //@Override
			    public String run() {
				    String architecture=System.getProperty("sun.arch.data.model");
				    if(architecture==null)
					    return ""; // default to x86 - 32
				    architecture=architecture.trim();
				    if(architecture.equals("64"))
					    return "x86_64"; // amd64
				    // TODO: support more archs
				    return "";
			    }
		    });
		return architecture;
	}

}
