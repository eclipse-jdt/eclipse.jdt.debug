/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.macosx;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.boot.BootLoader;
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
		
		for (int i= 0; i < cmdLine.length; i++) {
			// test whether we depend on SWT
			if (useSWT(cmdLine[i]))
				return createSWTlauncher(clazz, cmdLine, cmdLine[0]);
		}
		return cmdLine;
	}
	
	/*
	 * Heuristics: returns true if given argument refers to SWT. 
	 */
	private static boolean useSWT(String arg) {
		return arg.indexOf("swt.jar") >= 0 ||	//$NON-NLS-1$
			   arg.indexOf("org.eclipse.swt") >= 0 ||	//$NON-NLS-1$
			   "-ws".equals(arg);	//$NON-NLS-1$
	}
	
	/**
	 * Returns path to executable.
	 */
	static String[] createSWTlauncher(Class clazz, String[] cmdLine, String vmVersion) {
				
		// the following property is defined if Eclipse is started via java_swt
		String java_swt= System.getProperty("org.eclipse.swtlauncher");	//$NON-NLS-1$
		if (java_swt == null) {
			// if not defined try to guess...
			URL url= BootLoader.getInstallURL();
			java_swt= url.getPath() + "Eclipse.app/Contents/MacOS/java_swt"; //$NON-NLS-1$
		}
		if (java_swt == null)
			return cmdLine;		// give up
		
		String[] newCmdLine= new String[cmdLine.length+1];
		int argCount= 0;
		newCmdLine[argCount++]= java_swt;
		newCmdLine[argCount++]= "-XXvm=" + vmVersion; //$NON-NLS-1$
		for (int i= 1; i < cmdLine.length; i++)
			newCmdLine[argCount++]= cmdLine[i];
		
		return newCmdLine;
	}	
}
