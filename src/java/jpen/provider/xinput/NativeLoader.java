/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.provider.xinput;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.prefs.Preferences;
import jpen.provider.Utils;

class NativeLoader{
	private static final Logger L=Logger.getLogger(NativeLoader.class.getName());
	{
		//L.setLevel(Level.ALL);
	}
	private static Map<String, Collection<String>> dataModelToArchitectures=new HashMap<String, Collection<String>>();
	static{
		dataModelToArchitectures.put("32", Arrays.asList(new String[]{""}));
		dataModelToArchitectures.put("64", Arrays.asList(new String[]{"x86_64", "ia64"}));
	}

	private static String PREFERENCE_KEY$ARCHITECTURE="NativeLoader.arch";
	private static boolean loaded;

	static synchronized void load(){
		if(!loaded){
			String preferredArchitecture=getPreferredArchitecture();
			Throwable loadExceptionCause=null;
			if(preferredArchitecture!=null)
				try{
					L.info("loading preferred architecture: \""+preferredArchitecture+"\"");
					Utils.loadLibrary(preferredArchitecture);
				}catch(Throwable t){
					setPreferredArchitecture(null);
					loadExceptionCause=t;
				}
			else{
				String dataModel=getDataModel();
				Collection<String> architectures=dataModelToArchitectures.get(dataModel);
				if(architectures==null)
					throw new IllegalStateException("Unsupported data model: "+dataModel);
				for(String architecture: architectures){
					try{
						L.info("loading architecture: \""+architecture+"\"");
						Utils.loadLibrary(architecture);
						setPreferredArchitecture(architecture);
						loadExceptionCause=null;
						break;
					}catch(Throwable t){
						setPreferredArchitecture(null);
						loadExceptionCause=t;
					}
				}
			}
			loaded=true;
			if(loadExceptionCause!=null)
				throw new LoadException(loadExceptionCause);
		}
	}

	static class LoadException
		extends RuntimeException{
		LoadException(Throwable cause){
			super(cause);
		}
	}

	private static String getDataModel(){
		String dataModel=AccessController.doPrivileged(new PrivilegedAction<String>() {
			    //@Override
			    public String run() {
				    return System.getProperty("sun.arch.data.model");
			    }
		    });
		return dataModel==null? "32": dataModel;
	}

	private static String getPreferredArchitecture(){
		return AccessController.doPrivileged(new PrivilegedAction<String>(){
			       //@Override
			       public String run(){
				       String override=System.getProperty("jpen.provider.architecture");
				       if(override!=null)
					       return override;
				       Preferences preferences=Preferences.userNodeForPackage(NativeLoader.class);
				       return preferences.get(PREFERENCE_KEY$ARCHITECTURE, null);
			       }
		       });
	}

	private static void setPreferredArchitecture(final String architecture){
		AccessController.doPrivileged(new PrivilegedAction<Object>(){
			    //@Override
			    public String run(){
				    Preferences preferences=Preferences.userNodeForPackage(NativeLoader.class);
						L.info("setting preferred architecture to: \""+architecture+"\"");
				    if(architecture==null)
					    preferences.remove(PREFERENCE_KEY$ARCHITECTURE);
				    else
					    preferences.put(PREFERENCE_KEY$ARCHITECTURE, architecture);
				    return null;
			    }
		    });
	}
}
