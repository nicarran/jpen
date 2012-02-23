/* [{
Copyright 2008 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.demo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PKind;
import jpen.internal.ThrowableUtils;

public class StatusReport{

	private final StringBuilder report=new StringBuilder();

	public StatusReport(PenManager penManager){
		appendHeader(penManager);
		appendPenThreadCrashInfo(penManager);
		appendProvidersInfo(penManager);
		appendSystemInfo(penManager);
		appendFooter(penManager);
	}

	private void appendLine(String line){
		appendLine(line, 0);
	}

	private void appendLine(String line, int indent){
		line=line.trim();
		for(int i=indent; --i>=0;)
			report.append("   ");
		report.append(line);
		if(!line.endsWith("\n"))
			report.append("\n");
	}

	private void appendHeader(PenManager penManager){
		appendLine("===== JPen - Status Report =====");
		appendLine("JPen Version: "+PenManager.getJPenFullVersion());
		appendLine("Date: "+new java.util.Date());
	}

	private void appendPenThreadCrashInfo(PenManager penManager){
		Exception penThreadCrashException=penManager.pen.getThreadException();
		if(penThreadCrashException!=null){
			appendLine("Pen Thread Crashed: "+ThrowableUtils.evalStackTraceString(penThreadCrashException));
		}
	}

	private void appendFooter(PenManager penManager){
		appendLine("===== ===== =====");
	}

	private static final Set<String> PRIVATE_SYSTEM_PROPERTIES=new HashSet<String>(Arrays.asList(
				new String[]{
					"user.dir",
					"java.io.tmpdir",
					"line.separator",
					"user.home",
					"user.name",
				}
			));

	private void appendSystemInfo(PenManager penManager){
		appendLine("System Properties:");
		try{
			TreeSet<Object> properties=new TreeSet<Object>(System.getProperties().keySet());
			for(Object property: properties){
				String propertyName=property.toString();
				if(PRIVATE_SYSTEM_PROPERTIES.contains(propertyName))
					continue;
				appendLine(propertyName+": "+System.getProperty(propertyName), 1);
			}
		}catch(SecurityException ex){
			appendLine("Security Exception: system properties are not readable.", 1);
		}
	}

	private void appendProvidersInfo(PenManager penManager){
		appendLine("Providers:");
		for(PenProvider.Constructor constructor: penManager.getProviderConstructors()){
			appendLine("Constructor: "+constructor.getName(), 1);
			PenProvider.ConstructionException constructionException=constructor.getConstructionException();
			String constructionExceptionStackTrace="none";
			if(constructionException!=null){
				constructionExceptionStackTrace=ThrowableUtils.evalStackTraceString(constructionException);
			}
			appendLine("Construction Exception: "+constructionExceptionStackTrace, 2);
			PenProvider penProvider=constructor.getConstructed();
			if(penProvider!=null){
				int nativeVersion=constructor.getNativeVersion();
				if(nativeVersion!=-1){
					appendLine("Native Version-Build(Expected): "+
										 constructor.getNativeVersion()+"-"+constructor.getNativeBuild()+
										 "("+constructor.getExpectedNativeBuild()+")", 2);
				}
				Collection<PenDevice> penDevices=penProvider.getDevices();
				for(PenDevice penDevice:penDevices){
					appendLine(
						(penManager.isSystemMouseDevice(penDevice)? "Device (System Mouse): ":"Device: ")+
						penDevice.getName()+" ("+penDevice.getPhysicalId()+")", 2);
					appendLine("Enabled: "+penDevice.getEnabled(), 3);
					appendLine("Kind: "+PKind.valueOf(penDevice.getKindTypeNumber()), 3);
					appendLine("Fractional Movements: "+penDevice.getUseFractionalMovements(), 3);
				}
			}
		}
	}

	@Override
	public String toString(){
		return report.toString();
	}

}