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

public final class Utils{
	private static final Logger L=Logger.getLogger(Utils.class.getName());
	private static ResourceBundle moduleProperties;

	private static final ResourceBundle getModuleProperties() {
		if(moduleProperties==null) {
			moduleProperties=ResourceBundle.getBundle("jpen.module");
		}
		return moduleProperties;
	}

	public static final String getModuleId(){
		return getModuleProperties().getString("module.id");
	}

	public static final String getFullVersion(){
		return getVersion()+"-"+getDistVersion();
	}

	public static final String getVersion(){
		return getModuleProperties().getString("module.version");
	}

	public static final String getDistVersion(){
		return getModuleProperties().getString("module.distVersion");
	}
}
