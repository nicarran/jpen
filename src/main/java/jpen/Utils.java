package jpen;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Utils{
	public static String evalStackTrace(Throwable t){
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}
}
