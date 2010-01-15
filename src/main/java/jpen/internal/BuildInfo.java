/* [{
Copyright 2010 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.internal;

import java.util.ResourceBundle;

public final class BuildInfo{

	private static ResourceBundle buildProperties;

	private BuildInfo(){}

	public static ResourceBundle getProperties() {
		if(buildProperties==null) {
			buildProperties=ResourceBundle.getBundle("jpen.build");
		}
		return buildProperties;
	}

	public static String getModuleId(){
		return getProperties().getString("module.id");
	}

	public static String getFullVersion(){
		return getVersion()+"-"+getDistVersion();
	}

	public static String getVersion(){
		return getProperties().getString("module.version");
	}

	public static String getDistVersion(){
		return getProperties().getString("module.distVersion");
	}
}