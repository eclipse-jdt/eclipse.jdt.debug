/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.macosx;

import java.io.*;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;

public class MacOSXLaunchingPlugin extends Plugin {
	
	private static MacOSXLaunchingPlugin fgPlugin;
	private static final String RESOURCE_BUNDLE= "org.eclipse.jdt.internal.launching.macosx.MacOSXLauncherMessages";//$NON-NLS-1$
	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	public MacOSXLaunchingPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgPlugin= this;
	}
	
	public static MacOSXLaunchingPlugin getDefault() {
		return fgPlugin;
	}
	
	static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.jdt.launching.macosx"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	static String[] wrap(Class clazz, String[] cmdLine) {
		
//		System.err.println("wrap:");
//		for (int ii= 0; ii < cmdLine.length; ii++) {
//			System.err.println("  " + cmdLine[ii]);
//		}
//		System.err.println();
		
		for (int i= 0; i < cmdLine.length; i++) {
			String arg= cmdLine[i];
			if (arg.indexOf("swt.jar") >= 0 || arg.indexOf("org.eclipse.swt") >= 0 || "-ws".equals(arg)) {	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String[] cmdLine2= new String[cmdLine.length + 2];
				
				String wrapper= "/tmp/start_carbon.sh";	//$NON-NLS-1$
				copy(clazz, "start_carbon.sh", wrapper);	//$NON-NLS-1$
				copy(clazz, "JavaApplicationStub", "/tmp/JavaApplicationStub");	//$NON-NLS-1$ //$NON-NLS-2$
				
				int j= 0;
				cmdLine2[j++]= "/bin/sh";	//$NON-NLS-1$
				cmdLine2[j++]= wrapper;
				for (i= 0; i < cmdLine.length; i++)
					cmdLine2[j++]= cmdLine[i];
		
				return cmdLine2;
			}
		}
		return cmdLine;
	}
		
	static void copy(Class where, String from, String to) {
		
		String output= to;
		FileOutputStream os= null;
		try {
			os= new FileOutputStream(output);
		} catch (FileNotFoundException ex) {
			return;
		}

		InputStream is= null;
		try {
			is= where.getResourceAsStream(from);
			if (is != null) {
				while (true) {
					int c= is.read();
					if (c == -1)
						break;
					os.write(c);
				}
			}
			os.flush();
		} catch (IOException io) {
			return;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			try {
				os.close();
			} catch(IOException e) {
			}
		}
	}
}
