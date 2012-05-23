/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.macosx;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Plugin;


public class MacOSXLaunchingPlugin extends Plugin {
	
	private static MacOSXLaunchingPlugin fgPlugin;
	private static final String RESOURCE_BUNDLE= "org.eclipse.jdt.internal.launching.macosx.MacOSXLauncherMessages";//$NON-NLS-1$
	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	public MacOSXLaunchingPlugin() {
		super();
		Assert.isTrue(fgPlugin == null);
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

	/*
	 * Convenience method which returns the unique identifier of this plug-in.
	 */
	static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plug-in id defined in plugin.xml
			return "org.eclipse.jdt.launching.macosx"; //$NON-NLS-1$
		}
		return getDefault().getBundle().getSymbolicName();
	}

	static String[] wrap(Class<?> clazz, String[] cmdLine) {
		
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
	 * @param clazz the class
	 * @param cmdLine the old command line
	 * @param vmVersion the version of the VM
	 * @return the new command line
	 * 
	 */
	static String[] createSWTlauncher(Class<?> clazz, String[] cmdLine, String vmVersion) {
		
		// the following property is defined if Eclipse is started via java_swt
		String java_swt= System.getProperty("org.eclipse.swtlauncher");	//$NON-NLS-1$
		
		if (java_swt == null) {	
			// not started via java_swt -> now we require that the VM supports the "-XstartOnFirstThread" option
			String[] newCmdLine= new String[cmdLine.length+1];
			int argCount= 0;
			newCmdLine[argCount++]= cmdLine[0];
			newCmdLine[argCount++]= "-XstartOnFirstThread"; //$NON-NLS-1$
			for (int i= 1; i < cmdLine.length; i++)
				newCmdLine[argCount++]= cmdLine[i];
			return newCmdLine;
		}
		
		try {
			// copy java_swt to /tmp in order to get the app name right
			Process process= Runtime.getRuntime().exec(new String[] { "/bin/cp", java_swt, "/tmp" }); //$NON-NLS-1$ //$NON-NLS-2$
			process.waitFor();
			java_swt= "/tmp/java_swt"; //$NON-NLS-1$
		} catch (IOException e) {
			// ignore and run java_swt in place
		} catch (InterruptedException e) {
			// ignore and run java_swt in place
		}
		
		String[] newCmdLine= new String[cmdLine.length+1];
		int argCount= 0;
		newCmdLine[argCount++]= java_swt;
		newCmdLine[argCount++]= "-XXvm=" + vmVersion; //$NON-NLS-1$
		for (int i= 1; i < cmdLine.length; i++)
			newCmdLine[argCount++]= cmdLine[i];
		
		return newCmdLine;
	}
}
