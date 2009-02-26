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
	static { L.setLevel(Level.ALL); }

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
