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

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class StatusReportPanel{
	private final JTextArea textArea=new JTextArea();
	
	final JScrollPane panel=new JScrollPane(textArea);
	{
		panel.setPreferredSize(new Dimension(600, 430));
	}
	
	StatusReportPanel(StatusReport statusReport){
		textArea.setText(statusReport.toString());
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setCaretPosition(0);
	}
}
//===== JPen - Status Report =====
//	JPen Version: 081223
//	Date: Sun Jan 11 17:57:44 PST 2009
//	Providers:
//	   Constructor: Cocoa
//	      Construction Exception: jpen.PenProvider$ConstructionException: jpen.provider.NativeLibraryLoader$LoadException: java.lang.UnsatisfiedLinkError: /Library/Java/Extensions/libjpen-2.jnilib: 
//		at jpen.provider.AbstractPenProvider$AbstractConstructor.construct(AbstractPenProvider.java:87)
//		at jpen.PenManager.addProvider(PenManager.java:69)
//		at jpen.PenManager.<init>(PenManager.java:57)
//		at jpen.demo.PenCanvas.<init>(PenCanvas.java:73)
//		at jpen.demo.JPenDemoControl.<init>(JPenDemoControl.java:49)
//		at jpen.demo.JPenDemoApplet.init(JPenDemoApplet.java:35)
//		at sun.applet.AppletPanel.run(AppletPanel.java:425)
//		at java.lang.Thread.run(Thread.java:637)
//	Caused by: jpen.provider.NativeLibraryLoader$LoadException: java.lang.UnsatisfiedLinkError: /Library/Java/Extensions/libjpen-2.jnilib: 
//		at jpen.provider.NativeLibraryLoader.load(NativeLibraryLoader.java:85)
//		at jpen.provider.osx.CocoaProvider$Constructor.constructProvider(CocoaProvider.java:46)
//		at jpen.provider.AbstractPenProvider$AbstractConstructor.construct(AbstractPenProvider.java:85)
//		... 7 more
//	Caused by: java.lang.UnsatisfiedLinkError: /Library/Java/Extensions/libjpen-2.jnilib: 
//		at java.lang.ClassLoader$NativeLibrary.load(Native Method)
//		at java.lang.ClassLoader.loadLibrary0(ClassLoader.java:1822)
//		at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1715)
//		at java.lang.Runtime.loadLibrary0(Runtime.java:823)
//		at java.lang.System.loadLibrary(System.java:1030)
//		at jpen.provider.Utils$1.run(Utils.java:107)
//		at java.security.AccessController.doPrivileged(Native Method)
//		at jpen.provider.Utils.loadLibrary(Utils.java:104)
//		at jpen.provider.NativeLibraryLoader.load(NativeLibraryLoader.java:73)
//		... 9 more
//	   Constructor: System
//	      Construction Exception: none
//	      Device: Mouse (Mouse@System)
//	         Is Digitizer: false
//	         Enabled: true
//	         Kind: (type=CURSOR)
//	System Properties:
//	   apple.awt.graphics.UseOpenGL: false
//	   apple.awt.graphics.UseQuartz: false
//	   awt.nativeDoubleBuffering: true
//	   awt.toolkit: apple.awt.CToolkit
//	   browser: sun.applet.AppletViewer
//	   browser.vendor: Sun Microsystems Inc.
//	   browser.version: 1.06
//	   file.encoding: MacRoman
//	   file.encoding.pkg: sun.io
//	   file.separator: /
//	   file.separator.applet: true
//	   ftp.nonProxyHosts: local|*.local|169.254/16|*.169.254/16
//	   gopherProxySet: false
//	   http.agent: Java(tm) 2 SDK, Standard Edition v1.6.0_07
//	   http.nonProxyHosts: local|*.local|169.254/16|*.169.254/16
//	   http.proxyHost:
//	   http.proxyPort: 80
//	   java.awt.graphicsenv: apple.awt.CGraphicsEnvironment
//	   java.awt.printerjob: apple.awt.CPrinterJob
//	   java.class.path: /Users/marcello/Documents/workspace-marcello/jpen/bin
//	   java.class.version: 50.0
//	   java.class.version.applet: true
//	   java.endorsed.dirs: /System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home/lib/endorsed
//	   java.ext.dirs: /Library/Java/Extensions:/System/Library/Java/Extensions:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home/lib/ext
//	   java.home: /System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home
//	   java.library.path: .:/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java
//	   java.runtime.name: Java(TM) SE Runtime Environment
//	   java.runtime.version: 1.6.0_07-b06-153
//	   java.security.policy: java.policy.applet
//	   java.specification.name: Java Platform API Specification
//	   java.specification.vendor: Sun Microsystems Inc.
//	   java.specification.version: 1.6
//	   java.vendor: Apple Inc.
//	   java.vendor.applet: true
//	   java.vendor.url: http://www.apple.com/
//	   java.vendor.url.applet: true
//	   java.vendor.url.bug: http://bugreport.apple.com/
//	   java.version: 1.6.0_07
//	   java.version.applet: true
//	   java.vm.info: mixed mode
//	   java.vm.name: Java HotSpot(TM) 64-Bit Server VM
//	   java.vm.specification.name: Java Virtual Machine Specification
//	   java.vm.specification.vendor: Sun Microsystems Inc.
//	   java.vm.specification.version: 1.0
//	   java.vm.vendor: Apple Inc.
//	   java.vm.version: 1.6.0_07-b06-57
//	   line.separator.applet: true
//	   mrj.version: 1040.1.6.0_07-153
//	   os.arch: x86_64
//	   os.arch.applet: true
//	   os.name: Mac OS X
//	   os.name.applet: true
//	   os.version: 10.5.6
//	   os.version.applet: true
//	   package.restrict.access.sun: true
//	   package.restrict.definition.java: true
//	   package.restrict.definition.sun: true
//	   path.separator: :
//	   path.separator.applet: true
//	   socksNonProxyHosts: local|*.local|169.254/16|*.169.254/16
//	   sun.arch.data.model: 64
//	   sun.awt.exception.handler: apple.awt.CToolkit$EventQueueExceptionHandler
//	   sun.boot.class.path: /System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/classes.jar:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/ui.jar:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/laf.jar:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/sunrsasign.jar:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/jsse.jar:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/jce.jar:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/charsets.jar
//	   sun.boot.library.path: /System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Libraries
//	   sun.cpu.endian: little
//	   sun.cpu.isalist:
//	   sun.io.unicode.encoding: UnicodeLittle
//	   sun.java.launcher: SUN_STANDARD
//	   sun.jnu.encoding: MacRoman
//	   sun.management.compiler: HotSpot 64-Bit Server Compiler
//	   sun.os.patch.level: unknown
//	   user.country: US
//	   user.language: en
//	   user.timezone: America/Los_Angeles
//	===== ===== =====
