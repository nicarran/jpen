/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.demo.inspect;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import jpen.PenManager;

public class Inspector{
	static final Logger L=Logger.getLogger(Inspector.class.getName());
	//static { L.setLevel(Level.ALL); }

	final PenManager penManager;
	final FileHandler fileHandler;
	final InspectorThread inspectorThread;

	public Inspector(PenManager penManager, String loggerName, int periodInSec) throws IOException{
		this.penManager=penManager;
		
		String fileHandlerName=evalFileHandlerName(loggerName);
		fileHandler=new FileHandler(fileHandlerName);
		fileHandler.setFormatter(new SimpleFormatter());
		fileHandler.setLevel(Level.ALL);
		L.info("logging to file:"+fileHandlerName);

		Logger logger=Logger.getLogger(loggerName);
		logger.setLevel(Level.ALL);
		logger.addHandler(fileHandler);

		inspectorThread=new InspectorThread(this, periodInSec*1000/2, 2);
	}

	private String evalFileHandlerName(String loggerName){
		StringBuilder sb=new StringBuilder(loggerName);
		sb.append("-inspect-");
		sb.append(new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		sb.append(".txt");
		return sb.toString();
	}
}
