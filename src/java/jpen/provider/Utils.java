/* [{
* (C) Copyright 2007 Nicolas Carranza and individual contributors.
* See the jpen-copyright.txt file in the jpen distribution for a full
* listing of individual contributors.
*
* This file is part of jpen.
*
* jpen is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* jpen is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with jpen.  If not, see <http://www.gnu.org/licenses/>.
* }] */
package jpen.provider;

import java.applet.Applet;
import java.awt.Component;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class Utils {
	private static final String DIST_REVISION = "${VERSION}";
	private static final String DIST_NAME = "${DIST_NAME}";
	
	static {
		// Awaken status:
		System.out.println("JPen status:");
		System.out.println("-  dist_name ... " + getDistName());
		System.out.println("-  revision .... " + getDistRevision());
	}
	
	
	private static final Logger L=Logger.getLogger(Utils.class.getName());
	private static ResourceBundle moduleProperties;

	/**
	@return the container window or null if 
	*/
	public static final Window getLocationOnScreen(Component c, Point2D.Float location) {
		if(location!=null)
			location.x=location.y=0;
		for(Component parentComponent=c; parentComponent!=null; parentComponent=parentComponent.getParent()) {
			if(parentComponent instanceof Applet) {
				try {
					Point p=parentComponent.getLocationOnScreen();
					if(location!=null){
						location.x+=p.x;
						location.y+=p.y;
					}
				} catch(IllegalComponentStateException ex) {
					L.info("Applet was not ready to get location on screen");
				}
				return null;
			}
			if(location!=null){
				location.x+=parentComponent.getX();
				location.y+=parentComponent.getY();
			}
			if(parentComponent instanceof Window)
				return (Window)parentComponent;
		}
		//L.info("Only top level containers of type Window or Applet are supported.");
		return null;
	}

	private static final ResourceBundle getModuleProperties() {
		if(moduleProperties==null) {
			moduleProperties=ResourceBundle.getBundle("jpen.module");
		}
		return moduleProperties;
	}

	private static final String getJniLibName() {
		return getModuleProperties().getString("module.id")+"-"+
		       getModuleProperties().getString("module.version");
	}

	public static final String getDistVersion(){
		return getModuleProperties().getString("module.distVersion");
	}
	
	
	private static final String getNewJniLibName() {
		return getDistName();
	}

	public static final String getNewDistVersion(){
		return getDistRevision();
	}
	
	
	public static String getDistName() {
//		return readProperty("dist_name");
		return DIST_NAME;
	}
	
	public static String getDistRevision() {
//		return readProperty("revision");
		return DIST_REVISION;
	}
	
	private static String readProperty(final String name) {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				final InputStream is = Utils.class.getResourceAsStream("/" + name);
				if (null == is)
					return null;
				try {
					final BufferedReader r = new BufferedReader(new InputStreamReader(is));
					try {
						return r.readLine();
					}
					finally {
						r.close();
					}
				}
				catch (IOException e) {
					System.err.println("Could not read \"" + name + "\"");
				}
				return null;
			}
		});
	}
	

	public static final void loadLibrary() {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				boolean newLib = false;
				final String newJniLibName = getNewJniLibName();
				if (null != newJniLibName) {
					try {
						System.loadLibrary(newJniLibName);
						newLib = true;
					}
					catch (UnsatisfiedLinkError e) {
						// Fall through
					}
				}
				if (! newLib) {
					// Try the old lib:
					System.loadLibrary(getJniLibName());
				}
				return null;
			}
		});
	}

	public static final void loadLibraryOrFail() {
		try {
			loadLibrary();
		} catch(Exception ex) {
			throw new AssertionError(ex);
		}
	}
	
}
