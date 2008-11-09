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
import java.awt.geom.Point2D;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.logging.Logger;
import java.util.ResourceBundle;

public final class Utils {
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

	public static final void loadLibrary() {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			    public Object run() {
				    System.loadLibrary(getJniLibName());
				    return null;
			    }
		    });
	}

	/**
	@deprecated Use {@link #loadLibrary()}.
	*/
	@Deprecated
	public static final void loadLibraryOrFail() {
		try {
			System.loadLibrary(getJniLibName());
		} catch(Exception ex) {
			throw new AssertionError(ex);
		}
	}

}
